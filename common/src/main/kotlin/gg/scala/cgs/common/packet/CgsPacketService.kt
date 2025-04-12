package gg.scala.cgs.common.packet

import com.google.gson.Gson
import redis.clients.jedis.JedisPool

/**
 * @author Topu
 * @date 11/16/2024
 */
class CgsPacketService private constructor() {
    init {
        throw UnsupportedOperationException("This is a utility class and cannot be instantiated")
    }

    companion object {
        private var jedisPool: JedisPool? = null
        private var jedisChannel: String? = null

        fun init(pool: JedisPool, channel: String?) {
            jedisPool = pool
            jedisChannel = channel
            connectToServer(pool)
        }

        private fun connectToServer(connectTo: JedisPool) {
            println("[CGS] Connecting to CGS Packet System.")
            val subscribeThread = Thread({
                while (true) {
                    try {
                        val jedis = connectTo.resource
                        var throwable: Throwable? = null
                        try {
                            val pubSub = PacketPubSub()
                            jedis!!.subscribe(pubSub, jedisChannel)
                        } catch (pubSub: Throwable) {
                            throwable = pubSub
                            throw pubSub
                        } finally {
                            if (jedis == null) continue
                            if (throwable != null) {
                                try {
                                    jedis.close()
                                } catch (pubSub: Throwable) {
                                    throwable.addSuppressed(pubSub)
                                }
                                continue
                            }
                            jedis.close()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }, "CGS - Packet Subscribe Thread")
            subscribeThread.isDaemon = true
            subscribeThread.start()
        }

        fun sendToAll(packet: Packet) {
            send(packet, jedisPool!!)
        }

        fun send(packet: Packet, sendOn: JedisPool) {
            Thread {
                sendOn.resource.use { jedis ->
                    val encodedPacket: String =
                        packet.javaClass.getName() + "||" + Gson().toJson(packet)
                    jedis.publish(jedisChannel, encodedPacket)
                }
            }.start()
        }
    }
}