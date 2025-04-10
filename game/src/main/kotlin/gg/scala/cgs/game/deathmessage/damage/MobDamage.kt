package gg.scala.cgs.game.deathmessage.damage

import org.bukkit.entity.EntityType
import java.util.*

/**
 * @author Topu
 * @date 11/17/2024
 */
abstract class MobDamage(
    damaged: UUID,
    damage: Double,
    val mobType: EntityType
) : Damage(damaged, damage)
