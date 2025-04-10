package gg.scala.cgs.game.commands

import org.bukkit.ChatColor
import org.bukkit.entity.Player
import gg.scala.cgs.game.game.GameStage
import gg.scala.cgs.game.ScGameFramework
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.Description
import gg.scala.commons.acf.annotation.Syntax
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand

@AutoRegister
object SpectatorCommands : ScalaCommand() {

    @CommandAlias("teleport|tp|goto|tpto")
    @Description("Teleport yourself to a player")
    @Syntax("<player>")
    fun onTeleport(sender: Player, target: Player) {
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

        sender.teleport(target)
        sender.sendMessage("${ChatColor.GOLD}Teleporting you to ${ChatColor.RESET}${target.displayName}${ChatColor.GOLD}.")
    }

}