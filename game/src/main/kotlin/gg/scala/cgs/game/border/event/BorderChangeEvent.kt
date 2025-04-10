package gg.scala.cgs.game.border.event

import gg.scala.cgs.game.border.Border
import gg.scala.cgs.game.border.runnable.BorderTask
import net.evilblock.cubed.util.bukkit.cuboid.Cuboid

/**
 * @author Topu
 * @date 1/4/2025
 */
open class BorderChangeEvent(
    border: Border,
    val previousSize: Int,
    val previousBounds: Cuboid,
    val action: BorderTask.BorderAction
) : BorderEvent(border)

