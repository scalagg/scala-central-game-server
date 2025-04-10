package gg.scala.cgs.game.deathmessage

import gg.scala.cgs.game.deathmessage.damage.Damage
import gg.scala.flavor.service.Service
import org.bukkit.entity.Player
import java.util.*
import kotlin.collections.HashMap

/**
 * @author Topu
 * @date 11/17/2024
 */
@Service
object DeathMessageService {

    private var config: DeathMessageConfig = DeathMessageConfig.DEFAULT_CONFIG
    private val damage: MutableMap<UUID, MutableList<Damage>> = HashMap()

    fun getDamage(player: Player): List<Damage> {
        return damage[player.uniqueId] ?: emptyList()
    }

    fun getDamage(playerUUID: UUID): List<Damage> {
        return damage[playerUUID] ?: emptyList()
    }

    fun addDamage(player: Player, addedDamage: Damage) {
        damage.putIfAbsent(player.uniqueId, mutableListOf())
        val damageList = damage[player.uniqueId]!!
        while (damageList.size > 30) {
            damageList.removeAt(0)
        }
        damageList.add(addedDamage)
    }

    fun clearDamage(player: Player) {
        damage.remove(player.uniqueId)
    }

    fun getConfig(): DeathMessageConfig {
        return config
    }

    fun setConfig(newConfig: DeathMessageConfig) {
        config = newConfig
    }
}