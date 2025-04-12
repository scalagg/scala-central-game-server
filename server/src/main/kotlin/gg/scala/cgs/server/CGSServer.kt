package gg.scala.cgs.server

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.packet.CgsPacketService
import gg.scala.cgs.common.packet.impl.GameServerPacket
import gg.scala.cgs.common.server.TempServer
import gg.scala.cgs.server.tasks.GenerateGameServerTask
import gg.scala.cgs.server.tasks.GenerateTeamGameServerTask
import java.io.*
import java.util.*
import kotlin.system.exitProcess

class CGSServer {

    lateinit var instance: CGSServer
    private var running: Boolean = true
    private val broken: MutableList<TempServer> = ArrayList<TempServer>() // lol

    fun init() {
        instance = this
    }

    fun start() {
        println("[CGS] Loading CGS Packet Service...")
        CgsGameEngine.initPacketHandler()
        println("[CGS] Loaded CGS System Type: ${CgsGameEngine.currentSystemType()}.")
        println("[CGS] Loaded CGS Packet Service.")

        val delay = 10000L
        val timer = Timer()
        timer.scheduleAtFixedRate(GenerateGameServerTask(), delay, 5000L)
        timer.scheduleAtFixedRate(GenerateTeamGameServerTask(), delay, 5000L)

        Thread {
            while (running) {
                try {
                    Thread.sleep(500L)
                } catch (ignored: Exception) {
                }

                val toRemove: MutableList<TempServer> = ArrayList()

                for (server in CgsGameEngine.TEMP_SERVERS) {
                    val lastText = server.lastText
                    var lastDockerText = server.lastText

                    println("Running: ${server.isRunning()}")
                    if (!server.shouldRemove()) continue

                    try {
                        lastDockerText = server.getLastStringFromDocker()
                    } catch (ignored: IOException) {
                    }

                    if (!lastDockerText.equals("Shutdown auto update", ignoreCase = true)) {
                        if (lastText != lastDockerText) server.lastText = lastDockerText
                        println("$lastText - $lastDockerText")

                        if (lastText != lastDockerText) {
                            server.removedTime = System.currentTimeMillis()
                            continue
                        }
                    }


                    if (broken.contains(server)) continue

                    println("[CGS] Removing server '" + server.name + "'. (d: " + (System.currentTimeMillis() - server.startedAt) + ", r: " + server.isRunning() + ")")

                    toRemove.add(server)

                    try {
                        server.destroy()
                    } catch (ex: Exception) {
                        System.err.println("[CGS] Couldn't destroy '" + server.name + "'!")
                        ex.printStackTrace()
                        CgsPacketService.sendToAll(
                            GameServerPacket(
                                server.name,
                                "Failed to destroy '" + server.name + "': " + ex.message
                            )
                        )

                    }
                }

                try {
                    CgsGameEngine.TEMP_SERVERS.removeAll(toRemove)
                } catch (ex: Exception) {
                    broken.addAll(toRemove)
                    println("[CGS] Failed to remove some servers, marked them as broken. (" + toRemove.size + ", " + broken.size + ")")
                }
            }
        }.start()
        println("[CGS] Started CGS Server...")
    }
}
