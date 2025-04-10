package gg.scala.cgs.server

import java.lang.reflect.Method
import java.net.URL
import java.net.URLClassLoader
import javax.annotation.Nonnull

/**
 * Provides access to [URLClassLoader]#addURL.
 */
abstract class URLClassLoaderAccess protected constructor(private val classLoader: URLClassLoader?) {
    /**
     * Adds the given URL to the class loader.
     *
     * @param url the URL to add
     */
    abstract fun addURL(@Nonnull url: URL?)

    /**
     * Accesses using reflection, not supported on Java 9+.
     */
    private class Reflection(classLoader: URLClassLoader?) : URLClassLoaderAccess(classLoader) {
        override fun addURL(@Nonnull url: URL?) {
            try {
                ADD_URL_METHOD!!.invoke(super.classLoader, url)
            } catch (e: ReflectiveOperationException) {
                throw RuntimeException(e)
            }
        }

        companion object {
            private val ADD_URL_METHOD: Method?

            init {
                var addUrlMethod: Method?
                try {
                    addUrlMethod = URLClassLoader::class.java.getDeclaredMethod("addURL", URL::class.java)
                    addUrlMethod.isAccessible = true
                } catch (e: Exception) {
                    addUrlMethod = null
                }
                ADD_URL_METHOD = addUrlMethod
            }

            val isSupported: Boolean
                get() = ADD_URL_METHOD != null
        }
    }

    /**
     * Accesses using sun.misc.Unsafe, supported on Java 9+.
     *
     * @author Vaishnav Anil (https://github.com/slimjar/slimjar)
     */
    private class Unsafe(classLoader: URLClassLoader?) : URLClassLoaderAccess(classLoader) {
        private val unopenedURLs: MutableCollection<URL?>?
        private val pathURLs: MutableCollection<URL?>?

        init {
            var unopenedURLs: MutableCollection<URL?>?
            var pathURLs: MutableCollection<URL?>?
            try {
                val ucp = fetchField(
                    URLClassLoader::class.java, classLoader, "ucp"
                )
                unopenedURLs = fetchField(ucp.javaClass, ucp, "unopenedUrls") as MutableCollection<URL?>
                pathURLs = fetchField(ucp.javaClass, ucp, "path") as MutableCollection<URL?>
            } catch (e: Throwable) {
                unopenedURLs = null
                pathURLs = null
            }
            this.unopenedURLs = unopenedURLs
            this.pathURLs = pathURLs
        }

        override fun addURL(@Nonnull url: URL?) {
            unopenedURLs!!.add(url)
            pathURLs!!.add(url)
        }

        companion object {
            private val UNSAFE: sun.misc.Unsafe?

            init {
                var unsafe: sun.misc.Unsafe?
                try {
                    val unsafeField = sun.misc.Unsafe::class.java.getDeclaredField("theUnsafe")
                    unsafeField.isAccessible = true
                    unsafe = unsafeField[null] as sun.misc.Unsafe
                } catch (t: Throwable) {
                    unsafe = null
                }
                UNSAFE = unsafe
            }

            val isSupported: Boolean
                get() = UNSAFE != null

            @Throws(NoSuchFieldException::class)
            private fun fetchField(clazz: Class<*>, `object`: Any?, name: String): Any {
                val field = clazz.getDeclaredField(name)
                val offset = UNSAFE!!.objectFieldOffset(field)
                return UNSAFE.getObject(`object`, offset)
            }
        }
    }

    private class Noop : URLClassLoaderAccess(null) {
        override fun addURL(@Nonnull url: URL?) {
            throw UnsupportedOperationException()
        }

        companion object {
            val INSTANCE: Noop = Noop()
        }
    }

    companion object {
        /**
         * Creates a [URLClassLoaderAccess] for the given class loader.
         *
         * @param classLoader the class loader
         * @return the access object
         */
        @JvmStatic
        fun create(classLoader: URLClassLoader?): URLClassLoaderAccess =
            if (Reflection.isSupported)
                Reflection(classLoader)
            else if (Unsafe.isSupported)
                Unsafe(classLoader)
            else
                Noop.INSTANCE
    }
}