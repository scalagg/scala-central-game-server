package gg.scala.cgs.game.listeners

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Arrow
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import gg.scala.cgs.game.game.GameRule
import gg.scala.cgs.game.ScGameFramework
import gg.scala.commons.annotations.Listeners
import gg.scala.lemon.util.QuickAccess.coloredName
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants.HEART_SYMBOL
import kotlin.math.ceil

@Listeners
object BowHealthListener : Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
        if (ScGameFramework.instance.game == null || !ScGameFramework.instance.game!!.getGameRule(GameRule.BOW_HEALTH_MESSAGE)) return

        val player = event.entity
        val arrow = event.damager

        if (arrow is Arrow && player is Player) {
            val shooter = arrow.shooter

            if (shooter is Player) {
                if (arrow.getName() != shooter.name) {
                    val health = ceil(player.health - event.finalDamage) / 2.0

                    if (health > 0.0) {
                        shooter.sendMessage("${coloredName(player)}${CC.SEC} is now at ${CC.PRI}$health$HEART_SYMBOL${CC.SEC}.")
                    }
                }
            }
        }
    }
}