package gg.scala.cgs.game.commands

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import gg.scala.cgs.game.game.GameStage
import gg.scala.cgs.game.ScGameFramework
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.Description
import gg.scala.commons.acf.annotation.Syntax
import gg.scala.commons.agnostic.sync.ServerSync
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import net.evilblock.cubed.util.bungee.BungeeUtil
import java.util.*

@AutoRegister
object SpecCommand : ScalaCommand() {

    @CommandAlias("spec|spectate")
    @Description("Spectate a player")
    @Syntax("<player>")
    fun onSpectate(sender: Player, uuid: UUID) {
        val game = ScGameFramework.instance.game

        if (game == null || game.gameStage.ordinal < GameStage.IN_GAME.ordinal) {
            if (!sender.hasPermission("basic.teleport")) {
                sender.sendMessage("${ChatColor.RED}No permission.")
                return
            }
        } else {
            val gamePlayer = game.getGamePlayer(sender)

            if (gamePlayer == null || (!gamePlayer.spectator && !sender.hasPermission("basic.teleport"))) {
                sender.sendMessage("${ChatColor.RED}You can only teleport to players as a spectator.")
                return
            }
        }

        val foundServer = ServerSync.getLocalGameServer()
        if(!foundServer.getPlayers()!!.contains(uuid)) {
            sender.sendMessage("${ChatColor.RED}That player is not online.")
            return
        }

//        if(foundServer != null || foundServer.metadata == null || !foundServer.metadata.contains("game")) {
//            sender.sendMessage("${ChatColor.RED}That player isn't playing a minigame.")
//            return
//        }

        if(Bukkit.getPlayer(uuid) != null) {
            SpectatorCommands.onTeleport(sender, Bukkit.getPlayer(uuid)!!)
            return
        }
        sender.sendMessage("${ChatColor.GOLD}Attempting to teleport you...")

        BungeeUtil.sendToServer(sender, foundServer.id)
    }

}