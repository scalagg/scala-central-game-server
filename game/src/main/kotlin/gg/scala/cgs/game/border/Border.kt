package gg.scala.cgs.game.border

import gg.scala.cgs.game.ScGameFramework
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import gg.scala.cgs.game.border.event.BorderChangeEvent
import gg.scala.cgs.game.border.runnable.BorderTask
import net.evilblock.cubed.util.bukkit.cuboid.Cuboid
import org.bukkit.block.BlockFace
import kotlin.math.max
import kotlin.math.min

/**
 * @author Topu
 * @date 1/4/2025
 */
class Border(val origin: Location, material: Material?, var size: Int, var height: Int) {
    var material: Material
        private set
    var isWrapTerrain: Boolean = false
        private set
    var borderConfig: BorderConfig = BorderConfig.Companion.DEFAULT_CONFIGURATION
        private set
    var particle: Effect? = null
        private set
    var physicalBounds: Cuboid
    val borderTask: BorderTask

    fun contract(amount: Int): Cuboid {
        this.size -= amount
        val prev: Cuboid = physicalBounds.clone()
        this.physicalBounds = physicalBounds.inset(Cuboid.CuboidDirection.HORIZONTAL, amount)!!
        return prev
    }

    fun expand(amount: Int): Cuboid {
        this.size += amount
        val prev: Cuboid = physicalBounds.clone()
        this.physicalBounds =
            physicalBounds.expand(Cuboid.CuboidDirection.NORTH, amount).expand(Cuboid.CuboidDirection.SOUTH, amount)
                .expand(Cuboid.CuboidDirection.EAST, amount).expand(Cuboid.CuboidDirection.WEST, amount)
        return prev
    }

    fun setSize(size: Int): Cuboid {
        return this.setSize(size, true)
    }

    fun setSize(size: Int, callEvent: Boolean): Cuboid {
        return this.setSize(size, this.height, callEvent)
    }

    fun setSize(size: Int, height: Int, callEvent: Boolean): Cuboid {
        val previousSize = this.size
        this.size = size
        this.height = height
        val prev: Cuboid = physicalBounds.clone()
        this.physicalBounds = Cuboid(
            origin.clone().add(
                (size + 1).toDouble(),
                origin.world.maxHeight.toDouble() - origin.y, (size + 1).toDouble()
            ),
            origin.clone().subtract((size + 1).toDouble(), origin.y, (size + 1).toDouble())
        )
        if (callEvent) {
            Bukkit.getPluginManager().callEvent(BorderChangeEvent(this, previousSize, prev, BorderTask.BorderAction.SET))
        }
        return prev
    }

    fun contains(block: Block): Boolean {
        return this.contains(block.x, block.z)
    }

    fun contains(entity: Entity): Boolean {
        return this.contains(entity.location)
    }

    fun contains(location: Location): Boolean {
        return this.contains(location.blockX, location.blockZ)
    }

    fun contains(x: Int, z: Int): Boolean {
        return x > physicalBounds.lowerX && x < physicalBounds.upperX && z > physicalBounds.lowerZ && z < physicalBounds.upperZ
    }

    fun fill() {
        val world = origin.world
        val xMin: Int = physicalBounds.lowerX
        val xMax: Int = physicalBounds.upperX
        val zMin: Int = physicalBounds.lowerZ
        val zMax: Int = physicalBounds.upperZ
        var tick = 0
        val chunksPerTick = 20
        var chunkZ = zMin shr 4

        while (chunkZ <= zMax shr 4) {
            val cz = chunkZ++
            Bukkit.getScheduler().runTaskLater(ScGameFramework.instance, Runnable {
                val chunk = world.getChunkAt(xMin shr 4, cz)
                // Use a while loop to iterate over z values
                var z = max(zMin.toDouble(), (cz shl 4).toDouble())
                val zEnd = min(zMax.toDouble(), (cz + 1 shl 4).toDouble())
                while (z < zEnd) {
                    this.fillAtXZ(world, chunk, xMin, z.toInt())
                    z++
                }
            }, (tick++ / chunksPerTick).toLong())

            Bukkit.getScheduler().runTaskLater(ScGameFramework.instance, Runnable {
                val chunk = world.getChunkAt(xMax shr 4, cz)
                // Use a while loop to iterate over z values
                var z = max((zMin + 1).toDouble(), (cz shl 4).toDouble())
                val zEnd = min((zMax + 1).toDouble(), (cz + 1 shl 4).toDouble())
                while (z < zEnd) {
                    this.fillAtXZ(world, chunk, xMax, z.toInt())
                    z++
                }
            }, (tick++ / chunksPerTick).toLong())
        }

        var chunkX = xMin shr 4
        while (chunkX <= xMax shr 4) {
            val cx = chunkX++
            Bukkit.getScheduler().runTaskLater(ScGameFramework.instance, Runnable {
                val chunk = world.getChunkAt(cx, zMin shr 4)
                // Use a while loop to iterate over x values
                var x = max((xMin + 1).toDouble(), (cx shl 4).toDouble())
                val xEnd = min((xMax + 1).toDouble(), (cx + 1 shl 4).toDouble())
                while (x < xEnd) {
                    this.fillAtXZ(world, chunk, x.toInt(), zMin)
                    x++
                }
            }, (tick++ / chunksPerTick).toLong())

            Bukkit.getScheduler().runTaskLater(ScGameFramework.instance, Runnable {
                val chunk = world.getChunkAt(cx, zMax shr 4)
                // Use a while loop to iterate over x values
                var x = max(xMin.toDouble(), (cx shl 4).toDouble())
                val xEnd = min(xMax.toDouble(), (cx + 1 shl 4).toDouble())
                while (x < xEnd) {
                    this.fillAtXZ(world, chunk, x.toInt(), zMax)
                    x++
                }
            }, (tick++ / chunksPerTick).toLong())
        }
    }


    private fun fillAtXZ(world: World, chunk: Chunk, x: Int, z: Int) {
        if (this.isWrapTerrain) {
            var y = world.getHighestBlockYAt(x, z)
            while (airBlocks[chunk.getBlock(x, y, z).type.id] && y > 0) {
                --y
            }
            y += this.height
            while (y >= 0) {
                chunk.getBlock(x, y, z).setTypeIdAndData(material.id, 0.toByte(), false)
                --y
            }
        } else {
            for (y in 0..(origin.blockY + this.height)) {
                chunk.getBlock(x, y, z).setTypeIdAndData(material.id, 0.toByte(), false)
            }
        }
    }

    fun correctLocation(location: Location): Location {
        val cuboid: Cuboid = this.physicalBounds.clone()
        var validX = location.blockX
        var validZ = location.blockZ
        var xAction: EnsureAction? = null
        var zAction: EnsureAction? = null
        if (location.blockX + 2 > cuboid.upperX) {
            xAction = EnsureAction.DECREASE
            validX = xAction.apply(cuboid.upperX, 4)
        } else if (location.blockX - 2 < cuboid.lowerX) {
            xAction = EnsureAction.INCREASE
            validX = xAction.apply(cuboid.lowerX, 4)
        }
        if (location.blockZ + 2 > cuboid.upperZ) {
            zAction = EnsureAction.DECREASE
            validZ = zAction.apply(cuboid.upperZ, 4)
        } else if (location.blockZ - 2 < cuboid.lowerZ) {
            zAction = EnsureAction.INCREASE
            validZ = zAction.apply(cuboid.lowerZ, 4)
        }
        var validY = location.world.getHighestBlockYAt(validX, validZ)
        var validLoc =
            Location(location.world, validX.toDouble() + 0.5, validY.toDouble() + 0.5, validZ.toDouble() + 0.5)
        var tries = 0
        while (!isSafe(validLoc) && tries++ < 30) {
            if (xAction != null) {
                validX = xAction.apply(validX, 1)
            }
            if (zAction != null) {
                validZ = zAction.apply(validZ, 1)
            }
            validY = location.world.getHighestBlockYAt(validX, validZ)
            validLoc =
                Location(location.world, validX.toDouble() + 0.5, validY.toDouble() + 0.5, validZ.toDouble() + 0.5)
        }
        validLoc.pitch = location.pitch
        validLoc.yaw = location.yaw
        return validLoc
    }

    fun setMaterial(material: Material): Border {
        this.material = material
        return this
    }

    fun setHeight(height: Int): Border {
        this.height = height
        return this
    }

    fun setWrapTerrain(wrapTerrain: Boolean): Border {
        this.isWrapTerrain = wrapTerrain
        return this
    }

    fun setBorderConfiguration(borderConfig: BorderConfig): Border {
        this.borderConfig = borderConfig
        return this
    }

    fun setParticle(particle: Effect?): Border {
        this.particle = particle
        return this
    }

    init {
        this.material = material ?: Material.BEDROCK
        this.physicalBounds = Cuboid(
            origin.clone()
                .add((size + 1).toDouble(), origin.world.maxHeight.toDouble() - origin.y, (size + 1).toDouble()),
            origin.clone().subtract((size + 1).toDouble(), origin.y, (size + 1).toDouble())
        )
        this.borderTask = BorderTask(this)
        BorderService.addBorder(this)
    }

    internal enum class EnsureAction {
        INCREASE,
        DECREASE;


        fun apply(previous: Int, amount: Int): Int {
            if (this == INCREASE) {
                return previous + amount
            }
            return previous - amount
        }
    }

    companion object {
        private val airBlocks = BooleanArray(256)

        private fun isSafe(location: Location): Boolean {
            return location.block.getRelative(BlockFace.DOWN).type.isSolid && location.block.isEmpty && location.block.getRelative(
                BlockFace.UP
            ).isEmpty
        }

        init {
            airBlocks[Material.LOG.id] = true
            airBlocks[Material.LOG_2.id] = true
            airBlocks[Material.LEAVES.id] = true
            airBlocks[Material.LEAVES_2.id] = true
            airBlocks[Material.HUGE_MUSHROOM_1.id] = true
            airBlocks[Material.HUGE_MUSHROOM_2.id] = true
            airBlocks[Material.SNOW.id] = true
            for (material in Material.entries) {
                if (!material.isBlock || material.isSolid) continue
                airBlocks[material.id] = true
            }
            airBlocks[Material.WATER.id] = false
            airBlocks[Material.STATIONARY_WATER.id] = false
            airBlocks[Material.LAVA.id] = false
            airBlocks[Material.STATIONARY_LAVA.id] = false
        }
    }
}