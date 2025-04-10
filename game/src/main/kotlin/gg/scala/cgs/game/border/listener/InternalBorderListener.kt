package gg.scala.cgs.game.border.listener

import gg.scala.cgs.game.border.event.BorderChangeEvent
import gg.scala.cgs.game.border.event.PlayerEnterBorderEvent
import gg.scala.cgs.game.border.event.PlayerExitBorderEvent
import gg.scala.commons.annotations.Listeners
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

/**
 * @author Topu
 * @date 1/4/2025
 */
@Listeners
object InternalBorderListener : Listener {

    @EventHandler
    fun onBorderChange(event: BorderChangeEvent) {
        event.border.borderConfig.defaultBorderChangeActions.forEach { action ->
            action(event)
        }
    }

    @EventHandler
    fun onBorderExit(event: PlayerExitBorderEvent) {
        event.border.borderConfig.defaultBorderExitActions.forEach { action ->
            action(event)
        }
    }

    @EventHandler
    fun onBorderEnter(event: PlayerEnterBorderEvent) {
        event.border.borderConfig.defaultBorderEnterActions.forEach { action ->
            action(event)
        }
    }
}


