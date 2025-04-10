package gg.scala.cgs.game.border.listener

import gg.scala.cgs.game.border.actions.BorderActions
import gg.scala.cgs.game.border.BorderService
import gg.scala.cgs.game.border.event.PlayerEnterBorderEvent
import gg.scala.cgs.game.border.event.PlayerExitBorderEvent
import gg.scala.commons.annotations.Listeners
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Horse
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerPortalEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.event.vehicle.VehicleUpdateEvent
import java.util.concurrent.TimeUnit

/**
 * @author Topu
 * @date 1/4/2025
 */
@Listeners
object BorderListener : Listener {

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val fromLoc = event.from
        val toLoc = event.to
        val border = BorderService.getBorderForWorld(fromLoc.world) ?: return
        val from = border.contains(fromLoc.blockX, fromLoc.blockZ)
        val to = border.contains(toLoc.blockX, toLoc.blockZ)
        val movedBlock = fromLoc.blockX != toLoc.blockX || fromLoc.blockY != toLoc.blockY || fromLoc.blockZ != toLoc.blockZ

        if (movedBlock) {
            val playerBorderEvent = when {
                from && !to -> PlayerExitBorderEvent(border, event.player, fromLoc, toLoc)
                !from && to -> PlayerEnterBorderEvent(border, event.player, fromLoc, toLoc)
                else -> null
            }

            playerBorderEvent?.let {
                Bukkit.getPluginManager().callEvent(it)
                if (it.isCancelled) event.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onVehicleUpdate(event: VehicleUpdateEvent) {
        val vehicle = event.vehicle
        val passenger = vehicle.passenger

        if (passenger !is Player) return

        val border = BorderService.getBorderForWorld(vehicle.world) ?: return
        val player = passenger
        if (!border.contains(vehicle.location) && vehicle is Horse) {
            val location = vehicle.location
            val cuboid = border.physicalBounds
            var validX = location.x
            var validZ = location.z

            if (location.blockX + 2 > cuboid.upperX) validX = (cuboid.upperX - 3).toDouble()
            else if (location.blockX - 2 < cuboid.lowerX) validX = (cuboid.lowerX + 4).toDouble()

            if (location.blockZ + 2 > cuboid.upperZ) validZ = (cuboid.upperZ - 3).toDouble()
            else if (location.blockZ - 2 < cuboid.lowerZ) validZ = (cuboid.lowerZ + 4).toDouble()

            val validLoc = Location(location.world, validX, location.y, validZ)
            val velocity = validLoc.toVector().subtract(location.toVector()).multiply(2)
            vehicle.velocity = velocity

            val lastMessaged = BorderActions.lastMessaged
            if (lastMessaged?.get(player.uniqueId)?.let { System.currentTimeMillis() - it > TimeUnit.SECONDS.toMillis(1L) } == true) {
                player.sendMessage("${CC.RED}You have reached the border!")
                BorderActions.lastMessaged[player.uniqueId] = System.currentTimeMillis()
            }
        }
    }

    @EventHandler
    fun onPlayerPortal(event: PlayerPortalEvent) {
        val border = BorderService.getBorderForWorld(event.to.world) ?: return
        val location = if (event.useTravelAgent()) event.portalTravelAgent.findOrCreate(event.to) else event.to
        val cuboid = border.physicalBounds
        var validX = location.x
        var validZ = location.z
        val buffer = 30

        if (location.blockX + 2 > cuboid.upperX) validX = (cuboid.upperX - buffer).toDouble()
        else if (location.blockX - 2 < cuboid.lowerX) validX = (cuboid.lowerX + buffer + 1).toDouble()

        if (location.blockZ + 2 > cuboid.upperZ) validZ = (cuboid.upperZ - buffer).toDouble()
        else if (location.blockZ - 2 < cuboid.lowerZ) validZ = (cuboid.lowerZ + buffer + 1).toDouble()

        val validLoc = Location(location.world, validX, location.y, validZ)
        event.to = validLoc
    }

    @EventHandler
    fun onPlayerTeleport(event: PlayerTeleportEvent) {
        val toLoc = event.to
        val border = BorderService.getBorderForWorld(toLoc.world) ?: return
        val to = border.contains(toLoc.blockX, toLoc.blockZ)

        if (!to && event.cause == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            event.isCancelled = true
            event.to = event.from
        }
    }
}
