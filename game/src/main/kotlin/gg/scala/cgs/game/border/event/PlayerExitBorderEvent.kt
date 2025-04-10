package gg.scala.cgs.game.border.event

import org.bukkit.Location
import gg.scala.cgs.game.border.Border
import org.bukkit.entity.Player

/**
 * @author Topu
 * @date 1/4/2025
 */
open class PlayerExitBorderEvent(
    border: Border,
    player: Player,
    val from: Location,
    val to: Location
) : PlayerBorderEvent(border, player)
