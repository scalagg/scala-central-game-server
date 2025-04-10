package gg.scala.cgs.server

import java.io.File

object StartCGS {

    @JvmStatic
    fun main(args: Array<String>) {
        System.setProperty("jdk.lang.Process.allowAmbiguousCommands", "true")
        System.setProperty("jdk.util.jar.enableMultiRelease", "force")
        val cgsServer = CGSServer()
        cgsServer.init()
        println("[CGS] Starting CGS Server...")
        cgsServer.start()
    }
}
