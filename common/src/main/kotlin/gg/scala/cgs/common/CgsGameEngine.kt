package gg.scala.cgs.common

import gg.scala.cgs.common.game.GameType
import gg.scala.cgs.common.packet.CgsPacketService
import gg.scala.cgs.common.server.TempServer
import org.apache.commons.io.FileUtils
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.*

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

        /*@Synchronized
        @Throws(Exception::class)
        fun createServer(serverName: String?, group: String, teamSize: Int, gameType: String) {
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
                    gameTypeValue = data["gameType"] as String
                }

                replace(File(serverFiles, "plugins/cgs-spigot/config.yml"), "gameType: $gameTypeValue", "gameType: $gameType")
            }

            setPermissions(targetDir)
            startServer(targetDir)

            TEMP_SERVERS.add(TempServer(serverName, group, port, targetDir, System.currentTimeMillis()))
        }*/

        @Throws(java.lang.Exception::class)
        fun createServer(serverName: String, group: String, teamSize: Int, gameType: String) {
            println("test2: createServer")
            var gameTypeEnum = GameType.getGameByDisplayName(group)
            if (gameTypeEnum == null) gameTypeEnum = GameType.valueOf(group.uppercase(Locale.getDefault()))

            val templateDir: File = File(templatePath + group)
            require(!(!templateDir.exists() || !templateDir.isDirectory)) { "No template found for group '$group'." }


            val targetDir: File = File(dockerTemp + serverName)
            if (targetDir.exists()) FileUtils.deleteDirectory(targetDir)

            // find port
            val port: Int = findFreePort()

            // copy docker setup
            FileUtils.copyDirectory(File(dockerBase), targetDir)
            replace(File(targetDir, "deploy.sh"), "port=25565", "port=$port")

            // change path
            replace(
                File(targetDir, "deploy.sh"),
                "path=/home/templates/mcdeploy",
                "path=" + targetDir.absolutePath
            )
            replace(
                File(targetDir, "startup.sh"),
                "path=/home/templates/mcdeploy",
                "path=" + targetDir.absolutePath
            )
            replace(
                File(targetDir, "down.sh"),
                "path=/home/templates/mcdeploy",
                "path=" + targetDir.absolutePath
            )

            // change server name
            replace(
                File(targetDir, "deploy.sh"), "servername=CGSServer",
                "servername=$serverName"
            )
            replace(
                File(targetDir, "startup.sh"), "servername=CGSServer",
                "servername=$serverName"
            )
            replace(
                File(targetDir, "down.sh"), "servername=CGSServer",
                "servername=$serverName"
            )

            // copy template
            val serverFiles = File(targetDir, "serverfiles")
            FileUtils.copyDirectory(templateDir, serverFiles)


            // replace variables
            replace(
                File(serverFiles, "server.properties"), "server-name=template",
                "server-name=$serverName"
            )
            replace(
                File(serverFiles, "server.properties"), "server-port=25565",
                "server-port=$port"
            )
            replace(
                File(serverFiles, "plugins/Bridge/config.yml"), "servername: template",
                "servername: $serverName"
            )

            var teamSizeValue = 1
            var gameTypeValue: String? = "Regular"

            val pluginDir = File(templateDir, "/plugins/" + gameTypeEnum.pluginName)
            if (pluginDir.exists()) {
                val groupConfig = File(pluginDir, "/config.yml")
                if (groupConfig.exists()) {
                    val inputStream: InputStream = FileInputStream(groupConfig)
                    val yaml = Yaml()
                    val data = yaml.load<Any>(inputStream) as Map<*, *>

                    teamSizeValue = data["teamSize"] as Int
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

                    gameTypeValue = data["gameType"] as String
                }

                replace(File(serverFiles, "plugins/cgs-spigot/config.yml"), "gameType: $gameTypeValue", "gameType: $gameType")
            }
            

            // fix perms
            ProcessBuilder("chown", "-R", "scala:scala", serverFiles.absolutePath).start()
            ProcessBuilder("chmod", "-R", "0755", serverFiles.absolutePath).start()
            ProcessBuilder("chmod", "+x", File(serverFiles, "start.sh").absolutePath).start()
            ProcessBuilder("chmod", "+x", File(targetDir, "deploy.sh").absolutePath).start()
            ProcessBuilder("chmod", "+x", File(targetDir, "down.sh").absolutePath).start()
            ProcessBuilder("chmod", "+x", File(targetDir, "startup.sh").absolutePath).start()

            // start server
            ProcessBuilder("./deploy.sh")
                .directory(targetDir)
                .start()

            println("Started server '$serverName' via Docker!")

            /*TEMP_SERVERS.add(
                TempServer(
                    serverName,
                    group,
                    port,
                    targetDir,
                    System.currentTimeMillis()
                )
            )*/
            //CgsPacketService.buildMessage("velocity-server", mapOf("serverName" to serverName, "address" to "127.0.0.1", "port" to port.toString())).publish()
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