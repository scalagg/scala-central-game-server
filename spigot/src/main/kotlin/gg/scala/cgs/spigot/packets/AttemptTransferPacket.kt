package gg.scala.cgs.spigot.packets

import gg.scala.cgs.common.packet.Packet
import gg.scala.commons.agnostic.sync.ServerSync
import gg.scala.commons.agnostic.sync.server.AbstractServer
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bungee.BungeeUtil
import org.bukkit.Bukkit
import java.util.*


/**
 * @author Topu
 * @date 4/14/2025
 */
class AttemptTransferPacket(
    private val uuid: UUID? = null,
    private val server: AbstractServer? = null
): Packet {

    override fun onReceive() {
        if (server?.id == ServerSync.getLocalGameServer().id) return

        val player = Bukkit.getPlayer(uuid) ?: return

        player.sendMessage("${CC.GREEN}Your private server is ready - attempting to transfer you now.")
        BungeeUtil.sendToServer(player, server!!.id)
    }
}