package gg.scala.cgs.game.border.listener

import gg.scala.cgs.game.border.runnable.BorderTask
import gg.scala.cgs.game.border.event.BorderChangeEvent
import gg.scala.cgs.game.border.event.PlayerEnterBorderEvent
import gg.scala.cgs.game.border.event.PlayerExitBorderEvent
import gg.scala.commons.annotations.Listeners
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

/**
 * @author Topu
 * @date 1/4/2025
 */
@Listeners
object ExampleBorderListener : Listener {

    @EventHandler
    fun onBorderChange(event: BorderChangeEvent) {
        val actionMessage = if (event.action == BorderTask.BorderAction.SHRINK) {
            "&6&lBorder size has &cdecreased"
        } else {
            "&6&lBorder size has &aincreased&c&l!"
        }
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', actionMessage))

        val border = event.border
        if (border.physicalBounds.sizeX <= 500 && border.physicalBounds.sizeZ <= 500) {
            border.borderTask.action = BorderTask.BorderAction.NONE
        }
    }

    @EventHandler
    fun onBorderExit(event: PlayerExitBorderEvent) {
        event.player.sendMessage("${CC.RED}Warning! You have left the border!")
        val playerLocation = event.player.location
        val border = event.border
        val cuboid = border.physicalBounds
        var newX = playerLocation.blockX
        var newZ = playerLocation.blockZ

        val minX = cuboid.lowerX
        val minZ = cuboid.lowerZ
        val maxX = cuboid.upperX
        val maxZ = cuboid.upperZ

        if (maxX < playerLocation.blockX) {
            newX = maxX
        } else if (minX > playerLocation.blockX) {
            newX = minX
        }

        if (maxZ < playerLocation.blockZ) {
            newZ = maxZ
        } else if (minZ > playerLocation.blockZ) {
            newZ = minZ
        }

        event.player.teleport(Location(playerLocation.world, newX.toDouble(), playerLocation.y, newZ.toDouble()))
    }

    @EventHandler
    fun onBorderEnter(event: PlayerEnterBorderEvent) {
        event.player.sendMessage(ChatColor.GREEN.toString() + "You have entered the border")
    }
}

