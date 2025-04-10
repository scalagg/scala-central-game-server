package gg.scala.cgs.game.border

import gg.scala.cgs.game.border.actions.BorderActions
import gg.scala.cgs.game.border.event.BorderChangeEvent
import gg.scala.cgs.game.border.event.PlayerEnterBorderEvent
import gg.scala.cgs.game.border.event.PlayerExitBorderEvent

/**
 * @author Topu
 * @date 1/4/2025
 */
class BorderConfig {

    companion object {
        val DEFAULT_CONFIGURATION = BorderConfig()
    }

    val defaultBorderChangeActions: MutableSet<(BorderChangeEvent) -> Unit> = mutableSetOf()
    val defaultBorderEnterActions: MutableSet<(PlayerEnterBorderEvent) -> Unit> = mutableSetOf()
    val defaultBorderExitActions: MutableSet<(PlayerExitBorderEvent) -> Unit> = mutableSetOf()

    init {
        defaultBorderChangeActions.add(BorderActions.ENSURE_PLAYERS_IN_BORDER)
        defaultBorderExitActions.add(BorderActions.PUSHBACK_ON_EXIT)
        defaultBorderExitActions.add(BorderActions.CANCEL_EXIT)
    }
}

