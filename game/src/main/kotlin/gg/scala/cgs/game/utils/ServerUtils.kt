package gg.scala.cgs.game.utils

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import gg.scala.cgs.game.ScGameFramework
import gg.scala.lemon.util.QuickAccess
import java.util.*
import kotlin.math.max

object ServerUtils {

    fun getVoteWeight(player: Player): Int {
        if (!player.hasMetadata("voteWeight")) return 1
        return max(1, player.getMetadata("voteWeight")[0].asInt())
    }

    fun getFixedServerName(): String {
        val serverName = Bukkit.getServerName()
        val game = ScGameFramework.instance.game ?: return serverName

        return getFixedServerName(game.gameName)
    }

    fun getFixedServerName(gameType: String): String {
        var serverName = Bukkit.getServerName()

        serverName = getFixed(gameType, serverName, "Bunkers")
        serverName = getFixed(gameType, serverName, "PotSG")
        serverName = getFixed(gameType, serverName, "Meetup")
        serverName = getFixed(gameType, serverName, "Bedwars")

        return serverName
    }

    @Suppress("NAME_SHADOWING")
    private fun getFixed(gameType: String, serverName: String, ruleName: String): String {
        var serverName = serverName
        if (!serverName.toLowerCase().startsWith(ruleName.toLowerCase())) serverName =
            ruleName + (if (serverName.startsWith("-")) "" else "-") + serverName
        return serverName
    }

    fun getName(player: Player) = getName(player.uniqueId)
    fun getName(uuid: UUID) = QuickAccess.nameOrConsole(uuid)

}