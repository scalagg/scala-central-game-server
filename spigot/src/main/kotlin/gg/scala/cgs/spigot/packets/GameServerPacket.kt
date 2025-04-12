package gg.scala.cgs.spigot.packets

import gg.scala.cgs.common.packet.Packet
import gg.scala.lemon.util.QuickAccess
import org.bukkit.ChatColor

/**
 * @author Topu
 * @date 4/12/2025
 */
class GameServerPacket : Packet {
    var serverName: String? = null
    var message: String? = null

    constructor()

    constructor(serverName: String?, message: String?) {
        this.serverName = serverName
        this.message = message
    }

    override fun onReceive() {
        QuickAccess.sendGlobalBroadcast(
            ChatColor.DARK_GRAY.toString() + "[" + ChatColor.YELLOW + "Server Monitor" + ChatColor.DARK_GRAY + "] " + ChatColor.WHITE + message,
            "cgs.admin"
        )
    }
}
