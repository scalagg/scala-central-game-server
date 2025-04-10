package gg.scala.cgs.game.deathmessage.listener

import gg.scala.cgs.game.deathmessage.DeathMessageService
import gg.scala.commons.annotations.Listeners
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

/**
 * @author Topu
 * @date 11/17/2024
 */
@Listeners
object DisconnectListener : Listener {

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        DeathMessageService.clearDamage(event.player)
    }
}