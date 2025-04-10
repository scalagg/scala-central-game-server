package gg.scala.cgs.game.deathmessage.listener

import gg.scala.cgs.game.ScGameFramework
import gg.scala.cgs.game.deathmessage.DeathMessageService
import gg.scala.cgs.game.deathmessage.damage.UnknownDamage
import gg.scala.cgs.game.deathmessage.event.CustomPlayerDamageEvent
import gg.scala.commons.annotations.Listeners
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent

/**
 * @author Topu
 * @date 11/17/2024
 */
@Listeners
object DamageListener : Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onEntityDamage(event: EntityDamageEvent) {
        if (event.entity is Player) {
            val player = event.entity as Player
            val customEvent = CustomPlayerDamageEvent(event)
            customEvent.trackerDamage = UnknownDamage(player.uniqueId, customEvent.getDamage())
            ScGameFramework.instance.server.pluginManager.callEvent(customEvent)
            DeathMessageService.addDamage(player, customEvent.trackerDamage as UnknownDamage)
        }
    }
}