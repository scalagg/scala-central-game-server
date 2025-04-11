package gg.scala.cgs.spigot.commands

import gg.scala.basics.plugin.commands.admin.ShutdownCommand
import gg.scala.cgs.common.packet.CgsPacketService
import gg.scala.commons.acf.CommandHelp
import gg.scala.commons.acf.annotation.*
import gg.scala.commons.agnostic.sync.server.ServerContainer
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.time.Duration
import org.apache.commons.io.FileUtils
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * @author Topu
 * @date 11/16/2024
 */
@AutoRegister
@CommandAlias("cgs-admin")
@CommandPermission("cgs.admin")
object CgsAdminCommands : ScalaCommand() {

    @Default
    @HelpCommand
    fun onHelp(help: CommandHelp)
    {
        help.showHelp()
    }

    @Subcommand("requestserver")
    @Syntax("<serverGroup> <teamSize> <gameType> <serverName>")
    fun onRequestServer(sender: CommandSender, serverGroup: String, teamSize: Int, gameType: String, serverName: String) {
        val file = File("/home/templates/templates/$serverGroup")

        if (!file.exists()) {
            sender.sendMessage("${CC.RED}No such group or template available for \"$serverGroup\".")
            return
        }

        if (serverName != "§") {
            if (ServerContainer.getServer(serverName) != null) {
                sender.sendMessage("${CC.RED}A server with that name already exists.")
                return
            }
            if (serverName.lowercase().endsWith("-template")) {
                sender.sendMessage("${CC.RED}Cannot use this name. Use /edittemplate to modify a template.")
                return
            }
        }

        CgsPacketService.buildMessage(
            "request-server",
            mapOf(
                "serverGroup" to serverGroup,
                "teamSize" to teamSize.toString(),
                "gameType" to gameType,
                "serverName" to serverName
            )
        ).publish()

        sender.sendMessage("${CC.GREEN}Requested server with group: $serverGroup (teamSize: $teamSize, gameType: $gameType)")
    }

    @Subcommand("template edit")
    @Syntax("<serverGroup>")
    fun onTemplateEdit(sender: CommandSender, serverGroup: String) {
        val file = File("/home/templates/templates/$serverGroup")

        if (ServerContainer.getServersInGroup(serverGroup) != null || !file.exists()) {
            sender.sendMessage("${CC.RED}There is no such group or template available for the group \"$serverGroup\".")
            return
        }

        CgsPacketService.buildMessage(
            "request-server",
            mapOf(
                "serverGroup" to serverGroup,
                "serverName" to "$serverGroup-Template"
            )
        ).publish()

        sender.sendMessage("${CC.GREEN}We have requested a template edit server to be made with the group: $serverGroup.")
    }

    @Subcommand("template save")
    fun onTemplateSave(sender: CommandSender) {
        if (!Bukkit.getServerName().endsWith("-Template")) {
            sender.sendMessage("${CC.RED}This is not a template server.")
            return
        }

        val serverGroup = Bukkit.getServerName().split("-")[0]
        val file = File("/home/templates/templates/$serverGroup")
        if (!file.exists()) {
            sender.sendMessage("${CC.RED}ERROR! Failed to find a template group in the updater directory called: $serverGroup")
            sender.sendMessage("Updater directory: /home/templates/")
        }

        val runningDir = File("/home/templates/".replace("/plugins", ""))
        if (!runningDir.exists()) {
            sender.sendMessage("${CC.RED}ERROR! Failed to find running directory.")
            return
        }
        Bukkit.broadcastMessage("${CC.GREEN}SAVING TEMPLATE...")

        Bukkit.broadcastMessage("${CC.GREEN}REPLACING TEMPLATE CONFIG VALUES...")
        try {
            replace(File(runningDir, "server.properties"), "server-name=${Bukkit.getServerName()}", "server-name=template")
            replace(File(runningDir, "server.properties"), "server-port=${Bukkit.getPort()}", "server-port=25565")
            replace(File(runningDir, "plugins/scala-commons/serversync.yml"), "id: ${Bukkit.getServerName()}", "id: template")
            Bukkit.broadcastMessage("${CC.GREEN}REPLACED TEMPLATE CONFIG VALUES!")
        } catch (e: Exception) {
            Bukkit.broadcastMessage("${CC.RED}ERROR EDITING TEMPLATE CONFIG VALUES!")
            Bukkit.broadcastMessage(e.toString())
            e.printStackTrace()
        }

        try {
            FileUtils.copyDirectory(runningDir, file)

            listOf("logs", "plugins", "crash-reports").forEach { str ->
                val deleteFile = File(file, str)
                if (deleteFile.exists() && deleteFile.isDirectory) {
                    try {
                        FileUtils.deleteDirectory(deleteFile)
                        Bukkit.broadcastMessage("Deleted dir: ${deleteFile.absolutePath}")
                    } catch (e: IOException) {
                        Bukkit.broadcastMessage("Failed to delete dir: ${deleteFile.absolutePath}")
                        Bukkit.broadcastMessage(e.toString())
                        e.printStackTrace()
                    }
                }
            }

            Bukkit.broadcastMessage("${CC.GREEN}SUCCESSFULLY SAVED THE TEMPLATE...")
            Bukkit.broadcastMessage("${CC.GREEN}CLOSING THE SERVER...")

            ShutdownCommand.onInitiate(sender, Duration.parse("5s"))
        } catch (e: IOException) {
            Bukkit.broadcastMessage("${CC.RED}ERROR SAVING TEMPLATE!")
            Bukkit.broadcastMessage(e.toString())
            e.printStackTrace()
        }
    }

    @Subcommand("destroyserver")
    fun destroyServer(sender: CommandSender) {
        if (!"/home/templates/".contains("/temp/")) {
            sender.sendMessage("${CC.RED}This server is not a game server - you cannot destroy this.")
            return
        }

        sender.sendMessage("${CC.GREEN}Attempting to destroy this server...")
        ShutdownCommand.onInitiate(sender, Duration.parse("3s"))
    }

    fun replace(file: File, from: String, to: String) {
        val content = file.readText(Charsets.UTF_8)
        file.writeText(content.replace(from, to), Charsets.UTF_8)
    }
}