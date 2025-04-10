package gg.scala.cgs.game.scoreboard

import org.bukkit.entity.Player
import gg.scala.cgs.game.ScGameFramework
import net.evilblock.cubed.scoreboard.ScoreboardAdapter
import net.evilblock.cubed.scoreboard.ScoreboardAdapterRegister
import java.util.*

class FrameworkBoard : ScoreboardAdapter() {

    override fun getTitle(player: Player): String
    {
        return ScGameFramework.instance.game?.boardSettings?.title ?: "Not setup"
    }

    override fun getLines(board: LinkedList<String>, player: Player) {
        val lines = LinkedList<String>()
        val game = ScGameFramework.instance.game ?: return
        val provider = game.scoreboards[game.gameStage] ?: return
        provider.getLines(lines, player)
    }

}