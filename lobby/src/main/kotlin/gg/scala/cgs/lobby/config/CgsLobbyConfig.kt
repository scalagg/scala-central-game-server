package gg.scala.cgs.lobby.config

import gg.scala.commons.config.annotations.Config
import net.evilblock.cubed.util.CC
import org.bukkit.Location
import org.bukkit.Sound
import xyz.mkotb.configapi.Coloured
import xyz.mkotb.configapi.comment.Comment

/**
 * @author Topu
 * @date 11/16/2024
 */
@Config("config", crossSync = false)
object CgsLobbyConfig {

    @Coloured
    val title = "&6&lScala &7❘ &r%type%"

    val scoreboardLines = listOf(
        "&6In Lobby: &r%in_lobby%",
        "%queue_line%",
        "&6In Game: &r%in_game%",
        "%party%",
        "%queued%"
    )

    @Coloured
    val party = "&6Your Party: &r%members%/%max%"

    val queue = mapOf(
        "one" to "&6In Queue: &r%in_queue%",
        "multiple" to "&6%type% Queue: &r%amount%"
    )

    val queued = listOf(
        "",
        "&a&l&oQueued for game..."
    )

    val queues = mapOf(
        "regular" to mapOf(
            "name" to "Regular",
            "size" to 5,
            "required" to 0 // If under 1 it will default to size * 2
        )
    )

    val signs = mapOf(
        0 to mapOf(
            "location" to "world|0|100|0|IGNOREME", // We add a string so it doesn't deserialize it - this is here for an example only
            "server" to "Game-1"
        )
    )
}