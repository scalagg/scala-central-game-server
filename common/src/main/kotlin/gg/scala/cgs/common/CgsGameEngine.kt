package gg.scala.cgs.common

import gg.scala.cgs.common.game.GameType
import gg.scala.cgs.common.packet.CgsPacketService
import gg.scala.cgs.common.server.TempServer
import gg.scala.cgs.common.util.SystemType
import org.apache.commons.io.FileUtils
import org.yaml.snakeyaml.Yaml
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.*
import java.util.function.Consumer

/**
 * @author Topu
 * @date 11/16/2024
 */
class CgsGameEngine {

    companion object {
        private val templatePath = "/opt/event-templates/templates/"
        private val dockerBase = "/opt/event-templates/docker_instances/base"
        private val dockerTemp = "/opt/event-templates/docker_instances/temp/"
        var systemType: SystemType = SystemType.UNKNOWN

        fun initPacketHandler() {
            systemType = currentSystemType()

            CgsPacketService.init(
                JedisPool(
                    JedisPoolConfig(),
                    "38.114.240.186",
                    4322,
                    2000,
                    "59U20W3h6PwNidHcjf2F11oZjgDpHH1AvAKw5",
                    0,
                ), "cgs-packets"
            )
        }

        fun currentSystemType(): SystemType {
            try {
                Class.forName("com.velocitypowered.api.proxy.ProxyServer")
                return SystemType.VELOCITY
            } catch (ignored: ClassNotFoundException) {
            }
            try {
                Class.forName("org.bukkit.Bukkit")
                return SystemType.SPIGOT
            } catch (ignored: ClassNotFoundException) {
            }
            return SystemType.UNKNOWN
        }

        val TEMP_SERVERS: MutableList<TempServer> = mutableListOf()

        fun findAvailableServerId(group: String): Int {
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

        fun getTemplates() : List<String> {
            val directory = File(templatePath)
            val folderNames = mutableListOf<String>()
            directory.listFiles()
                ?.filter { it.isDirectory }
                ?.forEach { folderNames.add(it.name) }

            return folderNames
        }

        @Throws(Exception::class)
        fun createServer(serverName: String?, group: String, teamSize: Int, gameType: String) {
            createServer(serverName, group, teamSize, gameType, null)
        }

        @Throws(Exception::class)
        fun createServer(serverName: String?, group: String, teamSize: Int, gameType: String, consumer: Consumer<String>?) {
            if (serverName == null) throw IllegalArgumentException("inputServerName cannot be null")

            println("[DEBUG] Starting server creation for $serverName...")

            val gameTypeEnum = GameType.getGameByDisplayName(group) ?: GameType.valueOf(group.uppercase())
            val templateDir = File("$templatePath$group")
            if (!templateDir.exists() || !templateDir.isDirectory)
                throw IllegalArgumentException("No template found for group '$group'.")

            val targetDir = File("$dockerTemp$serverName")
            if (targetDir.exists()) FileUtils.deleteDirectory(targetDir)

            val port = findFreePort()

            val serverFiles = File(targetDir, "serverfiles")
            println("Copying template from ${templateDir.absolutePath} to ${serverFiles.absolutePath}")
            safeCopyDirectory(templateDir, serverFiles)

            FileUtils.copyDirectory(File(dockerBase), targetDir)
            replace(File(targetDir, "deploy.sh"), "port=25565", "port=$port")
            replace(File(targetDir, "deploy.sh"), "path=/home/templates/mcdeploy", "path=${targetDir.absolutePath}")
            replace(File(targetDir, "startup.sh"), "path=/home/templates/mcdeploy", "path=${targetDir.absolutePath}")
            replace(File(targetDir, "down.sh"), "path=/home/templates/mcdeploy", "path=${targetDir.absolutePath}")
            replace(File(targetDir, "deploy.sh"), "servername=CGSServer", "servername=$serverName")
            replace(File(targetDir, "startup.sh"), "servername=CGSServer", "servername=$serverName")
            replace(File(targetDir, "down.sh"), "servername=CGSServer", "servername=$serverName")
            replace(File(serverFiles, "server.properties"), "server-name=template", "server-name=$serverName")
            replace(File(serverFiles, "server.properties"), "server-port=25565", "server-port=$port")
            replace(File(serverFiles, "plugins/scala-commons/serversync.yml"), "server-id: template", "server-id: $serverName")
            replace(File(serverFiles, "plugins/scala-commons/serversync.yml"), "advertisedServerAddress: 127.0.0.1:25565", "advertisedServerAddress: 127.0.0.1:$port")

            var teamSizeValue = 1
            var gameTypeValue = "Regular"

            val pluginDir = File(templateDir, "/plugins/" + gameTypeEnum.pluginName)
            if (pluginDir.exists()) {
                val groupConfig = File(pluginDir, "/config.yml")
                if (groupConfig.exists()) {
                    val inputStream: InputStream = FileInputStream(groupConfig)
                    val yaml = Yaml()
                    val data = yaml.load<Any>(inputStream) as Map<*, *>
                    teamSizeValue = data["teamSize"] as? Int ?: teamSizeValue
                }


                replace(File(serverFiles, "plugins/" + gameTypeEnum.pluginName + "/config.yml"), "teamSize: $teamSizeValue", "teamSize: $teamSize")
            }

            val spigotPluginDir = File(templateDir, "/plugins/cgs-spigot")
            if (spigotPluginDir.exists()) {
                val spigotPluginConfig = File(spigotPluginDir, "/config.yml")
                if (spigotPluginConfig.exists()) {
                    val inputStream: InputStream = FileInputStream(spigotPluginConfig)
                    val yaml = Yaml()
                    val data = yaml.load<Any>(inputStream) as Map<*, *>
                    gameTypeValue = data["game-type"] as? String ?: gameTypeValue
                }

                replace(File(serverFiles, "plugins/cgs-spigot/config.yml"), "game-type: $gameTypeValue", "game-type: $gameType")
            }

            println("[DEBUG] Setting permissions for $serverName...")
            setPermissions(targetDir)
            println("[DEBUG] Starting server $serverName...")

            // fix perms
            ProcessBuilder("chown", "-R", "root:root", serverFiles.absolutePath).start()
            ProcessBuilder("chmod", "-R", "0755", serverFiles.absolutePath).start()
            ProcessBuilder("chmod", "+x", File(serverFiles, "start.sh").absolutePath).start()
            ProcessBuilder("chmod", "+x", File(targetDir, "deploy.sh").absolutePath).start()
            ProcessBuilder("chmod", "+x", File(targetDir, "down.sh").absolutePath).start()
            ProcessBuilder("chmod", "+x", File(targetDir, "startup.sh").absolutePath).start()

            println(targetDir)

            ProcessBuilder("./deploy.sh")
                .directory(targetDir)
                .start()

            println("[CGS] Started server '$serverName' via Docker!")

            TEMP_SERVERS.add(TempServer(serverName, group, port, targetDir, System.currentTimeMillis()))

            consumer?.accept(serverName)
        }

        private fun replace(file: File, from: String, to: String) {
            val content = String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8)
            Files.write(file.toPath(), content.replace(from, to).toByteArray(StandardCharsets.UTF_8))
        }

        private fun findFreePort(): Int {
            return ServerSocket(0).use { it.localPort }
        }

        private fun setPermissions(targetDir: File) {
            listOf("chown", "-R", "root:root", targetDir.absolutePath).runCommand()
            listOf("chmod", "-R", "0755", targetDir.absolutePath).runCommand()
            listOf("chmod", "+x", "${targetDir.absolutePath}/deploy.sh").runCommand()
            listOf("chmod", "+x", "${targetDir.absolutePath}/startup.sh").runCommand()
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

        private fun safeCopyDirectory(src: File, dest: File) {
            if (!src.exists()) throw IOException("Source directory doesn't exist: ${src.absolutePath}")
            if (!src.isDirectory) throw IOException("Source is not a directory: ${src.absolutePath}")
            try {
                FileUtils.copyDirectory(src, dest)
            } catch (e: IOException) {
                println("Error copying directory from ${src.absolutePath} to ${dest.absolutePath}")
                throw e
            }
        }
    }
}