package gg.scala.cgs.game.commands

import org.bukkit.ChatColor
import org.bukkit.entity.Player
import gg.scala.cgs.game.ScGameFramework
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.Syntax
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import net.evilblock.cubed.util.CC

@AutoRegister
object MapVoteCommand : ScalaCommand() {

    @CommandAlias("vote")
    @Syntax("<map>")
    fun onMapVote(player: Player, name: String) {
        val game = ScGameFramework.instance.game

        if (game == null || !game.isVoteActive()) {
            player.sendMessage("${CC.RED}Map voting is currently closed.")
            return
        }

        val mapVote = game.mapVote

        if (mapVote.getVote(player) != null && mapVote.getVote(player)!!.name.equals(name, ignoreCase = true)) {
            player.sendMessage("${CC.RED}You have already voted for this map.")
            return
        }

        mapVote.remove(player)
        mapVote.vote(player, name)
    }

}