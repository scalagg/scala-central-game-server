package gg.scala.cgs.game.timer.event

import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import gg.scala.cgs.game.timer.PlayerTimer
import java.util.*

class TimerApplyEvent(val uuid: UUID, val timer: PlayerTimer, val duration: Long) : Event() {
    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }

    var cancelled = false

    override fun getHandlers(): HandlerList = handlerList
}

class TimerRemoveEvent(val uuid: UUID, val timer: PlayerTimer, val remainingTime: Long) : Event() {
    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }

    var cancelled = false

    override fun getHandlers(): HandlerList = handlerList
}

class TimerExpireEvent(val uuid: UUID, val timer: PlayerTimer) : Event() {
    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }

    override fun getHandlers(): HandlerList = handlerList
}

class TimerExtendEvent(val uuid: UUID, val timer: PlayerTimer, val oldTime: Long, val newTime: Long) : Event() {
    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }

    var cancelled = false

    override fun getHandlers(): HandlerList = handlerList
}
