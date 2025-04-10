package gg.scala.cgs.game.combat.listener

import com.mojang.authlib.GameProfile
import gg.scala.cgs.game.ScGameFramework
import gg.scala.cgs.game.combat.CombatLog
import gg.scala.cgs.game.combat.CombatLogService
import gg.scala.cgs.game.deathmessage.DeathMessageService
import gg.scala.cgs.game.deathmessage.damage.Damage
import gg.scala.commons.annotations.Listeners
import net.minecraft.server.v1_8_R3.EntityPlayer
import net.minecraft.server.v1_8_R3.MinecraftServer
import net.minecraft.server.v1_8_R3.PlayerInteractManager
import org.apache.commons.lang.WordUtils
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_8_R3.CraftServer
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityInteractEvent
import org.bukkit.event.entity.EntityPortalEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.world.ChunkUnloadEvent
import java.util.*

/**
 * @author Topu
 * @date 11/17/2024
 */
@Listeners
object CombatLogListener : Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onEntityDeath(event: EntityDeathEvent) {
        val logger = CombatLogService.combatLogMap[event.entity.uniqueId]
        if (event.entity.hasMetadata("Cgs-CombatLog") && logger != null) {
            // Add armor and inventory items to drops
            logger.armor.forEach { event.drops.add(it) }
            logger.inventory.forEach { event.drops.add(it) }

            // Call event adapter
            logger.eventAdapter?.onEntityDeath(logger, event)

            val configuration = CombatLogService.getConfig()
            val dmConfig = DeathMessageService.getConfig()
            val killer = event.entity.killer

            // Handle death message asynchronously
            configuration.let { config ->
                dmConfig.let { deathMessageConfig ->
                    Bukkit.getScheduler().runTaskAsynchronously(ScGameFramework.instance) {
                        ScGameFramework.instance.server.onlinePlayers.forEach { player ->
                            val deathMessage = getCombatLoggerDeathMessage(logger.playerUuid, killer, player.uniqueId)
                            val showDeathMessage = deathMessageConfig.shouldShowDeathMessage(player.uniqueId, logger.playerUuid, killer.uniqueId)
                            if (showDeathMessage) {
                                player.sendMessage(deathMessage)
                            }
                        }
                    }
                }
            }

            // Handle player data and despawn logic
            val target = ScGameFramework.instance.server.getPlayer(logger.playerUuid)
                ?: (spawnEntityForLogger(logger) ?: return)

            CombatLogService.combatLogMap.apply {
                remove(event.entity.uniqueId)
                remove(logger.playerUuid)
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onEntityInteract(event: PlayerInteractEntityEvent) {
        if (event.rightClicked.hasMetadata("Cgs-CombatLog")) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onChunkUnload(event: ChunkUnloadEvent) {
        event.chunk.entities
            .filter { it.hasMetadata("Cgs-CombatLog") && !it.isDead }
            .forEach { event.isCancelled = true }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onEntityPortal(event: EntityPortalEvent) {
        if (event.entity.hasMetadata("Cgs-CombatLog")) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val logger = CombatLogService.combatLogMap[event.player.uniqueId]
        logger?.spawnedEntity?.takeIf { it.isValid && !it.isDead }?.let {
            it.remove()
            CombatLogService.combatLogMap.apply {
                remove(it.uniqueId)
                remove(event.player.uniqueId)
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
        if (event.entity.hasMetadata("Cgs-CombatLog")) {
            val logger = CombatLogService.combatLogMap[event.entity.uniqueId]
            logger?.eventAdapter?.onEntityDamageByEntity(logger, event)
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onEntityPressurePlate(event: EntityInteractEvent) {
        if (event.block.type in listOf(Material.STONE_PLATE, Material.GOLD_PLATE, Material.IRON_PLATE, Material.WOOD_PLATE) &&
            event.entity.hasMetadata("Cgs-CombatLog")) {
            event.isCancelled = true
        }
    }

    private fun getCombatLoggerDeathMessage(player: UUID, killer: Player, getFor: UUID): String {
        val itemString = killer?.itemInHand?.takeIf { it.type != Material.AIR }?.let {
            if (it.itemMeta?.hasDisplayName() == true) {
                ChatColor.stripColor(it.itemMeta.displayName)
            } else {
                WordUtils.capitalizeFully(it.type.name.replace('_', ' '))
            }
        } ?: "their fists"

        return "${wrapLogger(player, getFor)}${ChatColor.YELLOW} was slain by ${Damage.wrapName(killer.uniqueId, getFor)}" +
                if (!DeathMessageService.getConfig().hideWeapons()) " using ${ChatColor.RED}$itemString" else "" +
                        "${ChatColor.YELLOW}."
    }

    private fun wrapLogger(player: UUID, wrapFor: UUID): String {
        return CombatLogService.getConfig().formatPlayerName(player, wrapFor)
    }

    private fun spawnEntityForLogger(logger: CombatLog): LivingEntity {
        val server = MinecraftServer.getServer()
        val entityPlayer = EntityPlayer(
            (server as CraftServer).server,
            server.worldServer[0],
            GameProfile(logger.playerUuid, logger.playerName),
            PlayerInteractManager(server.worldServer[0])
        ).bukkitEntity
        entityPlayer.loadData()
        return entityPlayer
    }

    private fun Player.clearInventoryAndSaveData() {
        inventory.clear()
        inventory.armorContents = null
        saveData()
    }
}
