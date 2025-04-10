package gg.scala.cgs.game.listeners

import gg.scala.cgs.game.ScGameFramework
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import gg.scala.cgs.game.game.GameRule
import gg.scala.cgs.game.game.GameStage
import gg.scala.commons.annotations.Listeners
import java.util.concurrent.ThreadLocalRandom

@Listeners
object PreGameListener : Listener {

    @EventHandler
    fun onFood(event: FoodLevelChangeEvent) {
        if (event.entity !is Player) return
        val game = ScGameFramework.instance.game ?: return

        if(game.getGameRule(GameRule.DISABLE_PRE_GAME_LISTENER)) return

        val player = event.entity as Player
        if (event.foodLevel > player.foodLevel) return

        when (game.gameStage) {
            GameStage.IN_GAME -> {
                event.isCancelled = ThreadLocalRandom.current().nextInt(100) <= 70
            }
            else -> {
                event.foodLevel = 20
            }
        }
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        val game = ScGameFramework.instance.game ?: return
        if(game.getGameRule(GameRule.DISABLE_PRE_GAME_LISTENER)) return

        val player = event.player

        if (game.gameStage == GameStage.IN_GAME) return
        if (player.gameMode != GameMode.CREATIVE || !player.hasMetadata("build"))
            event.isCancelled = true
    }

    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        val game = ScGameFramework.instance.game ?: return
        if(game.getGameRule(GameRule.DISABLE_PRE_GAME_LISTENER)) return

        val player = event.player

        if (game.gameStage == GameStage.IN_GAME) return
        if (player.gameMode != GameMode.CREATIVE || !player.hasMetadata("build"))
            event.isCancelled = true
    }

}