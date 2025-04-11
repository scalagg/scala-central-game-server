package gg.scala.cgs.server.tasks

import gg.scala.aware.thread.AwareThreadContext
import gg.scala.cgs.common.game.GameType
import gg.scala.cgs.common.packet.CgsPacketService
import gg.scala.commons.agnostic.sync.server.ServerContainer
import java.util.TimerTask

class GenerateGameServerTask : TimerTask() {
    private var lastServerRequest = 0L

    override fun run() {
        val requestPassed = System.currentTimeMillis() - lastServerRequest
        if (requestPassed <= 20000L) {
            println("Ignoring as request was sent less than 20 seconds ago: $requestPassed")
            return
        }

        for (value in GameType.entries) {
            if (!value.autoCreateServers) continue

            val maxAvailable = 4
            var availableGames = 0
            var onlineServers = 0

            var hasInGame = false

            for (server in ServerContainer.getServersInGroup("${value.serverName}")) {
                if (server == null || server.metadata == null || server.getMetadataValue<String>("game", "stageType") == null || /*!server.getMetadata().has("gameTeamSize")*/ server.getMetadataValue<Int>("game", "gameTeamSize") != null) continue
                if (server.getMetadataValue<Int>("game", "gameTeamSize") != 1) continue

                onlineServers++

                val gameStage = server.getMetadataValue<String>("game", "stageType")

                if (gameStage.equals("IN_GAME")) hasInGame = true
                else if (gameStage.equals("setup") || gameStage.equals("pre_game") || gameStage.equals("voting") || gameStage.equals("starting")) availableGames++
            }

            if (onlineServers < 4 || (availableGames == 0 && hasInGame)) {
                if (availableGames < maxAvailable) {
                    println("Generate ffa game...")

                    CgsPacketService.buildMessage(
                        "request-server",
                        mapOf("serverName" to value.serverName)
                    ).publish(AwareThreadContext.SYNC)

                    lastServerRequest = System.currentTimeMillis()
                }
            }

        }
    }
}
