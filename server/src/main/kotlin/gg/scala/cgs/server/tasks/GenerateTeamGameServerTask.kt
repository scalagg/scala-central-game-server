package gg.scala.cgs.server.tasks

import gg.scala.aware.thread.AwareThreadContext
import gg.scala.cgs.common.game.GameType
import gg.scala.cgs.common.packet.CgsPacketService
import gg.scala.commons.agnostic.sync.server.ServerContainer
import java.util.*

class GenerateTeamGameServerTask : TimerTask() {
    private var lastServerRequest = 0L

    override fun run() {
        val requestPassed = System.currentTimeMillis() - lastServerRequest
        if (requestPassed <= 20000L) {
            println("Ignoring as team request was sent less than 20 seconds ago: $requestPassed")
            return
        }

        for (value in GameType.entries) {
            if (!value.autoCreateServers) continue

            val maxAvailable: Int = value.maxTeamServers
            var availableGames = 0
            var onlineServers = 0

            var hasInGame = false

           for (server in ServerContainer.getServersInGroup("${value.serverName}")) {
               if (server == null || server.metadata == null || server.getMetadataValue<String>("game", "stageType") == null || /*!server.getMetadata().has("gameTeamSize")*/ server.getMetadataValue<Int>("game", "gameTeamSize") != null) continue
               if (server.getMetadataValue<Int>("game", "gameTeamSize") != value.maxTeamSize) continue

               onlineServers++

               val gameStage = server.getMetadataValue<String>("game", "stageType")

                if (gameStage.equals("IN_GAME")) hasInGame = true
                else if (gameStage.equals("setup") || gameStage.equals("pre_game") || gameStage.equals("voting") || gameStage.equals("starting")) availableGames++
            }

            if (onlineServers < value.maxTeamServers || (availableGames == 0 && hasInGame)) {
                if (availableGames < maxAvailable) {
                    println("Generating team game...")

                    CgsPacketService.buildMessage(
                        "request-server",
                        mapOf("serverName" to value.serverName,
                            "maxTeamSize" to value.maxTeamSize.toString())
                    ).publish(AwareThreadContext.SYNC)

                    lastServerRequest = System.currentTimeMillis()
                }
            }
        }
    }
}
