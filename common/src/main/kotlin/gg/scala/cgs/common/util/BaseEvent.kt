package gg.scala.cgs.common.util

import org.bukkit.Bukkit
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * @author Topu
 * @date 11/17/2024
 */
open class BaseEvent : Event() {
    override fun getHandlers(): HandlerList {
        return handlerList
    }

    fun call() {
        Bukkit.getServer().pluginManager.callEvent(this)
    }

    companion object {
        val handlerList: HandlerList = HandlerList()
    }
}