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
object FallTracker : Listener {

    @EventHandler(priority = EventPriority.LOW)
    fun onCustomPlayerDamage(event: CustomPlayerDamageEvent) {
        if (event.cause.cause != EntityDamageEvent.DamageCause.FALL) {
            return
        }

        val record = DeathMessageService.getDamage(event.player)
        var knocker: Damage? = null
        var knockerTime = 0L

        for (damage in record) {
            if (damage is FallDamageByPlayer || damage !is PlayerDamage || knocker != null && damage.time <= knockerTime) continue
            knocker = damage
            knockerTime = damage.time
        }

        event.trackerDamage = if (knocker != null && knockerTime + TimeUnit.MINUTES.toMillis(1L) > System.currentTimeMillis()) {
            FallDamageByPlayer(event.player.uniqueId, event.getDamage(), (knocker as PlayerDamage).damager)
        } else {
            FallDamage(event.player.uniqueId, event.getDamage())
        }
    }

    class FallDamageByPlayer(
        damaged: UUID,
        damage: Double,
        damager: UUID
    ) : PlayerDamage(damaged, damage, damager) {

        override fun getDeathMessage(getFor: UUID): String {
            return "${wrapName(damaged, getFor)}${CC.YELLOW} hit the ground too hard thanks to ${wrapName(damager, getFor)}${CC.YELLOW}."
        }
    }

    class FallDamage(
        damaged: UUID,
        damage: Double
    ) : Damage(damaged, damage) {

        override fun getDeathMessage(getFor: UUID): String {
            return "${wrapName(damaged, getFor)}${CC.YELLOW} hit the ground too hard."
        }
    }
}