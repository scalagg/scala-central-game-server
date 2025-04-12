package gg.scala.cgs.common.packet

import com.google.gson.Gson
import gg.scala.cgs.common.util.SystemType
import redis.clients.jedis.JedisPubSub

/**
 * @author Topu
 * @date 4/11/2025
 */
internal class PacketPubSub : JedisPubSub() {

    private val gson = Gson()

    override fun onMessage(channel: String, message: String) {
        println("[CGS] $channel: Received packet: $message")

        val packetMessageSplit = message.indexOf("||")
        val packetClassStr = message.substring(0, packetMessageSplit)
        val messageJson = message.substring(packetMessageSplit + "||".length)
        val packageLayout = "gg.scala.cgs."

        var packetClass: Class<*>? = null

        for (value in SystemType.entries) {
            try {
                packetClass = Class.forName(
                    packetClassStr.replace(
                        "${packageLayout}common",
                        packageLayout + value.name.lowercase()
                    )
                )
                break
            } catch (_: ClassNotFoundException) {
            }
        }

        if (packetClass == null) {
            try {
                packetClass = Class.forName(packetClassStr)
            } catch (_: Exception) {
                return
            }
        }

        val packet = gson.fromJson(messageJson, packetClass) as Packet
        packet.onReceive()
    }
}