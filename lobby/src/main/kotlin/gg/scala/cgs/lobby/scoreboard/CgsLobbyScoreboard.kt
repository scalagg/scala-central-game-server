package gg.scala.cgs.lobby.scoreboard

import gg.scala.cgs.lobby.ScGameLobby
import gg.scala.cgs.lobby.config.CgsLobbyConfig
import gg.scala.cgs.spigot.CgsSpigotPlugin
import gg.scala.parties.service.PartyService
import net.evilblock.cubed.scoreboard.ScoreboardAdapter
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

/**
 * @author Topu
 * @date 11/16/2024
 */
abstract class CgsLobbyScoreboard : ScoreboardAdapter() {

    private var lastCache: Long = 0L
    private val queueMap = mutableMapOf<String, Int>()
    private var inLobby = 0
    private var inGame = 0
    private var inQueue = 0
    private var queues = 0

    override fun getTitle(player: Player): String {
        return CgsLobbyConfig.title.replace("%type%", CgsSpigotPlugin.gameType.displayName)
    }

    override fun getLines(board: LinkedList<String>, player: Player) {
        val lines = mutableListOf<String>()
        if (lastCache == 0L || lastCache - System.currentTimeMillis() <= -1500L) {
            inLobby = Bukkit.getOnlinePlayers().size
            inQueue = ScGameLobby.instance.queues.values.sumOf { it.getQueued() }
            inGame = if (ScGameLobby.instance.servers.isEmpty()) 0 else ScGameLobby.instance.servers.values
                .filter { it.status != null }
                .flatMap { listOf(it.status!!.playerCount) }
                .sum()
            lastCache = System.currentTimeMillis()

            queues = ScGameLobby.instance.queues.size
            queueMap.clear()
            for (queue in ScGameLobby.instance.queues.values) {
                queueMap[queue.gameType] = queue.getQueued()
            }
        }

        val party = PartyService.findPartyByUniqueId(player)
        val queue = ScGameLobby.instance.queues.values.find { it.contains(player) }

        for (line in CgsLobbyConfig.scoreboardLines) {
            when {
                line.equals("%party%", ignoreCase = true) -> {
                    if (party == null) continue
                    lines.add(
                        CgsLobbyConfig.party
                            .replace("%members%", (party.members.size + 1).toString())
                            .replace("%max%", CgsSpigotPlugin.gameType.maxPartySize.toString())
                    )
                }

                line.equals("%queued%", ignoreCase = true) -> {
                    if (queue == null) continue
                    lines.addAll(CgsLobbyConfig.queued)
                }

                line.equals("%queue_line%", ignoreCase = true) -> {
                    if (queues <= 0) continue

                    if (queues == 1) {
                        lines.add(
                            CgsLobbyConfig.queue["one"]!!
                                .replace("%in_queue%", inQueue.toString())
                        )
                    } else {
                        for (value in ScGameLobby.instance.queues.values) {
                            lines.add(
                                CgsLobbyConfig.queue["multiple"]!!
                                    .replace("%type%", value.gameType)
                                    .replace("%amount%", queueMap[value.gameType].toString())
                                    .replace("%in_queue%", queueMap[value.gameType].toString())
                            )
                        }
                    }
                }

                else -> {
                    lines.add(
                        line
                            .replace("%in_lobby%", inLobby.toString())
                            .replace("%in_game%", inGame.toString())
                    )
                }
            }
        }
    }
}