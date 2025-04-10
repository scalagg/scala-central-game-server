package gg.scala.cgs.game.utils

import net.minecraft.server.v1_8_R3.PlayerList
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_8_R3.CraftServer

object BukkitUtils {

    val playerListField = CraftServer::class.java.getDeclaredField("playerList")
    val maxPlayersField = PlayerList::class.java.getDeclaredField("maxPlayers")

    init {
        playerListField.isAccessible = true
        maxPlayersField.isAccessible = true
    }

    fun setMaxPlayers(amount: Int) {
        val playerList = playerListField[Bukkit.getServer()]
        maxPlayersField.set(playerList, amount)
    }

}