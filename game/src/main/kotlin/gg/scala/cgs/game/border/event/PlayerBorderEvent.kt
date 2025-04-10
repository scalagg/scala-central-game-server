package gg.scala.cgs.game.border.event

import org.bukkit.event.Cancellable
import gg.scala.cgs.game.border.Border
import org.bukkit.entity.Player

/**
 * @author Topu
 * @date 1/4/2025
 */
open class PlayerBorderEvent(border: Border, val player: Player) : BorderEvent(border), Cancellable {

    private var cancelled = false

    override fun isCancelled(): Boolean {
        return cancelled
    }

    override fun setCancelled(cancel: Boolean) {
        cancelled = cancel
    }
}


