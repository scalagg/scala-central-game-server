package gg.scala.cgs.server

import com.google.common.base.Suppliers
import java.io.File
import java.net.URLClassLoader
import java.util.function.Supplier

object Library {
    private val URL_INJECTOR: Supplier<URLClassLoaderAccess> = Suppliers.memoize { URLClassLoaderAccess.create(CGSServer().instance!!.javaClass.classLoader as URLClassLoader) }

    fun load(file: File) {
        try {
            URL_INJECTOR.get().addURL(file.toURI().toURL())
        } catch (e: Exception) {
            throw RuntimeException("Unable to load dependency", e)
        }
        println("Loaded dependency " + file.name)
    }
}
