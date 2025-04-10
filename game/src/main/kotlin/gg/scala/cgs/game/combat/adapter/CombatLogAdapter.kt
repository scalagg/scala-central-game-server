package gg.scala.cgs.game.combat.adapter

import gg.scala.cgs.game.combat.CombatLog
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent

/**
 * @author Topu
 * @date 11/17/2024
 */
interface CombatLogAdapter {

 fun onEntityDamageByEntity(logger: CombatLog?, event: EntityDamageByEntityEvent?) {
 }

 fun onEntityDeath(logger: CombatLog?, event: EntityDeathEvent?) {
 }
}