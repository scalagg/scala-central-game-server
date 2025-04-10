package gg.scala.cgs.game.border.actions

import gg.scala.cgs.game.ScGameFramework
import gg.scala.cgs.game.border.event.BorderChangeEvent
import gg.scala.cgs.game.border.event.PlayerExitBorderEvent
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.scheduler.BukkitRunnable
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * @author Topu
 * @date 1/4/2025
 */
object BorderActions {

    val lastMessaged: MutableMap<UUID, Long> = mutableMapOf()

    val CANCEL_EXIT: (PlayerExitBorderEvent) -> Unit
    val PUSHBACK_ON_EXIT: (PlayerExitBorderEvent) -> Unit
    val ENSURE_PLAYERS_IN_BORDER: (BorderChangeEvent) -> Unit

    init {
        CANCEL_EXIT = { playerExitBorderEvent ->
            val player = playerExitBorderEvent.player
            playerExitBorderEvent.isCancelled = true
            if (!lastMessaged.containsKey(player.uniqueId) || System.currentTimeMillis() - lastMessaged[player.uniqueId]!! > TimeUnit.SECONDS.toMillis(1L)) {
                player.sendMessage("${CC.RED}You have reached the border!")
                lastMessaged[player.uniqueId] = System.currentTimeMillis()
            }
        }

        PUSHBACK_ON_EXIT = { playerExitBorderEvent ->
            val player = playerExitBorderEvent.player
            player.setMetadata("Border-Pushback", FixedMetadataValue(ScGameFramework.instance, System.currentTimeMillis()))
            object : BukkitRunnable() {
                override fun run() {
                    val border = playerExitBorderEvent.border
                    val location = playerExitBorderEvent.to
                    val cuboid = border.physicalBounds
                    var validX = location.x
                    var validZ = location.z
                    if (location.blockX + 2 > cuboid.upperX) {
                        validX = (cuboid.upperX - 3).toDouble()
                    } else if (location.blockX - 2 < cuboid.lowerX) {
                        validX = (cuboid.lowerX + 4).toDouble()
                    }
                    if (location.blockZ + 2 > cuboid.upperZ) {
                        validZ = (cuboid.upperZ - 3).toDouble()
                    } else if (location.blockZ - 2 < cuboid.lowerZ) {
                        validZ = (cuboid.lowerZ + 4).toDouble()
                    }
                    val validLoc = Location(location.world, validX, location.y, validZ)
                    val velocity = validLoc.toVector().subtract(playerExitBorderEvent.to.toVector()).multiply(0.18)
                    if (player.vehicle != null) {
                        player.vehicle.velocity = velocity
                    } else {
                        player.velocity = velocity
                    }
                    if (!lastMessaged.containsKey(player.uniqueId) || System.currentTimeMillis() - lastMessaged[player.uniqueId]!! > TimeUnit.SECONDS.toMillis(1L)) {
                        player.sendMessage("${CC.RED}You have reached the border!")
                        lastMessaged[player.uniqueId] = System.currentTimeMillis()
                    }
                }
            }.runTask(ScGameFramework.instance)
        }

        ENSURE_PLAYERS_IN_BORDER = { borderChangeEvent ->
            val border = borderChangeEvent.border
            val iterator = Bukkit.getOnlinePlayers().iterator()

            while (iterator.hasNext()) {
                val player = iterator.next()
                if (player.world != border.origin.world) continue
                if (!border.contains(player.location.blockX, player.location.blockZ)) {
                    val location = border.correctLocation(player.location)
                    player.vehicle?.let { vehicle ->
                        player.leaveVehicle()
                        vehicle.teleport(location)
                        player.teleport(location)
                        vehicle.passenger = player
                    } ?: player.teleport(location)
                }
            }
        }
    }
}
