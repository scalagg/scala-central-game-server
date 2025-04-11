package gg.scala.cgs.common.packet.impl

import gg.scala.cgs.common.packet.Packet

/**
 * @author Topu
 * @date 4/11/2025
 */
class GameServerPacket : Packet {
    var serverName: String = ""
    var message: String = ""

    constructor()

    constructor(serverName: String, message: String) {
        this.serverName = serverName
        this.message = message
    }

    override fun onReceive() {
    }
}
