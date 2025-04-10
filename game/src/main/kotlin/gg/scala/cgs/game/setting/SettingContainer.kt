package gg.scala.cgs.game.setting

import org.bukkit.entity.Player
import gg.scala.cgs.game.setting.impl.SpectatorsSetting
import java.util.*

object SettingContainer {

    val settings: MutableMap<String, PlayerSetting> = HashMap()
    val playerSettingMap: MutableMap<String, MutableMap<UUID, Boolean>> = HashMap()

    fun load() {
        register(SpectatorsSetting())
    }

    fun register(setting: PlayerSetting) {
        settings[fixId(setting.id)] = setting
    }

    fun getSetting(idOrName: String): PlayerSetting? = settings[fixId(idOrName)] ?: settings.values.find { it.name.equals(idOrName, true) }

    fun get(player: Player, idOrName: String): Boolean {
        val setting = getSetting(idOrName) ?: return false
        return get(player, setting)
    }

    fun get(player: Player, setting: PlayerSetting): Boolean {
        return playerSettingMap.getOrDefault(fixId(setting.id), HashMap()).getOrDefault(player.uniqueId, setting.defaultValue)
    }

    fun toggle(player: Player, setting: PlayerSetting): Boolean {
        val value = !get(player, setting)

        playerSettingMap.putIfAbsent(fixId(setting.id), HashMap())
        playerSettingMap[fixId(setting.id)]!![player.uniqueId] = value

        return value
    }

    fun fixId(id: String) = id.toUpperCase().replace(" ", "_")

}