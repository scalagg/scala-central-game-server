package gg.scala.cgs.game.deathmessage.event

/**
 * @author Topu
 * @date 11/17/2024
 */
import gg.scala.cgs.game.deathmessage.damage.Damage
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent
import org.bukkit.event.entity.EntityDamageEvent

class CustomPlayerDamageEvent(
    val cause: EntityDamageEvent
) : PlayerEvent(cause.entity as Player) {

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }

    var trackerDamage: Damage? = null

    fun getDamage(): Double {
        return cause.getDamage(EntityDamageEvent.DamageModifier.BASE)
    }

    override fun getHandlers(): HandlerList {
        return handlerList
    }
}
