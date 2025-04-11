package gg.scala.cgs.server.tasks

import gg.scala.cgs.common.game.GameType
import gg.scala.cgs.common.packet.CgsPacketService
import gg.scala.cgs.common.packet.impl.RequestServerPacket
import gg.scala.commons.agnostic.sync.server.ServerContainer
import java.util.*

class GenerateGameServerTask : TimerTask() {
    private var lastServerRequest = 0L

    override fun run() {
        val requestPassed = System.currentTimeMillis() - lastServerRequest
        if (requestPassed <= 20_000L) {
            println("Ignoring as request was sent less than 20 seconds ago: $requestPassed ms ago")
            return
        }

        for (gameType in GameType.entries) {
            if (!gameType.autoCreateServers) continue

            val maxAvailable = 4
            var availableGames = 0
            var onlineServers = 0
            var hasInGame = false

            val servers = ServerContainer.getServersInGroup(gameType.serverName)
            for (server in servers) {
                val gameStage = server.getMetadataValue<String>("game", "stageType")
                val teamSize = server.getMetadataValue<Int>("game", "gameTeamSize")

                if (gameStage == null || teamSize == null || teamSize != 1) continue

                onlineServers++
                when (gameStage.lowercase()) {
                    "in_game" -> hasInGame = true
                    "setup", "pre_game", "voting", "starting" -> availableGames++
                }
            }

            val shouldCreate = onlineServers < 4 || (availableGames == 0 && hasInGame)
            if (shouldCreate && availableGames < maxAvailable) {
                println("Generating FFA game for ${gameType.serverName}...")

                CgsPacketService.sendToAll(RequestServerPacket(gameType.serverName))

                lastServerRequest = System.currentTimeMillis()
            }
        }
    }
}
