package gg.scala.cgs.game.deathmessage.tracker

import gg.scala.cgs.game.deathmessage.DeathMessageService
import gg.scala.cgs.game.deathmessage.damage.PlayerDamage
import gg.scala.cgs.game.deathmessage.event.CustomPlayerDamageEvent
import gg.scala.commons.annotations.Listeners
import org.apache.commons.lang.WordUtils
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import java.util.*

/**
 * @author Topu
 * @date 11/17/2024
 */
@Listeners
object PvPTracker : Listener {

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onCustomPlayerDamage(event: CustomPlayerDamageEvent) {
        if (event.cause !is EntityDamageByEntityEvent) return

        val damageByEntityEvent = event.cause
        val damager = damageByEntityEvent.damager
        if (damager is Player) {
            val damaged = event.player
            event.trackerDamage = PvPDamage(damaged.uniqueId, event.getDamage(), damager)
        }
    }

    class PvPDamage(
        damaged: UUID,
        damage: Double,
        damager: Player
    ) : PlayerDamage(damaged, damage, damager.uniqueId) {

        private val itemString: String

        init {
            val hand: ItemStack = damager.itemInHand
            itemString = if (hand.type == Material.AIR) {
                "their fists"
            } else {
                hand.itemMeta?.displayName?.let {
                    ChatColor.stripColor(it)
                } ?: WordUtils.capitalizeFully(hand.type.name.replace('_', ' '))
            }
        }

        override fun getDeathMessage(getFor: UUID): String {
            val extraMessage = DeathMessageService.getConfig().extraMessage(damager)
            val baseMessage = wrapName(damaged, getFor) + ChatColor.YELLOW +
                    extraMessage.let { " $it " } +
                    wrapName(damager, getFor) + ChatColor.YELLOW

            return if (!DeathMessageService.getConfig().hideWeapons()) {
                "$baseMessage using ${ChatColor.RED}${itemString.trim()}${ChatColor.YELLOW}."
            } else {
                "$baseMessage."
            }
        }
    }
}