package gg.scala.cgs.lobby.server

import gg.scala.commons.agnostic.sync.server.ServerContainer
import gg.scala.commons.agnostic.sync.server.impl.GameServer

/**
 * @author Topu
 * @date 11/16/2024
 */
data class GameServer(
    val serverId: String,
    var serverDisplayName: String = "Loading",
    var status: ServerStatus? = null
) {

    private fun server(id: String) = ServerContainer
        .allServers<GameServer>()
        .find {
            it.id.equals(id, true)
        }

    fun isAvailable(): Boolean {
        val status = status ?: return false
        return status.playerCount < status.maxPlayers && listOf("PRE_GAME", "VOTING", "STARTING").contains(status.gameStageType)
    }

    fun isGameType(type: String): Boolean {
        return status?.gameType?.equals(type, ignoreCase = true) ?: false
    }

    fun fetch() {
        val server = server(serverId) ?: return run {
            serverDisplayName = serverId
            status = null
        }

        if (server == null) {
            serverDisplayName = serverId
            status = null
            return
        }

        serverDisplayName = server.id

        val players = server.getPlayersCount()!!
        val stage = server.getMetadata("stage", "")?.toString() ?: "Unknown"
        val stageType = server.getMetadata("stageType", "")?.toString() ?: "UNKNOWN"
        val type = server.getMetadata("type", "")?.toString() ?: "Unknown"
        val min = server.getMetadata("reqPlayers", "")?.toString()?.toInt() ?: 6
        val max = server.getMetadata("maxPlayers", "")?.toString()?.toInt() ?: 12

        status = ServerStatus(players, stage, stageType, type, min, max)
    }

    data class ServerStatus(
        val playerCount: Int,
        val gameStage: String,
        val gameStageType: String,
        val gameType: String,
        val requiredPlayers: Int,
        val maxPlayers: Int
    )
}
