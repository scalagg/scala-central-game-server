package gg.scala.cgs.game.server

import gg.scala.cgs.game.ScGameFramework
import gg.scala.cgs.spigot.CgsSpigotPlugin
import gg.scala.commons.agnostic.sync.ServerSync

class FrameworkImplementer {

    fun serverData() {
        val game = ScGameFramework.instance.game

        ServerSync.getLocalGameServer()
            .setMetadata(
                "game", "game-type", CgsSpigotPlugin.gameType!!.name
            )
        ServerSync.getLocalGameServer()
            .setMetadata(
                "game", "stage", game?.gameStage?.displayName ?: "Loading"
            )
        ServerSync.getLocalGameServer()
            .setMetadata(
                "game", "stage-type", game?.gameStage?.name ?: "Loading"
            )
        ServerSync.getLocalGameServer()
            .setMetadata(
                "game", "game-type", game?.gameType ?: "Loading"
            )
        ServerSync.getLocalGameServer()
            .setMetadata(
                "game", "map", game?.getActiveMapName().toString()
            )
        ServerSync.getLocalGameServer()
            .setMetadata(
                "game", "game-time", (game?.playingSeconds ?: 0).toString()
            )
        ServerSync.getLocalGameServer()
            .setMetadata(
                "game", "req-players", (game?.playerSettings?.minPlayers ?: 0).toString()
            )
        ServerSync.getLocalGameServer()
            .setMetadata(
                "game", "max-players", (game?.playerSettings?.maxPlayers ?: 0).toString()
            )
        ServerSync.getLocalGameServer()
            .setMetadata(
                "game", "players", (game?.getPlayerCount() ?: 0).toString()
            )
        ServerSync.getLocalGameServer()
            .setMetadata(
                "game", "spectators", (game?.getSpectatorCount() ?: 0).toString()
            )

        return
    }

}