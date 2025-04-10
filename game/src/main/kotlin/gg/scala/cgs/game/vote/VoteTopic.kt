package gg.scala.cgs.game.vote

import org.bukkit.inventory.ItemStack

class VoteTopic(
    val name: String,
    val description: String? = null,
    val alternativeNames: MutableList<String>? = null,
    val displayItemStack: ItemStack? = null
)


