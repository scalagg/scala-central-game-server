package gg.scala.cgs.lobby.menu

import com.cryptomorin.xseries.XMaterial
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import gg.scala.cgs.lobby.server.GameServer
import gg.scala.commons.agnostic.sync.server.ServerContainer
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.time.TimeUtil
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView

/**
 * @author Topu
 * @date 11/18/2024
 */
class SpectateButton(
    private val gameServer: GameServer
) : Button() {

    private fun server(id: String) = ServerContainer
        .allServers<gg.scala.commons.agnostic.sync.server.impl.GameServer>()
        .find {
            it.id.equals(id, true)
        }

    override fun getName(player: Player): String {
        return "${CC.LIGHT_PURPLE}${gameServer.serverDisplayName}"
    }

    override fun getDescription(player: Player): List<String> {
        val lore = mutableListOf<String>()

        val status = gameServer.status ?: return listOf("${CC.RED}Failed to load.")
        if (server(gameServer.serverId) == null) {
            lore.add("${CC.RED}Failed to retrieve match data...")
            return lore
        }

        lore.add("${CC.GRAY}${CC.STRIKE_THROUGH}${"-".repeat(15)}")
        lore.add("${CC.GOLD}Players: ${CC.RESET}${status.playerCount}/${status.maxPlayers}")
        lore.add(
            "${CC.GOLD}Game Time: ${CC.RESET}" +
                    (if (server(gameServer.serverId)?.metadata != null) {
                        TimeUtil.formatIntoMMSS(server(gameServer.serverId)?.getMetadataValue<String>("game", "gameTime")?.toInt() ?: 0)
                    } else {
                        "00:00"
                    })
        )

        if (server(gameServer.serverId)?.getMetadataValue<String>("game", "gameLobbyLore") != null) {
            lore.add("${CC.GRAY}${CC.STRIKE_THROUGH}${"-".repeat(15)}")
            val providedLore: List<String> = Gson().fromJson(
                server(gameServer.serverId)?.getMetadataValue<String>("game", "gameLobbyLore"),
                object : TypeToken<List<String>>() {}.type
            )
            lore.addAll(providedLore)
        } else if (player.hasPermission("basic.staff")) {
            lore.add("${CC.GRAY}${CC.STRIKE_THROUGH}${"-".repeat(15)}")
            lore.add("${CC.RED}No game information provided...")
        }

        lore.add("${CC.GRAY}${CC.STRIKE_THROUGH}${"-".repeat(15)}")

        return lore
    }

    override fun getMaterial(player: Player): XMaterial {
        return XMaterial.NETHER_STAR
    }

    override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
        //SpectateCommands.spectate(player, gameServer.serverId)
    }
}
