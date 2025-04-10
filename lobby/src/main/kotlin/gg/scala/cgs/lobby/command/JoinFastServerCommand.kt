package gg.scala.cgs.lobby.command

import gg.scala.cgs.common.game.GameType
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandCompletion
import gg.scala.commons.acf.annotation.Description
import gg.scala.commons.acf.annotation.Syntax
import gg.scala.commons.agnostic.sync.ServerSync
import gg.scala.commons.agnostic.sync.server.ServerContainer
import gg.scala.commons.agnostic.sync.server.impl.GameServer
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bungee.BungeeUtil
import org.bukkit.entity.Player
import java.util.concurrent.TimeUnit

/**
 * @author Topu
 * @date 11/16/2024
 */
@AutoRegister
object JoinFastServerCommand : ScalaCommand() {

    fun gameServer(id: String) = ServerContainer
        .allServers<GameServer>()
        .find {
            it.id.equals(id, true)
        }

    @CommandAlias("joinfastserver")
    @CommandCompletion("@servers")
    @Syntax("<server>")
    fun join(
        player: Player,
        server: String
    ) {
        if (server == "   ") return

        player.sendMessage("${CC.GOLD}Attempting to send you to ${CC.WHITE}$server${CC.GOLD}...")

        Thread {
            var attempt = 0
            while (attempt < 4) {
                if (!player.isOnline) return@Thread

                try {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(3))
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

                if (gameServer(server) != null) {
                        player.sendMessage(
                            "${CC.RED}Failed to connect you to ${CC.WHITE}${gameServer(server)?.id}${CC.RED}."
                        )
                        return@Thread
                }

                if (attempt == 3) {
                    player.sendMessage(
                        "${CC.RED}Failed to connect you to ${CC.WHITE}$server${CC.RED}. Connection timeout"
                    )
                    return@Thread
                }

                player.sendMessage("${CC.GOLD}Connecting to ${CC.WHITE}$server${CC.GOLD}...")
                if (canJoin(player, server)) {
                    BungeeUtil.sendToServer(player, server)
                    return@Thread
                }

                attempt++
            }
        }.start()
    }

    private fun canJoin(player: Player, server: String): Boolean {

        if (GameType.values().none {
                server.startsWith("${it.serverName.lowercase()}-", ignoreCase = true) &&
                        !server.endsWith("lobby", ignoreCase = true)
            }) return false

        if (gameServer(server) != null) return false
        if (player.hasPermission("basic.staff") && gameServer(server)?.getWhitelisted() == true) return true
        return gameServer(server)?.getWhitelisted() == false
    }
}
