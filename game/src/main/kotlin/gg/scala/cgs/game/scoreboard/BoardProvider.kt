package gg.scala.cgs.game.scoreboard

import org.bukkit.entity.Player
import gg.scala.cgs.game.game.GameStage
import java.util.LinkedList

open class BoardProvider(val forStage: GameStage) {
    open fun getLines(lines: LinkedList<String>, player: Player) {}
}