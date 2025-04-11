package gg.scala.cgs.common.packet

import com.google.gson.Gson
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
        if (packetMessageSplit == -1) return

        val packetClassStr = message.substring(0, packetMessageSplit)
        val messageJson = message.substring(packetMessageSplit + 2)

        val packetClass: Class<*> = try {
            Class.forName(packetClassStr)
        } catch (e: ClassNotFoundException) {
            println("Could not find class for packet: $packetClassStr")
            return
        }

        try {
            val packetObj = gson.fromJson(messageJson, packetClass)
            if (packetObj is Packet) {
                packetObj.onReceive()
            } else {
                println("Received object is not a Packet: $packetObj")
            }
        } catch (e: Exception) {
            println("Failed to deserialize packet: $packetClassStr")
            e.printStackTrace()
        }
    }
}