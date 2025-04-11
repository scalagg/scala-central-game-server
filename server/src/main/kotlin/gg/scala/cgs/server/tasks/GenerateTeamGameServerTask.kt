package gg.scala.cgs.server.tasks

import gg.scala.cgs.common.game.GameType
import gg.scala.cgs.common.packet.CgsPacketService
import gg.scala.cgs.common.packet.impl.RequestServerPacket
import gg.scala.commons.agnostic.sync.server.ServerContainer
import java.util.*

class GenerateTeamGameServerTask : TimerTask() {
    private var lastServerRequest = 0L

    override fun run() {
        val requestPassed = System.currentTimeMillis() - lastServerRequest
        if (requestPassed <= 20_000L) {
            println("Ignoring as team request was sent less than 20 seconds ago: $requestPassed ms")
            return
        }

        for (gameType in GameType.entries) {
            if (!gameType.autoCreateServers) continue

            val maxAvailable = gameType.maxTeamServers
            var availableGames = 0
            var onlineServers = 0
            var hasInGame = false

            val servers = ServerContainer.getServersInGroup(gameType.serverName)
            for (server in servers) {
                val gameStage = server.getMetadataValue<String>("game", "stageType")
                val teamSize = server.getMetadataValue<Int>("game", "gameTeamSize")

                if (gameStage == null || teamSize == null || teamSize != gameType.maxTeamSize) continue

                onlineServers++
                when (gameStage.lowercase()) {
                    "in_game" -> hasInGame = true
                    "setup", "pre_game", "voting", "starting" -> availableGames++
                }
            }

            val shouldCreate = onlineServers < maxAvailable || (availableGames == 0 && hasInGame)
            if (shouldCreate && availableGames < maxAvailable) {
                println("Generating team game for ${gameType.serverName}...")

                CgsPacketService.sendToAll(RequestServerPacket(gameType.serverName, gameType.maxTeamSize))

                lastServerRequest = System.currentTimeMillis()
            }
        }
    }
}
