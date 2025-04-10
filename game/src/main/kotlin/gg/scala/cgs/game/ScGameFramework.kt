package gg.scala.cgs.game

import gg.scala.cgs.game.game.Game
import gg.scala.cgs.game.nametag.FrameworkTag
import gg.scala.cgs.game.setting.SettingContainer
import gg.scala.cgs.game.visibility.CgsGameVisibility
import gg.scala.commons.ExtendedScalaPlugin
import gg.scala.commons.annotations.container.ContainerDisable
import gg.scala.commons.annotations.container.ContainerEnable
import gg.scala.commons.core.plugin.*
import net.evilblock.cubed.nametag.NametagHandler
import net.evilblock.cubed.visibility.VisibilityHandler
import org.bukkit.Bukkit
import java.util.concurrent.TimeUnit

/**
 * @author Topu
 * @date 11/16/2024
 */
@Plugin(
    name = "ScGameFramework",
    version = "%remote%/%branch%/%id%"
)
@PluginAuthor("Scala")
@PluginWebsite("https://scala.gg")
@PluginDependencyComposite(
    PluginDependency("scala-commons"),
    PluginDependency("Lemon"),
    PluginDependency("Parties"),
    PluginDependency("cloudsync"),
    PluginDependency("CoreGameExtensions", soft = true),
    PluginDependency("LunarClient-API", soft = true),
    PluginDependency("SlimeWorldManager", soft = true)
)
class ScGameFramework : ExtendedScalaPlugin()
{

    companion object
    {
        @JvmStatic
        lateinit var instance: ScGameFramework
    }
    var game: Game? = null
        set(value) {
            logger.info("Setting Game...")
            field = value
            field?.initialize()
            SettingContainer.load()
        }

    @ContainerEnable
    fun containerEnable() {
        instance = this
        saveDefaultConfig()

        // Register providers and handlers
        NametagHandler.registerProvider(FrameworkTag())
        VisibilityHandler.registerAdapter("CGS Visibility Handler", CgsGameVisibility())

        // Scheduled tasks
        scheduleRepeatingTask(5L) { game!!.tick() }
        scheduleRepeatingTask(20L) { game!!.fullTick() }
        scheduleRepeatingTask(20L) {
            game!!.let {
                if (it.gameStartedAt != -1L) {
                    val timeElapsed = System.currentTimeMillis() - it.gameStartedAt
                    if (server.onlinePlayers.isEmpty() && timeElapsed >= TimeUnit.MINUTES.toMillis(5)) {
                        Bukkit.shutdown()
                    }
                }
            }
        }
    }

    @ContainerDisable
    fun containerDisable() {
        // Add necessary cleanup logic if required
    }

    /**
     * Helper function to schedule a repeating task
     */
    private fun scheduleRepeatingTask(interval: Long, task: () -> Unit) {
        Bukkit.getScheduler().runTaskTimer(this, Runnable(task), interval, interval)
    }
}