package gg.scala.cgs.game.border

import com.google.common.base.Preconditions
import gg.scala.cgs.game.ScGameFramework
import gg.scala.cgs.game.border.runnable.EnsureInsideRunnable
import gg.scala.flavor.service.Service

/**
 * @author Topu
 * @date 1/4/2025
 */
@Service
object BorderService {
    private val borderMap: MutableMap<org.bukkit.World, Border> = java.util.HashMap()
    private var configured = false

    fun configure() {
        Preconditions.checkState(!configured)
        configured = true
        EnsureInsideRunnable().runTaskTimer(ScGameFramework.instance, 5L, 5L)
    }

    fun getBorderForWorld(world: org.bukkit.World): Border? {
        return borderMap[world]
    }

    fun addBorder(border: Border) {
        borderMap[border.origin.world] = border
    }
}

