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
object BurnTracker : Listener {

    @EventHandler(priority = EventPriority.LOW)
    fun onCustomPlayerDamage(event: CustomPlayerDamageEvent) {
        val cause = event.cause.cause
        if (cause != EntityDamageEvent.DamageCause.FIRE_TICK && cause != EntityDamageEvent.DamageCause.LAVA) {
            return
        }

        val record = DeathMessageService.getDamage(event.player)
        var knocker: Damage? = null
        var knockerTime = 0L

        for (damage in record) {
            if (damage is BurnDamageByPlayer || damage !is PlayerDamage || knocker != null && damage.time <= knockerTime) {
                continue
            }
            knocker = damage
            knockerTime = damage.time
        }

        if (knocker != null && knockerTime + TimeUnit.MINUTES.toMillis(1) > System.currentTimeMillis()) {
            event.trackerDamage = BurnDamageByPlayer(event.player.uniqueId, event.getDamage(), (knocker as PlayerDamage).damager)
        } else {
            event.trackerDamage = BurnDamage(event.player.uniqueId, event.getDamage())
        }
    }

    class BurnDamageByPlayer(damaged: UUID, damage: Double, damager: UUID) : PlayerDamage(damaged, damage, damager) {
        override fun getDeathMessage(getFor: UUID): String {
            return "${wrapName(damaged, getFor)}${CC.YELLOW} burned to death thanks to ${wrapName(damager, getFor)}${CC.YELLOW}."
        }
    }

    class BurnDamage(damaged: UUID, damage: Double) : Damage(damaged, damage) {
        override fun getDeathMessage(getFor: UUID): String {
            return "${wrapName(damaged, getFor)}${CC.YELLOW} burned to death."
        }
    }
}