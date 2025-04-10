package gg.scala.cgs.game.timer

import org.bukkit.event.Listener
import gg.scala.cgs.game.ScGameFramework

@Suppress("UNCHECKED_CAST")
object TimerHandler {

    public val playerTimerMap: MutableMap<String, PlayerTimer> = LinkedHashMap()

    fun <T : PlayerTimer> getPlayerTimer(name: String): T? = playerTimerMap[name.toLowerCase()] as T?

    fun <T> getPlayerTimer(clazz: Class<T>): T? = playerTimerMap.values.filter { it.javaClass == clazz }.map { it as T }.getOrNull(0)

    fun registerPlayerTimer(timer: PlayerTimer) {
        playerTimerMap[timer.name.toLowerCase()] = timer
        if (timer is Listener) ScGameFramework.instance.server.pluginManager.registerEvents(timer, ScGameFramework.instance)
    }

}