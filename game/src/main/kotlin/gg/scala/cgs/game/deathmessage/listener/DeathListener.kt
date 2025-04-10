package gg.scala.cgs.game.deathmessage.listener

import gg.scala.cgs.game.ScGameFramework
import gg.scala.cgs.game.deathmessage.DeathMessageService
import gg.scala.cgs.game.deathmessage.damage.PlayerDamage
import gg.scala.cgs.game.deathmessage.damage.UnknownDamage
import gg.scala.commons.annotations.Listeners
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import java.util.concurrent.TimeUnit

/**
 * @author Topu
 * @date 11/17/2024
 */
@Listeners
object DeathListener : Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    fun onPlayerDeathEarly(event: PlayerDeathEvent) {
        val record = DeathMessageService.getDamage(event.entity)
        if (record.isNotEmpty()) {
            val deathCause = record.last()
            if (deathCause is PlayerDamage && deathCause.getTimeAgoMillis() < TimeUnit.MINUTES.toMillis(1L)) {
                val killerUuid = deathCause.damager
                val killerPlayer = ScGameFramework.instance.server.getPlayer(killerUuid)
                if (killerPlayer != null) {
                    (event.entity as CraftPlayer).handle.killer = (killerPlayer as CraftPlayer).handle
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerDeathLate(event: PlayerDeathEvent) {
        val record = DeathMessageService.getDamage(event.entity)
        val deathCause = if (record.isNotEmpty()) record.last() else UnknownDamage(event.entity.uniqueId, 1.0)
        DeathMessageService.clearDamage(event.entity)
        event.deathMessage = null
        val config = DeathMessageService.getConfig()
        val diedUuid = event.entity.uniqueId
        val killerUuid = event.entity.killer.uniqueId
        for (player in ScGameFramework.instance.server.onlinePlayers) {
            val showDeathMessage = config.shouldShowDeathMessage(player.uniqueId, diedUuid, killerUuid)
            if (showDeathMessage) {
                val deathMessage = deathCause.getDeathMessage(player.uniqueId)
                player.sendMessage(deathMessage)
            }
        }
    }
}