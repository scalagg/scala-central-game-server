package gg.scala.cgs.game.preferences.buttons

import com.cryptomorin.xseries.XMaterial
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import gg.scala.cgs.game.setting.PlayerSetting
import gg.scala.cgs.game.setting.SettingContainer
import net.evilblock.cubed.menu.Button
import org.bukkit.inventory.InventoryView

class SettingButton(val setting: PlayerSetting) : Button() {

    override fun getMaterial(player: Player): XMaterial = XMaterial.matchXMaterial(setting.item.type)
    override fun getDamageValue(player: Player): Byte = setting.item.durability.toByte()

    override fun getName(player: Player): String = ChatColor.AQUA.toString() + setting.name

    override fun clicked(player: Player, slot: Int, clickType: ClickType, inventoryView: InventoryView) {
        if (clickType != ClickType.LEFT) return

        SettingContainer.toggle(player, setting)
        setting.settingChanged.accept(player)
    }

    override fun getDescription(player: Player): List<String> {
        val lines: MutableList<String> = ArrayList()

        lines += ""

        if (setting.lore.isNotEmpty()) {
            lines.addAll(setting.lore.map { ChatColor.BLUE.toString() + it }.toList())
            lines += ""
        }

        val value = SettingContainer.get(player, setting)
        lines += (if (value) (ChatColor.BLUE.toString() + ChatColor.BOLD + "  ► ") else "") + ChatColor.GRAY + setting.trueText
        lines += (if (!value) (ChatColor.BLUE.toString() + ChatColor.BOLD + "  ► ") else "") + ChatColor.GRAY + setting.falseText

        return lines
    }
}