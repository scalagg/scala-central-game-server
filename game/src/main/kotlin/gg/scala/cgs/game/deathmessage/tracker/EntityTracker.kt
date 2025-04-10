package gg.scala.cgs.game.deathmessage.tracker

import gg.scala.cgs.common.util.EntityUtil
import gg.scala.cgs.game.deathmessage.damage.MobDamage
import gg.scala.cgs.game.deathmessage.event.CustomPlayerDamageEvent
import gg.scala.commons.annotations.Listeners
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import java.util.*

/**
 * @author Topu
 * @date 11/17/2024
 */
@Listeners
object EntityTracker : Listener {

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onCustomPlayerDamage(event: CustomPlayerDamageEvent) {
        val cause = event.cause
        if (cause !is EntityDamageByEntityEvent) {
            return
        }

        val damager = cause.damager
        if (damager is LivingEntity && damager !is Player) {
            event.trackerDamage = EntityDamage(event.player.uniqueId, event.getDamage(), damager)
        }
    }

    class EntityDamage(damaged: UUID, damage: Double, entity: Entity) : MobDamage(damaged, damage, entity.type) {
        override fun getDeathMessage(getFor: UUID): String {
            return "${wrapName(damaged, getFor)}${CC.YELLOW} was slain by a ${CC.RED}${EntityUtil.getName(mobType)}${CC.YELLOW}."
        }
    }
}
