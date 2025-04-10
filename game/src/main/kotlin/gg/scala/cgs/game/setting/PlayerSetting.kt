package gg.scala.cgs.game.setting

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.function.Consumer

abstract class PlayerSetting(
    val id: String,
    val name: String,

    val lore: List<String>,

    val trueText: String,
    val falseText: String,

    val settingChanged: Consumer<Player>,

    val item: ItemStack,
    val defaultValue: Boolean = true
)