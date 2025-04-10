package gg.scala.cgs.lobby

import gg.scala.cgs.lobby.queue.Queue
import gg.scala.cgs.lobby.server.GameServer
import gg.scala.commons.ExtendedScalaPlugin
import gg.scala.commons.core.plugin.*
import java.util.concurrent.ConcurrentHashMap

/**
 * @author Topu
 * @date 11/16/2024
 */
@Plugin(
    name = "ScGameLobby",
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
class ScGameLobby : ExtendedScalaPlugin()
{

    companion object
    {
        @JvmStatic
        lateinit var instance: ScGameLobby
    }

    val queues: MutableMap<String, Queue> = ConcurrentHashMap()
    val servers: MutableMap<String, GameServer> = ConcurrentHashMap()

}