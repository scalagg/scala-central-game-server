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
object VoidTracker : Listener {

    @EventHandler(priority = EventPriority.LOW)
    fun onCustomPlayerDamage(event: CustomPlayerDamageEvent) {
        if (event.cause.cause != EntityDamageEvent.DamageCause.VOID) {
            return
        }
        val record = DeathMessageService.getDamage(event.player)
        var knocker: Damage? = null
        var knockerTime = 0L
        for (damage in record) {
            if (damage is VoidDamageByPlayer || damage !is PlayerDamage || knocker != null && damage.time <= knockerTime) continue
            knocker = damage
            knockerTime = damage.time
        }
        if (knocker != null && knockerTime + TimeUnit.MINUTES.toMillis(1) > System.currentTimeMillis()) {
            event.trackerDamage = VoidDamageByPlayer(event.player.uniqueId, event.getDamage(), (knocker as PlayerDamage).damager)
        } else {
            event.trackerDamage = VoidDamage(event.player.uniqueId, event.getDamage())
        }
    }

    open class VoidDamageByPlayer(damaged: UUID, damage: Double, damager: UUID) : PlayerDamage(damaged, damage, damager) {
        override fun getDeathMessage(getFor: UUID): String {
            return "${wrapName(this.damaged, getFor)}${CC.YELLOW} fell into the void thanks to ${wrapName(this.damager, getFor)}${CC.YELLOW}."
        }
    }

    open class VoidDamage(damaged: UUID, damage: Double) : Damage(damaged, damage) {
        override fun getDeathMessage(getFor: UUID): String {
            return "${wrapName(this.damaged, getFor)}${CC.YELLOW} fell into the void."
        }
    }
}
