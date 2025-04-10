package gg.scala.cgs.game.deathmessage.tracker

import gg.scala.cgs.game.deathmessage.DeathMessageService
import gg.scala.cgs.game.deathmessage.damage.Damage
import gg.scala.cgs.game.deathmessage.damage.PlayerDamage
import gg.scala.cgs.game.deathmessage.event.CustomPlayerDamageEvent
import gg.scala.commons.annotations.Listeners
import net.evilblock.cubed.util.CC
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * @author Topu
 * @date 11/17/2024
 */
@Listeners
object GeneralTracker : Listener {

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onCustomPlayerDamage(event: CustomPlayerDamageEvent) {
        val message = when (event.cause.cause) {
            EntityDamageEvent.DamageCause.SUFFOCATION -> "suffocated"
            EntityDamageEvent.DamageCause.DROWNING -> "drowned"
            EntityDamageEvent.DamageCause.STARVATION -> "starved to death"
            EntityDamageEvent.DamageCause.LIGHTNING -> "was struck by lightning"
            EntityDamageEvent.DamageCause.POISON -> "was poisoned"
            EntityDamageEvent.DamageCause.WITHER -> "withered away"
            EntityDamageEvent.DamageCause.CONTACT -> "was pricked to death"
            EntityDamageEvent.DamageCause.ENTITY_EXPLOSION,
            EntityDamageEvent.DamageCause.BLOCK_EXPLOSION -> "was blown to smithereens"
            else -> return
        }

        val record = DeathMessageService.getDamage(event.player)
        var knocker: Damage? = null
        var knockerTime = 0L

        for (damage in record) {
            if (damage is GeneralDamageByPlayer || damage !is PlayerDamage || knocker != null && damage.time <= knockerTime) continue
            knocker = damage
            knockerTime = damage.time
        }

        event.trackerDamage = if (knocker != null && knockerTime + TimeUnit.MINUTES.toMillis(1L) > System.currentTimeMillis()) {
            GeneralDamageByPlayer(event.player.uniqueId, event.getDamage(), (knocker as PlayerDamage).damager, message)
        } else {
            GeneralDamage(event.player.uniqueId, event.getDamage(), message)
        }
    }

    class GeneralDamageByPlayer(
        damaged: UUID,
        damage: Double,
        damager: UUID,
        private val message: String
    ) : PlayerDamage(damaged, damage, damager) {

        override fun getDeathMessage(getFor: UUID): String {
            return "${wrapName(damaged, getFor)} ${CC.YELLOW}$message while fighting ${wrapName(damager, getFor)}${CC.YELLOW}."
        }
    }

    class GeneralDamage(
        damaged: UUID,
        damage: Double,
        private val message: String
    ) : Damage(damaged, damage) {

        override fun getDeathMessage(getFor: UUID): String {
            return "${wrapName(damaged, getFor)} ${CC.YELLOW}$message."
        }
    }
}