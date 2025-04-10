package gg.scala.cgs.spigot.stats

import com.cryptomorin.xseries.XMaterial
import com.mongodb.client.model.Filters
import gg.scala.cgs.spigot.CgsSpigotPlugin
import gg.scala.cgs.spigot.game.GameData
import gg.scala.cgs.spigot.game.GameData.getData
import gg.scala.cgs.spigot.profile.GameProfile
import gg.scala.store.spigot.ScalaDataStoreSpigot
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.time.TimeUtil
import org.bson.Document
import org.bson.json.JsonMode
import org.bson.json.JsonWriterSettings
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

/**
 * @author Topu
 * @date 1/4/2025
 */
class MinigameStatsProvider(
    override val name: String,
    override val displayItem: ItemStack,
    override val dataClazz: Class<*>
) : StatsProvider(name, displayItem, dataClazz)
{

    override fun resetStats(player: Player, uuid: UUID) {
        val gamePlayer: GameProfile = getFromJson(getData(uuid).toString()) as GameProfile

        if (gamePlayer == null) {
            player.sendMessage("Failed to grab stats for the gamemode $name.")
            return
        }

        // handle reset
        player.sendMessage("yup")
    }

    fun getData(uuid: UUID): String? {
        val profileDocument: Document =
            ScalaDataStoreSpigot.INSTANCE.client!!.getDatabase(name + "-" + CgsSpigotPlugin.season)
                .getCollection("players").find(
                    Filters.eq<String>("uuid", uuid.toString())
                ).first()
                ?: return null
        val relaxed = JsonWriterSettings.builder().outputMode(JsonMode.RELAXED).build()
        return profileDocument.toJson(relaxed)
    }

    fun getMatches(uuid: UUID): List<Document> {
        val gamePlayer: GameProfile = GameData.getData(uuid).get(name) ?: return ArrayList<Document>()

        return gamePlayer.totalSeasonGamesDocuments
    }

    override fun fetchPreviousMatchesButton(uuid: UUID): List<Button> {
        val buttons: MutableList<Button> = ArrayList<Button>()
        for (match in getMatches(uuid)) {
            val gamePlayers = match["gamePlayers"] as Document?
            val gamePlayerDocument = gamePlayers!![uuid.toString()] as Document?
            val activeMap = match["activeMap"] as Document?

            val teamName = gamePlayerDocument!!.getString("teamName")
            val hasWon = gamePlayerDocument.getBoolean("hasWon")
            val placedAt = gamePlayerDocument.getInteger("placedAt")

            buttons.add(
                object : Button() {
                    override fun getName(player: Player): String {
                        return "${CC.GOLD}${TimeUtil.formatIntoDateString(Date(match.getLong("gameEndedAt")))}"
                    }

                    override fun getDescription(player: Player): List<String> {
                        val lore: MutableList<String> = ArrayList()
                        lore.add(ChatColor.DARK_GRAY.toString() + "#" + match.getString("matchID"))
                        lore.add("${CC.STRIKE_THROUGH}${CC.GRAY}---------------------------------------------")
                        val mapName = activeMap!!.getString("name")
                        lore.add(ChatColor.YELLOW.toString() + "Players: " + ChatColor.WHITE + gamePlayers.size)
                        if (!mapName.equals("classic", ignoreCase = true)) {
                            lore.add(ChatColor.YELLOW.toString() + "Map: " + ChatColor.WHITE + mapName)
                        }
                        lore.add("")

                        lore.add(ChatColor.YELLOW.toString() + "Your Team")
                        lore.add(ChatColor.RED.toString() + "//TODO: Add :)")
                        lore.add("")

                        if (placedAt == -1) {
                            lore.add(ChatColor.GRAY.toString() + ChatColor.ITALIC + "You was spectating this game!")
                        } else if (hasWon) {
                            lore.add(ChatColor.GREEN.toString() + ChatColor.BOLD + "YOU WON!")
                        } else {
                            lore.add(ChatColor.YELLOW.toString() + "You placed " + ChatColor.LIGHT_PURPLE + "#" + placedAt + ChatColor.YELLOW + " this game.")
                        }
                        lore.add("${CC.STRIKE_THROUGH}${CC.GRAY}---------------------------------------------")

                        return lore
                    }

                    override fun getMaterial(var1: Player): XMaterial {
                        if (placedAt == -1) return XMaterial.IRON_BLOCK
                        return if (hasWon) XMaterial.EMERALD_BLOCK else XMaterial.REDSTONE_BLOCK
                    }
                }
            )
        }

        buttons.reverse()
        return buttons
    }
}
