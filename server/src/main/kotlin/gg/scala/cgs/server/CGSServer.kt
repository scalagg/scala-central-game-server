package gg.scala.cgs.server

import gg.scala.cgs.common.CgsGameEngine
import gg.scala.cgs.common.packet.CgsPacketService
import gg.scala.cgs.common.server.TempServer
import gg.scala.cgs.server.tasks.GenerateGameServerTask
import gg.scala.cgs.server.tasks.GenerateTeamGameServerTask
import java.io.*
import java.util.*
import kotlin.system.exitProcess

class CGSServer {

    var instance: CGSServer? = null
    private var running: Boolean = true
    private val broken: MutableList<TempServer> = java.util.ArrayList<TempServer>() // lol

    fun init() {
        instance = this
    }

    fun start() {
        println("[CGS] Loading Config...")
        val cgsConfigFile = File("config.yml")

        if (!cgsConfigFile.exists()) {
            println("[CGS] CGS config not found...")
            exitProcess(0)
        }
        println("[CGS] Loaded Config.")

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
                        CgsPacketService.buildMessage(
                            "game-server",
                            mapOf("serverName" to server.name,
                                "message" to "Failed to destroy '" + server.name + "': " + ex.message)
                        ).publish()
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
