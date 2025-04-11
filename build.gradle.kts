import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("maven-publish")
    id("com.jfrog.artifactory") version "4.28.2"
    id("org.jetbrains.kotlin.jvm") version "2.0.0"
    id("org.jetbrains.kotlin.kapt") version "2.0.0"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

allprojects {
    group = "gg.scala.cgs"
    version = "1.0.0"

    repositories {
        mavenCentral()
        configureScalaRepository()
        maven {
            url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        }

        maven { url = uri("https://repo.glaremasters.me/repository/concuncan/") }

        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
        maven { url = uri("https://oss.sonatype.org/content/repositories/central") }
    }
}

subprojects {
    apply(plugin = "maven-publish")
    apply(plugin = "com.jfrog.artifactory")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.kapt")
    apply(plugin = "com.github.johnrengelman.shadow")

    dependencies {
        compileOnly("org.jetbrains.kotlin:kotlin-stdlib")
        implementation("redis.clients:jedis:2.8.0")

        compileOnly("gg.scala.lemon:bukkit:1.9.0")
        compileOnly("gg.scala.spigot:server:1.1.0")

        compileOnly("gg.scala.store:spigot:0.1.8")
        compileOnly("gg.scala.store:shared:0.1.8")

        compileOnly("gg.scala.cloudsync:spigot:1.0.1")

        compileOnly("com.grinderwolf:slimeworldmanager-api:2.2.1")

        compileOnly("gg.scala.commons:bukkit:4.0.1")
//        kapt("gg.scala.commons:bukkit:4.0.1")
    }

    kotlin {
        jvmToolchain(jdkVersion = 17)
    }

    tasks {
        withType<ShadowJar> {
            archiveClassifier.set("")
            exclude(
                "**/*.kotlin_metadata",
                "**/*.kotlin_builtins",
                "META-INF/"
            )

            archiveFileName = "cgs-${project.name}.jar"
        }

        withType<KotlinCompile> {
            kotlinOptions.javaParameters = true
            kotlinOptions.jvmTarget = "17"
        }

        withType<JavaCompile> {
            options.compilerArgs.add("-parameters")
            options.fork()
            options.encoding = "UTF-8"
        }
    }

    publishing {
        repositories.configureScalaRepository(
            dev = version.toString().endsWith("-dev")
        )

        publications {
            register(
                name = "mavenJava",
                type = MavenPublication::class,
                configurationAction = shadow::component
            )
        }
    }

    tasks.getByName("build")
        .dependsOn(
            "shadowJar",
            "publishMavenJavaPublicationToScalaRepository"
        )
}

fun RepositoryHandler.configureScalaRepository(dev: Boolean = false)
{
    maven("${property("artifactory_contextUrl")}/gradle-${if (dev) "dev" else "release"}") {
        name = "scala"
        credentials {
            username = property("artifactory_user").toString()
            password = property("artifactory_password").toString()
        }
    }
}