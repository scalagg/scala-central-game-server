package gg.scala.cgs.common.packet.impl

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.packet.CgsPacketService
import gg.scala.cgs.common.packet.Packet
import gg.scala.commons.agnostic.sync.ServerSync

/**
 * @author Topu
 * @date 4/11/2025
 */
class RequestServerPacket : Packet {
    var serverName: String? = null
    var serverGroup: String? = null
    var teamSize: Int = 1
    var gameType: String = "Regular"

    constructor()

    constructor(serverGroup: String?) {
        this.serverGroup = serverGroup
    }

    constructor(serverName: String?, serverGroup: String?) {
        this.serverName = serverName
        this.serverGroup = serverGroup
    }

    constructor(serverGroup: String?, teamSize: Int) {
        this.serverGroup = serverGroup
        this.teamSize = teamSize
    }

    constructor(serverGroup: String?, teamSize: Int, gameType: String) {
        this.serverGroup = serverGroup
        this.teamSize = teamSize
        this.gameType = gameType
    }

    constructor(serverName: String?, serverGroup: String?, teamSize: Int) {
        this.serverName = serverName
        this.serverGroup = serverGroup
        this.teamSize = teamSize
    }

    constructor(serverName: String?, serverGroup: String?, teamSize: Int, gameType: String) {
        this.serverName = serverName
        this.serverGroup = serverGroup
        this.teamSize = teamSize
        this.gameType = gameType
    }

    override fun onReceive() {
        println("[Game Manager] Detected a request for a server with the group: " + this.serverGroup)

        var generatedName = serverName + "-" + ServerSync.getLocalGameServer().region.name.uppercase()
        if (serverName == null) generatedName =
            serverGroup + "-" + CgsGameEngine.findAvailableServerId(serverGroup!!) + "-" + ServerSync.getLocalGameServer().region.name.uppercase()
                .toUpperCase()

        println("generatedName = $generatedName, serverGroup = $serverGroup, teamSize = $teamSize, gameType = $gameType")

        try {
            CgsGameEngine.createServer(generatedName, this.serverGroup!!, teamSize, gameType)
        } catch (ex: Exception) {
            ex.printStackTrace()
            CgsPacketService.sendToAll(
                GameServerPacket(
                    generatedName,
                    "Failed to create '" + generatedName + "': " + ex.message
                )
            )
        }
    }
}