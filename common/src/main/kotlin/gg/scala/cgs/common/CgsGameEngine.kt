package gg.scala.cgs.common

import gg.scala.aware.Aware
import gg.scala.aware.message.AwareMessage
import gg.scala.aware.thread.AwareThreadContext
import gg.scala.cgs.common.game.GameType
import gg.scala.cgs.common.packet.CgsPacketService
import gg.scala.cgs.common.server.TempServer
import gg.scala.cloudsync.shared.connection.CloudSyncConnector
import org.apache.commons.io.FileUtils
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import kotlin.properties.Delegates

/**
 * @author Topu
 * @date 11/16/2024
 */
class CgsGameEngine {

    companion object {
        private val templatePath = "/home/templates/templates/"
        private val dockerBase = "/home/templates/docker_instances/base"
        private val dockerTemp = "/home/templates/docker_instances/temp/"

        val TEMP_SERVERS: MutableList<TempServer> = mutableListOf()

        fun findAvailableServerId(group: String): Int {
            // Logic for finding the available server ID, adapted to Kotlin
            val filteredCount = TEMP_SERVERS.stream()
                .filter { it.group.equals(group, ignoreCase = true) }
                .count() + 1

            for (i in 0 until filteredCount) {
                val name = "$group-${i + 1}"
                if (TEMP_SERVERS.stream().noneMatch { it.name.equals(name, ignoreCase = true) }) {
                    return (i + 1).toInt()
                }
            }

            return -1
        }

        fun doesTemplateExist(group: String): Boolean {
            val templateDir = File("$templatePath$group")
            return templateDir.exists() && templateDir.isDirectory
        }

        @Throws(Exception::class)
        fun createServer(serverName: String?, group: String, teamSize: Int, gameType: String) {
            if (serverName == null) throw IllegalArgumentException("inputServerName cannot be null")

            val gameTypeEnum = GameType.getGameByDisplayName(group) ?: GameType.valueOf(group.uppercase())
            val templateDir = File("$templatePath$group")
            if (!templateDir.exists() || !templateDir.isDirectory)
                throw IllegalArgumentException("No template found for group '$group'.")

            val targetDir = File("$dockerTemp$serverName")
            if (targetDir.exists()) FileUtils.deleteDirectory(targetDir)

            // Find available port
            val port = findFreePort()

            // Copy docker setup
            FileUtils.copyDirectory(File(dockerBase), targetDir)
            replace(File(targetDir, "deploy.sh"), "port=25565", "port=$port")
            replace(File(targetDir, "deploy.sh"), "path=/home/templates/mcdeploy", "path=${targetDir.absolutePath}")
            replace(File(targetDir, "startup.sh"), "path=/home/templates/mcdeploy", "path=${targetDir.absolutePath}")
            replace(File(targetDir, "down.sh"), "path=/home/templates/mcdeploy", "path=${targetDir.absolutePath}")

            replace(File(targetDir, "deploy.sh"), "servername=CGSServer", "servername=$serverName")
            replace(File(targetDir, "startup.sh"), "servername=CGSServer", "servername=$serverName")
            replace(File(targetDir, "down.sh"), "servername=CGSServer", "servername=$serverName")

            // Copy template
            val serverFiles = File(targetDir, "serverfiles")
            FileUtils.copyDirectory(templateDir, serverFiles)

            replace(File(serverFiles, "server.properties"), "server-name=template", "server-name=$serverName")
            replace(File(serverFiles, "server.properties"), "server-port=25565", "server-port=$port")
            replace(File(serverFiles, "plugins/scala-commons/serversync.yml"), "id: template", "id: $serverName")

            var teamSizeValue = 1
            var gameTypeValue = "Regular"

            val pluginDir = File(templateDir, "/plugins/" + gameTypeEnum.pluginName)
            if (pluginDir.exists()) {
                val groupConfig = File(pluginDir, "/config.yml")
                if (groupConfig.exists()) {
                    val inputStream: InputStream = FileInputStream(groupConfig)
                    val yaml = Yaml()
                    val data = yaml.load<Any>(inputStream) as Map<*, *>
                    teamSizeValue = data["teamSize"] as Int
                    gameTypeValue = (data["gameType"] as String?)!!
                }

                replace(File(serverFiles, "plugins/" + gameTypeEnum.pluginName + "/config.yml"), "teamSize: $teamSizeValue", "teamSize: $teamSize")
                replace(File(serverFiles, "plugins/" + gameTypeEnum.pluginName + "/config.yml"), "gameType: $gameTypeValue", "gameType: $gameType")
            }

            setPermissions(targetDir)
            startServer(targetDir)

            TEMP_SERVERS.add(TempServer(serverName, group, port, targetDir, System.currentTimeMillis()))

            CgsPacketService.buildMessage(
                "velocity-server",
                mapOf(
                    "serverName" to serverName,
                    "serverHost" to "127.0.0.1",
                    "port" to port.toString()
                )
            ).publish(AwareThreadContext.SYNC)
        }

        private fun replace(file: File, from: String, to: String) {
            val content = String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8)
            Files.write(file.toPath(), content.replace(from, to).toByteArray(StandardCharsets.UTF_8))
        }

        private fun findFreePort(): Int {
            return ServerSocket(0).use { it.localPort }
        }

        private fun setPermissions(targetDir: File) {
            listOf("chown", "-R", "scala:scala", targetDir.absolutePath).runCommand()
            listOf("chmod", "-R", "0755", targetDir.absolutePath).runCommand()
            listOf("chmod", "+x", "${targetDir.absolutePath}/deploy.sh").runCommand()
            listOf("chmod", "+x", "${targetDir.absolutePath}/startup.sh").runCommand()
        }

        private fun startServer(targetDir: File) {
            ProcessBuilder("./deploy.sh")
                .directory(targetDir)
                .start()
        }

        private fun List<String>.runCommand() {
            ProcessBuilder(*this.toTypedArray()).start()
        }

        fun available(port: Int): Boolean {
            return try {
                Socket("localhost", port).use { false }
            } catch (e: Exception) {
                true
            }
        }
    }
}
