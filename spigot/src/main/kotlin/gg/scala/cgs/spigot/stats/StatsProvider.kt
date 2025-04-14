package gg.scala.cgs.spigot.stats

import com.google.gson.Gson
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.util.CC
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.Nullable
import java.util.*

/**
 * @author Topu
 * @date 11/16/2024
 */
open class StatsProvider(
    open val name: String,
    open val displayItem: ItemStack,
    open val dataClazz: Class<*>
) {

    open fun getFromJson(json: String): Any {
        return Gson().fromJson(json, dataClazz)
    }

    open fun resetStats(player: Player, uuid: UUID) {
    }

    open fun fetchPreviousMatchesButton(uuid: UUID): List<Button> {
        return listOf(Button.placeholder(Material.STAINED_GLASS_PANE, 14.toByte(), "${CC.RED}No data provided..."))
    }
}
