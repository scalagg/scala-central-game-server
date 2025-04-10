package gg.scala.cgs.game.commands

import org.bukkit.entity.Player
import gg.scala.cgs.game.preferences.PreferencesMenu
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand

@AutoRegister
object PrefsCommands : ScalaCommand() {

    @CommandAlias("gamesettings")
    fun onPreferences(player: Player) {
        PreferencesMenu().openMenu(player)
    }

}