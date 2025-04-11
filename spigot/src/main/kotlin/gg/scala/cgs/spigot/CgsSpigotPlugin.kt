package gg.scala.cgs.spigot

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.game.GameType
import gg.scala.cgs.spigot.config.CgsSpigotConfig
import gg.scala.commons.ExtendedScalaPlugin
import gg.scala.commons.agnostic.sync.ServerSync
import gg.scala.commons.agnostic.sync.server.ServerContainer
import gg.scala.commons.annotations.container.ContainerEnable
import gg.scala.commons.core.plugin.*
import org.bukkit.Bukkit
import java.util.*
import java.util.stream.Collectors

/**
 * @author Topu
 * @date 11/16/2024
 */
@Plugin(
    name = "cgs-spigot",
    version = "%remote%/%branch%/%id%"
)
@PluginAuthor("Scala")
@PluginWebsite("https://scala.gg")
@PluginDependencyComposite(
    PluginDependency("scala-commons"),
    PluginDependency("Lemon"),
    PluginDependency("Parties"),
    PluginDependency("cloudsync")
)
class CgsSpigotPlugin : ExtendedScalaPlugin() {

    companion object {
        @JvmStatic
        lateinit var instance: CgsSpigotPlugin

        @JvmStatic
        var gameType: GameType? = null
        @JvmStatic
        val season = 1
    }

    val config: CgsSpigotConfig
        get() = config()

    @ContainerEnable
    fun containerEnable() {
        instance = this
        saveDefaultConfig()

        CgsGameEngine.initPacketHandler()

        try {
            gameType = GameType.valueOf(getConfig().getString("game-type").uppercase(Locale.getDefault()))
        } catch (ex: Exception) {
            Bukkit.getLogger().severe(
                "Invalid game-type in config! Valid types: " + Arrays.stream(GameType.entries.toTypedArray())
                    .map { obj: Enum<*> -> obj.name }
                    .collect(Collectors.joining(", ")))
            Bukkit.shutdown()
        }

        if (ServerSync.getLocalGameServer().groups.contains("hub")) {
            gameType = null
        }
    }
}