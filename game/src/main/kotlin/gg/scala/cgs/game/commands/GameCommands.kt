package gg.scala.cgs.game.commands

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import gg.scala.cgs.game.game.GameStage
import gg.scala.cgs.game.ScGameFramework
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandPermission
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import net.evilblock.cubed.util.CC

@AutoRegister
object GameCommands : ScalaCommand() {

    @CommandAlias("forcestart")
    @CommandPermission("cgs.force-start")
    fun onForceStart(player: Player) {
        val game = ScGameFramework.instance.game

        if (game == null) {
            player.sendMessage("${CC.RED}No game is running!")
            return
        }

        when (game.gameStage) {
            GameStage.PRE_GAME -> {
                player.sendMessage("${CC.GREEN}Voting forcefully started.")
                game.startVoting()
            }

            GameStage.VOTING -> {
                player.sendMessage("${CC.GREEN}Countdown forcefully started.")
                game.selectMap()
            }

            GameStage.STARTING -> {
                player.sendMessage("${CC.GREEN}Game forcefully started.")
                game.startGame()
            }

            else -> player.sendMessage("${CC.RED}You can't do this right now.")
        }
    }

    @CommandAlias("pausetimer|pausematchtimer|pausegame|pausegametimer")
    @CommandPermission("cgs.manage-game")
    fun onPauseTimer(commandSender: CommandSender) {
        val game = ScGameFramework.instance.game

        if (game == null) {
            commandSender.sendMessage("${CC.RED}No game is running!")
            return
        }

        game.timerPaused = !game.timerPaused
        Bukkit.broadcastMessage("${CC.YELLOW}The game timer has been ${if (game.timerPaused) "${CC.RED}paused" else "${CC.GREEN}resumed"}${CC.YELLOW}.")
    }

}