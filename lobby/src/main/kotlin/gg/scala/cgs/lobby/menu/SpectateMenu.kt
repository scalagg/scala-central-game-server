package gg.scala.cgs.lobby.menu

import gg.scala.cgs.lobby.ScGameLobby
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PageButton
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import org.bukkit.entity.Player

/**
 * @author Topu
 * @date 11/18/2024
 */
class SpectateMenu : PaginatedMenu() {

    init {
        autoUpdate = true
    }

    override fun getPrePaginatedTitle(player: Player): String {
        return "Spectate Server"
    }

    override fun getAllPagesButtons(player: Player): Map<Int, Button> {
        val buttons = mutableMapOf<Int, Button>()
        var i = 0

        for (server in ScGameLobby.instance.servers.values) {
            val status = server.status ?: continue
            if (status.gameStageType in listOf("PRE_GAME", "VOTING")) continue

//            buttons[i++] = SpectateButton(server)
        }

        return buttons
    }
}