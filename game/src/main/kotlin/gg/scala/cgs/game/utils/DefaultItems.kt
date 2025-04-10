package gg.scala.cgs.game.utils

import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.ChatColor
import org.bukkit.Material

class DefaultItems {

    companion object {
        @JvmStatic
        val returnToLobby = ItemBuilder
                .of(Material.INK_SACK)
                .data(14.toShort())
                .name(ChatColor.RED.toString() + "Return to Lobby")
                .build()
    }

}