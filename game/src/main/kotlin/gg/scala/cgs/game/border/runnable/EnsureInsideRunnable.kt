package gg.scala.cgs.game.border.runnable

import gg.scala.cgs.game.border.BorderService
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

/**
 * @author Topu
 * @date 1/4/2025
 */
class EnsureInsideRunnable : BukkitRunnable() {

    override fun run() {
        for (player in Bukkit.getOnlinePlayers()) {
            val border = BorderService.getBorderForWorld(player.world)
            if (border == null || player.world !== border.origin.world || !shouldEnsure(player) || border.contains(
                    player.location.blockX,
                    player.location.blockZ
                )
            ) continue
            val location = border.correctLocation(player.location)
            if (player.vehicle != null) {
                val vehicle = player.vehicle
                player.leaveVehicle()
                vehicle.teleport(location)
                player.teleport(location)
                vehicle.setPassenger(player)
                continue
            }
            player.teleport(location)
        }
    }

    companion object {
        private fun isSafe(location: Location): Boolean {
            return location.block.getRelative(BlockFace.DOWN).type.isSolid
        }

        private fun shouldEnsure(player: Player): Boolean {
            if (!player.hasMetadata("Border-Pushback")) {
                return true
            }
            try {
                val pushed = player.getMetadata("Border-Pushback")[0].asLong()
                val delta = System.currentTimeMillis() - pushed
                return delta >= 500L
            } catch (e: Exception) {
                return true
            }
        }
    }
}

