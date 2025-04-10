package gg.scala.cgs.game.timer

import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import gg.scala.cgs.game.ScGameFramework
import gg.scala.cgs.game.timer.event.TimerApplyEvent
import gg.scala.cgs.game.timer.event.TimerExpireEvent
import gg.scala.cgs.game.timer.event.TimerExtendEvent
import gg.scala.cgs.game.timer.event.TimerRemoveEvent
import gg.scala.cgs.game.utils.Timespan
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

open class PlayerTimer(name: String, displayName: String, hidden: Boolean, val defaultLength: Timespan) : Timer(name, displayName, hidden) {

    private val timerMap: MutableMap<UUID, Long> = ConcurrentHashMap()
    private val timerRunnableMap: MutableMap<UUID, BukkitTask> = ConcurrentHashMap()

    fun getRemaining(uuid: UUID, unit: TimeUnit = TimeUnit.MILLISECONDS): Long {
        val msLeft = if (timerMap.containsKey(uuid)) (timerMap[uuid]!! - System.currentTimeMillis()) else 0L

        return when (unit) {
            TimeUnit.NANOSECONDS -> TimeUnit.MILLISECONDS.toNanos(msLeft)
            TimeUnit.MICROSECONDS -> TimeUnit.MILLISECONDS.toMicros(msLeft)
            TimeUnit.MILLISECONDS -> msLeft
            TimeUnit.SECONDS -> TimeUnit.MILLISECONDS.toSeconds(msLeft)
            TimeUnit.MINUTES -> TimeUnit.MILLISECONDS.toMinutes(msLeft)
            TimeUnit.HOURS -> TimeUnit.MILLISECONDS.toHours(msLeft)
            TimeUnit.DAYS -> TimeUnit.MILLISECONDS.toDays(msLeft)
        }
    }

    fun clear() {
        timerMap.clear()
        timerRunnableMap.values.forEach { it.cancel() }
        timerRunnableMap.clear()
    }

    fun isActive(uuid: UUID) = getRemaining(uuid) > 0L

    fun remove(uuid: UUID) {
        if (!isActive(uuid)) return
        val msLeft = getRemaining(uuid)

        val event = TimerRemoveEvent(uuid, this, msLeft)
        Bukkit.getServer().pluginManager.callEvent(event)

        if (!event.cancelled) {
            timerMap.remove(uuid)
            timerRunnableMap[uuid]?.cancel()
            timerRunnableMap.remove(uuid)
        }
    }

    fun apply(uuid: UUID, override: Boolean = false, duration: Timespan = defaultLength) {
        if (isActive(uuid)) {
            if (!override) return
            val msLeft = getRemaining(uuid)

            val event = TimerExtendEvent(uuid, this, msLeft, duration.toMillis())
            Bukkit.getServer().pluginManager.callEvent(event)

            if (!event.cancelled) {
                timerMap[uuid] = System.currentTimeMillis() + duration.toMillis()
                timerRunnableMap[uuid]?.cancel()
                timerRunnableMap[uuid] =
                    Bukkit.getScheduler().runTaskLater(ScGameFramework.instance, TimerExpireTask(uuid, this), duration.toSeconds() * 20)
            }
            return
        }

        val event = TimerApplyEvent(uuid, this, duration.toMillis())
        Bukkit.getServer().pluginManager.callEvent(event)

        if (!event.cancelled) {
            timerMap[uuid] = System.currentTimeMillis() + duration.toMillis()
            timerRunnableMap[uuid]?.cancel()
            timerRunnableMap[uuid] =
                Bukkit.getScheduler().runTaskLater(ScGameFramework.instance, TimerExpireTask(uuid, this), duration.toSeconds() * 20)
        }
    }

}

class TimerExpireTask(private val uuid: UUID, private val timer: PlayerTimer) : BukkitRunnable() {

    override fun run() {
        val event = TimerExpireEvent(uuid, timer)
        Bukkit.getServer().pluginManager.callEvent(event)
    }

}