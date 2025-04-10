package gg.scala.cgs.game.preferences

import org.bukkit.entity.Player
import gg.scala.cgs.game.preferences.buttons.SettingButton
import gg.scala.cgs.game.setting.SettingContainer
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu

class PreferencesMenu : Menu() {

    override fun getButtons(player: Player): Map<Int, Button> {
        val buttons: MutableMap<Int, Button> = HashMap()

        var i = 0
        for (setting in SettingContainer.settings.values) {
            buttons[i++] = SettingButton(setting)
        }

        return buttons
    }

    override fun getTitle(player: Player): String = "Preferences"
}