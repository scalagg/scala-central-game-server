package gg.scala.cgs.game.listeners

import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import gg.scala.cgs.game.game.GameRule
import gg.scala.cgs.game.game.GameStage
import gg.scala.cgs.game.ScGameFramework
import gg.scala.cgs.game.utils.ServerUtils
import gg.scala.commons.annotations.Listeners

@Listeners
object DamageListener : Listener {

    @EventHandler
    fun onDamage(event: EntityDamageByEntityEvent) {
        if (event.entity !is Player) return
        if (event.damager !is Player) return

        val attacker = event.damager as Player
        val victim = event.entity as Player

        val game = ScGameFramework.instance.game ?: return
        val team = game.getTeam(attacker) ?: return

        when (game.gameStage) {
            GameStage.IN_GAME -> {
                if (team == game.getTeam(victim) && !game.getGameRule(GameRule.FRIENDLY_FIRE)) {
                    attacker.sendMessage("${ChatColor.RED}You cannot hurt ${ServerUtils.getName(victim.uniqueId)}.")
                    event.isCancelled = true
                }
                ScGameFramework.instance.game!!.getGamePlayer(attacker)?.addDamageDealt(event.damage)
            }

            else -> {
                if(!game.getGameRule(GameRule.ALLOW_DAMAGE_IN_ALL_STAGES))
                    event.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onDamaged(event: EntityDamageEvent) {
        if (event.entity !is Player) return
        val player = event.entity as Player

        val game = ScGameFramework.instance.game ?: return
        if (event.cause == EntityDamageEvent.DamageCause.CUSTOM) return

        when (game.gameStage) {
            GameStage.IN_GAME -> {
                if (event.cause == EntityDamageEvent.DamageCause.FALL && player.hasMetadata("nofall")) {
                    player.removeMetadata("nofall", ScGameFramework.instance)
                    event.isCancelled = true
                    return
                }
                ScGameFramework.instance.game!!.getGamePlayer(player)?.addDamageTaken(event.damage)
            }

            else -> {
                if(!game.getGameRule(GameRule.ALLOW_DAMAGE_IN_ALL_STAGES))
                    event.isCancelled = true
            }
        }
    }

}