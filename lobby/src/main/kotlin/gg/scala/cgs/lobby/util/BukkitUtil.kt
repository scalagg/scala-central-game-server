package gg.scala.cgs.lobby.util

import com.google.common.io.ByteStreams
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin


/**
 * @author Topu
 * @date 11/16/2024
 */
object BukkitUtil {

    fun sendPlayer(plugin: JavaPlugin?, player: Player, serverName: String?) {
        try {
            Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord")
        } catch (ex: Exception) {
        }

        try {
            val output = ByteStreams.newDataOutput()

            output.writeUTF("Connect")
            output.writeUTF(serverName)

            player.sendPluginMessage(plugin, "BungeeCord", output.toByteArray())
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}