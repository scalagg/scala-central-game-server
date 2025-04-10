package gg.scala.cgs.game.setting.impl

import org.bukkit.ChatColor
import org.bukkit.Material
import gg.scala.cgs.game.setting.PlayerSetting
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.visibility.VisibilityHandler
import java.util.function.Consumer

class SpectatorsSetting : PlayerSetting(
    "SEE_SPECTATORS",
    "View Other Spectators",

    listOf(
        "Whether or not to see other spectators",
        "when you're a spectator yourself."
    ),

    ChatColor.YELLOW.toString() + "Show other spectators",
    ChatColor.YELLOW.toString() + "Hide other spectators",

    Consumer {
        VisibilityHandler.update(it)
    },

    ItemBuilder.of(Material.SKULL_ITEM).build(),
    true
)