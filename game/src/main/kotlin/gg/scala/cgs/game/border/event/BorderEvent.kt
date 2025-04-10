package gg.scala.cgs.game.border.event

import org.bukkit.event.Event
import gg.scala.cgs.game.border.Border
import org.bukkit.event.HandlerList

/**
 * @author Topu
 * @date 1/4/2025
 */
open class BorderEvent(val border: Border) : Event() {

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }

    override fun getHandlers(): HandlerList {
        return handlerList
    }
}
