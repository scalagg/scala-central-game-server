package gg.scala.cgs.game.border.runnable

import gg.scala.cgs.game.border.Border
import gg.scala.cgs.game.border.BorderService
import net.evilblock.cubed.util.bukkit.cuboid.Cuboid
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

/**
 * @author Topu
 * @date 1/4/2025
 */
class ParticleRunnable : BukkitRunnable() {

    companion object {
        private const val RADIUS = 15
    }

    override fun run() {
        Bukkit.getOnlinePlayers().forEach { player ->
            val border = BorderService.getBorderForWorld(player.world) ?: return@forEach
            if (border.particle == null || !shouldCheck(player, border)) return@forEach

            val playerLocation = player.location
            for (x in playerLocation.blockX - 15 until playerLocation.blockX + 15) {
                for (y in playerLocation.blockY - 5 until playerLocation.blockY + 5) {
                    for (z in playerLocation.blockZ - 15 until playerLocation.blockZ + 15) {
                        val cuboid = border.physicalBounds
                        var finalX = x.toFloat()
                        var finalZ = z.toFloat()

                        if (x < 0) finalX += 1.0f
                        if (z < 0) finalZ += 1.0f

                        var location: Location? = null
                        if ((x > 0 && x == cuboid.upperX || x < 0 && x == cuboid.lowerX) &&
                            (z > 0 && z <= cuboid.upperZ || z < 0 && z >= cuboid.lowerZ)) {
                            location = Location(player.world,
                                (finalX + if (x < 0) 0.1f else -0.1f).toDouble(),
                                (y.toFloat() + 0.5f).toDouble(), finalZ + 0.5)
                        }
                        if ((z > 0 && z == cuboid.upperZ || z < 0 && z == cuboid.lowerZ) &&
                            (x > 0 && x <= cuboid.upperX || x < 0 && x >= cuboid.lowerX)) {
                            location = Location(player.world, finalX + 0.5, (y.toFloat() + 0.5f).toDouble(),
                                (finalZ + if (z < 0) 0.1f else -0.1f).toDouble()
                            )
                        }
                        location?.let {
                            player.spigot().playEffect(it, border.particle, border.material.id, 0, 0.0f, 0.0f, 0.0f, 0.0f, 1, 15)
                        }
                    }
                }
            }
        }
    }

    private fun shouldCheck(player: Player, border: Border): Boolean {
        val cuboid = border.physicalBounds.clone().inset(Cuboid.CuboidDirection.HORIZONTAL, 15)!!
        return !contains(player.location.blockX, player.location.blockZ, cuboid)
    }

    private fun contains(x: Int, z: Int, cuboid: Cuboid): Boolean {
        return x in cuboid.lowerX..cuboid.upperX && z in cuboid.lowerZ..cuboid.upperZ
    }
}
