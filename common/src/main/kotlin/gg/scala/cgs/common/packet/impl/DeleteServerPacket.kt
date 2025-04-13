package gg.scala.cgs.common.packet.impl

import gg.scala.cgs.common.packet.Packet

class DeleteServerPacket(var serverName: String) : Packet {

    override fun onReceive() {
        TODO("Add logic to delete the docker container (prolly better to switch from using .sh files to the" +
                    "docker java library......)")
    }


}