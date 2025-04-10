package gg.scala.cgs.game.scoreboard

import org.bukkit.entity.Player
import gg.scala.cgs.game.game.GameStage
import java.util.LinkedList
import java.util.function.BiConsumer

class BoardBuilder {
    val providers: MutableList<BoardProvider> = ArrayList()

    fun forStage(stage: GameStage, provider: BiConsumer<MutableList<String>, Player>): BoardBuilder { // Java
        providers += SingleBoardBuilder(stage).setLines(provider).create()
        return this
    }
}

class SingleBoardBuilder(val stage: GameStage) {
    private lateinit var boardProvider: BoardProvider

    fun setLines(provider: BiConsumer<MutableList<String>, Player>): SingleBoardBuilder { // Java
        boardProvider = object : BoardProvider(stage) {
            override fun getLines(lines: LinkedList<String>, player: Player) {
                provider.accept(lines, player)
            }
        }

        return this
    }

    fun create() = boardProvider

}