package gg.scala.cgs.common.packet

import gg.scala.aware.AwareBuilder
import gg.scala.aware.annotation.Subscribe
import gg.scala.aware.codec.codecs.interpretation.AwareMessageCodec
import gg.scala.aware.message.AwareMessage
import gg.scala.cgs.common.CgsGameEngine
import gg.scala.flavor.service.Close
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service

/**
 * @author Topu
 * @date 11/16/2024
 */
@Service
object CgsPacketService {

    val aware by lazy {
        AwareBuilder
            .of<AwareMessage>("cgs:backbone")
            .codec(AwareMessageCodec)
            .build()
    }

    @Configure
    fun configure()
    {
        aware.listen(this)
        aware.connect()
            .toCompletableFuture()
            .join()
    }

    @Close
    fun close()
    {
        aware.shutdown()
    }

    @Subscribe("request-server")
    fun onRequestServer(message: AwareMessage)
    {
        val serverGroup = message.retrieve("serverGroup") as? String ?: return
        val serverName = message.retrieve("serverName") as? String
        val teamSize = message.retrieve("teamSize") as? Int ?: 1
        val gameType = message.retrieve("gameType") as? String ?: "Regular"

        println("[CGS] Detected a request for a server with group: $serverGroup")

        val generatedName = serverName ?: "${serverGroup}-${CgsGameEngine.findAvailableServerId(serverGroup)}"

        println("generatedName = $generatedName, serverGroup = $serverGroup, teamSize = $teamSize, gameType = $gameType")

        try {
            CgsGameEngine.createServer(generatedName, serverGroup, teamSize, gameType)

            val successMessage = mapOf(
                "serverName" to generatedName,
                "serverGroup" to serverGroup,
                "teamSize" to teamSize.toString(),
                "gameType" to gameType
            )
            buildMessage("server-creation-success", successMessage).publish()

            buildMessage(
                "game-server",
                mapOf(
                    "serverName" to generatedName,
                    "message" to "Server created successfully."
                )
            ).publish()

        } catch (ex: Exception) {
            ex.printStackTrace()
            val failureMessage = mapOf(
                "serverName" to generatedName,
                "message" to "Failed to create '$generatedName': ${ex.message}"
            )
            buildMessage("server-creation-failure", failureMessage).publish()

            buildMessage(
                "game-server",
                mapOf(
                    "serverName" to generatedName,
                    "message" to "Server creation failed: ${ex.message}"
                )
            ).publish()
        }
    }

    @Subscribe("game-server")
    fun onGameServer(message: AwareMessage)
    {
        val serverName = message.retrieve("serverName") as? String ?: return
        val messageContent = message.retrieve("message") as? String ?: return

        println("Received message for server $serverName: $messageContent")
    }

    fun buildMessage(
        packet: String,
        message: Map<String, String>
    ): AwareMessage
    {
        return AwareMessage.of(
            packet, aware
        ).apply {
            for (entry in message)
            {
                this.assign(entry.key, entry.value)
            }
        }
    }
}