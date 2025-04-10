package gg.scala.cgs.common.server

import gg.scala.aware.thread.AwareThreadContext
import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.packet.CgsPacketService
import java.io.File
import java.io.IOException
import java.nio.file.Files

/**
 * @author Topu
 * @date 11/16/2024
 */
data class TempServer(
    val name: String,
    val group: String,
    val port: Int,
    val directory: File,
    val startedAt: Long,
    var removedTime: Long = startedAt,
    var lastText: String = ""
) {

    fun shouldRemove(): Boolean {
        return (System.currentTimeMillis() - removedTime) > 15_000L && !isRunning()
    }

    fun isRunning(): Boolean {
        return !CgsGameEngine.available(port)
    }

    @Throws(Exception::class)
    fun destroy() {
        CgsPacketService.buildMessage(
            "velocity-server",
            mapOf("serverName" to name)
        ).publish(AwareThreadContext.SYNC)

        ProcessBuilder("sudo", "chown", "scala:scala", "serverfiles")
            .directory(directory)
            .start()

        ProcessBuilder("./down.sh")
            .directory(directory)
            .start()

        Files.walk(directory.toPath()).sorted(Comparator.reverseOrder()).forEach {
            Files.delete(it)
        }
    }

    @Throws(IOException::class)
    fun getLastStringFromDocker(): String {
        val command = "docker logs -f --tail 2 $name"
        val process = Runtime.getRuntime().exec(command)
        return process.inputStream.bufferedReader().use { it.readText() }
    }
}
