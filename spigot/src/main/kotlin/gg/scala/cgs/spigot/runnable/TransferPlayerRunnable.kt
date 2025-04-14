package gg.scala.cgs.spigot.runnable

import gg.scala.cgs.common.packet.CgsPacketService
import gg.scala.cgs.spigot.packets.AttemptTransferPacket
import gg.scala.commons.agnostic.sync.server.ServerContainer
import gg.scala.commons.agnostic.sync.server.impl.GameServer
import gg.scala.commons.agnostic.sync.server.state.ServerState
import java.util.*

/**
 * @author Topu
 * @date 4/14/2025
 */
class TransferPlayerRunnable : Runnable {

    override fun run() {
        for (uuid in serverMap.keys) {
            val server: GameServer = ServerContainer.getServer(serverMap[uuid]!!) as GameServer
                ?: continue

            if (server == null) {
                serverMap.remove(uuid)
                continue
            }

            if (server.getPlayers()!!.contains(uuid)) {
                serverMap.remove(uuid)
                continue
            }

            if (server.state == ServerState.Loaded) {
                serverMap.remove(uuid)
                CgsPacketService.sendToAll(AttemptTransferPacket(uuid, server))
            }
        }
    }

    companion object {
        private val serverMap: MutableMap<UUID, String> = HashMap()
    }
}