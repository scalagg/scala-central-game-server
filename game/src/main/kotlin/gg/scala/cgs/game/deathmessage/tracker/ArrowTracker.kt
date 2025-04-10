package gg.scala.cgs.game.deathmessage.tracker

import gg.scala.cgs.game.deathmessage.damage.Damage
import gg.scala.cgs.game.deathmessage.damage.MobDamage
import gg.scala.cgs.game.deathmessage.damage.PlayerDamage
import gg.scala.cgs.game.deathmessage.event.CustomPlayerDamageEvent
import gg.scala.cgs.common.util.EntityUtil
import gg.scala.cgs.game.ScGameFramework
import gg.scala.commons.annotations.Listeners
import net.evilblock.cubed.util.CC
import org.bukkit.Location
import org.bukkit.entity.Arrow
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.metadata.FixedMetadataValue
import java.util.*

/**
 * @author Topu
 * @date 11/17/2024
 */
@Listeners
object ArrowTracker : Listener {

    @EventHandler
    fun onEntityShootBow(event: EntityShootBowEvent) {
        if (event.entity is Player) {
            event.projectile.setMetadata(
                "ShotFromDistance",
                FixedMetadataValue(ScGameFramework.instance, event.projectile.location)
            )
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onCustomPlayerDamage(event: CustomPlayerDamageEvent) {
        val cause = event.cause
        if (cause !is EntityDamageByEntityEvent) return

        val damager = cause.damager
        if (damager !is Arrow) return

        when (val shooter = damager.shooter) {
            is Player -> {
                for (value in damager.getMetadata("ShotFromDistance")) {
                    val shotFrom = value.value() as Location
                    val distance = shotFrom.distance(event.player.location)
                    event.trackerDamage = ArrowDamageByPlayer(
                        event.player.uniqueId, event.getDamage(), shooter.uniqueId, distance
                    )
                }
            }

            is Entity -> {
                event.trackerDamage = ArrowDamageByMob(
                    event.player.uniqueId, event.getDamage(), shooter
                )
            }

            else -> {
                event.trackerDamage = ArrowDamage(
                    event.player.uniqueId, event.getDamage()
                )
            }
        }
    }

    class ArrowDamageByMob(
        damaged: UUID,
        damage: Double,
        damager: Entity
    ) : MobDamage(damaged, damage, damager.type) {

        override fun getDeathMessage(player: UUID): String {
            return "${wrapName(damaged, player)}${CC.YELLOW} was shot by a ${CC.RED}${
                EntityUtil.getName(
                    mobType
                )
            }${CC.YELLOW}."
        }
    }

    class ArrowDamageByPlayer(
        damaged: UUID,
        damage: Double,
        damager: UUID,
        private val distance: Double
    ) : PlayerDamage(damaged, damage, damager) {

        override fun getDeathMessage(player: UUID): String {
            return "${wrapName(damaged, player)}${CC.YELLOW} was shot by ${
                wrapName(
                    damager,
                    player
                )
            }${CC.YELLOW} from ${CC.BLUE}${distance.toInt()} blocks${CC.YELLOW}."
        }
    }

    class ArrowDamage(
        damaged: UUID,
        damage: Double
    ) : Damage(damaged, damage) {

        override fun getDeathMessage(player: UUID): String {
            return "${wrapName(damaged, player)}${CC.YELLOW} was shot."
        }
    }
}