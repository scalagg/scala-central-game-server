package gg.scala.cgs.game.combat

import gg.scala.cgs.game.ScGameFramework
import gg.scala.cgs.game.combat.adapter.CombatLogAdapter
import gg.scala.cgs.game.combat.event.CombatLogDespawnEvent
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.time.TimeUtil
import org.apache.commons.lang.time.DurationFormatUtils
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * @author Topu
 * @date 11/17/2024
 */
class CombatLog(player: Player, time: Long, unit: TimeUnit) {

    companion object {
        const val COMBAT_LOGGER_METADATA = "Cgs-CombatLog"
    }

    val playerName: String = player.name
    val playerUuid: UUID = player.uniqueId
    val armor: Array<ItemStack> = player.inventory.armorContents
    val inventory: Array<ItemStack> = player.inventory.contents
    private var health: Double = 20.0
    private val effects: MutableSet<PotionEffect> = mutableSetOf()
    private var despawnTime: Long = unit.toSeconds(time)
    private val timeStart: Long = System.currentTimeMillis()
    private var entityType: EntityType = EntityType.VILLAGER
    private var nameFormat: String = CC.YELLOW.toString() + "%s"
    var eventAdapter: CombatLogAdapter? = null
    var spawnedEntity: LivingEntity? = null

    fun getTimeLeft(): String {
        return formatDuration((timeStart + (despawnTime * 1000L)) - System.currentTimeMillis())
    }

    fun setDespawnTime(time: Long, unit: TimeUnit): CombatLog {
        despawnTime = unit.toSeconds(time)
        return this
    }

    fun setEntityType(entityType: EntityType): CombatLog {
        if (!entityType.isAlive && !entityType.isSpawnable) {
            throw IllegalArgumentException("EntityType must be living and spawnable!")
        }
        this.entityType = entityType
        return this
    }

    fun setHealth(health: Double): CombatLog {
        this.health = health
        return this
    }

    fun setNameFormat(nameFormat: String): CombatLog {
        this.nameFormat = nameFormat
        return this
    }

    fun setPotionEffects(effects: Collection<PotionEffect>): CombatLog {
        this.effects.addAll(effects)
        return this
    }

    fun setAdapter(adapter: CombatLogAdapter): CombatLog {
        this.eventAdapter = adapter
        return this
    }

    fun spawn(location: Location): LivingEntity {
        val entity = location.world.spawnEntity(location, entityType) as LivingEntity
        entity.setMetadata(COMBAT_LOGGER_METADATA, FixedMetadataValue(ScGameFramework.instance, "001100010010011110100001"))
        CombatLogService.combatLogMap[entity.uniqueId] = this
        CombatLogService.combatLogMap[playerUuid] = this
        entity.customName = String.format(nameFormat, playerName)
        entity.isCustomNameVisible = true
        entity.canPickupItems = false
        entity.addPotionEffects(effects)
        entity.addPotionEffect(PotionEffect(PotionEffectType.SLOW, Int.MAX_VALUE, 100), true)
        entity.maxHealth = health + 2.0
        entity.health = health

        object : BukkitRunnable() {
            override fun run() {
                if (!entity.isDead && entity.isValid) {
                    entity.remove()
                    CombatLogService.combatLogMap.remove(entity.uniqueId)
                    CombatLogService.combatLogMap.remove(playerUuid)
                    Bukkit.getPluginManager().callEvent(CombatLogDespawnEvent(this@CombatLog))
                }
            }
        }.runTaskLater(ScGameFramework.instance, despawnTime * 20L)

        spawnedEntity = entity
        return entity
    }

    private fun formatDuration(time: Long): String {
        return DurationFormatUtils.formatDurationWords(time, true, true)
    }
}
