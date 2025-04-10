package gg.scala.cgs.game.deathmessage.damage

import java.util.*

/**
 * @author Topu
 * @date 11/17/2024
 */
abstract class PlayerDamage(
    damaged: UUID,
    damage: Double,
    val damager: UUID
) : Damage(damaged, damage)
