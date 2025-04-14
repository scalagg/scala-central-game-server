package gg.scala.cgs.game.border.runnable

import gg.scala.cgs.game.ScGameFramework
import gg.scala.cgs.game.border.Border
import gg.scala.cgs.game.border.event.BorderChangeEvent
import net.evilblock.cubed.util.bukkit.cuboid.Cuboid
import java.util.concurrent.TimeUnit

import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable

/**
 * @author Topu
 * @date 1/4/2025
 */
class BorderTask(private val border: Border) : BukkitRunnable() {

    var borderChange = 5
    var borderChangeDelay = 10L
    var action = BorderAction.NONE
    private var tracker = 0

    init {
        runTaskTimerAsynchronously(ScGameFramework.instance, 1L, 1L)
    }

    override fun run() {
        if (action != BorderAction.NONE) {
            tracker++
        }
        if (border != null && tracker % 20 == 0 && (tracker / 20) % borderChangeDelay == 0L) {
            val previous: Cuboid
            val previousSize = border.size
            previous = when (action) {
                BorderAction.SHRINK -> border.contract(borderChange)
                BorderAction.GROW -> border.expand(borderChange)
                else -> return
            }
            Bukkit.getPluginManager().callEvent(BorderChangeEvent(border, previousSize, previous, action))
            border.fill()
        }
    }

    fun start() {
        // Functionality is handled in the constructor with the asynchronous runnable
    }

    fun setAction(action: BorderAction): BorderTask {
        this.action = action
        tracker = 0
        return this
    }

    fun setBorderChangeDelay(time: Long, timeUnit: TimeUnit): BorderTask {
        borderChangeDelay = timeUnit.toSeconds(time)
        tracker = 0
        return this
    }

    fun getBorder(): Border = border

    enum class BorderAction {
        SHRINK,
        GROW,
        SET,
        NONE
    }
}

