package gg.scala.cgs.game.listeners

import net.minecraft.server.v1_8_R3.PacketPlayOutScoreboardScore
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.scoreboard.DisplaySlot
//import org.imanity.imanityspigot.event.PlayerHealthChangeEvent
import gg.scala.cgs.game.game.GameRule
import gg.scala.cgs.game.player.GamePlayer
import gg.scala.cgs.game.ScGameFramework
import gg.scala.commons.annotations.Listeners
import java.lang.reflect.Field
import java.util.*
import java.util.stream.Collectors

/**
 * Handles registering and un-registering the Objective that shows the health
 * below player's name-tags. This is also responsible for listening to health
 * changes and sending the update score packets manually, for consistency.
 */
@Listeners
object MatchHealthDisplayListener : Listener {

    private const val OBJECTIVE_NAME = "HealthDisplay"
    private var aField: Field? = null
    private var bField: Field? = null
    private var cField: Field? = null
    private var dField: Field? = null

    init {
        try {
            aField = PacketPlayOutScoreboardScore::class.java.getDeclaredField("a").apply { isAccessible = true }
            bField = PacketPlayOutScoreboardScore::class.java.getDeclaredField("b").apply { isAccessible = true }
            cField = PacketPlayOutScoreboardScore::class.java.getDeclaredField("c").apply { isAccessible = true }
            dField = PacketPlayOutScoreboardScore::class.java.getDeclaredField("d").apply { isAccessible = true }
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        }
    }

    @EventHandler
    fun onSpectatorJoin(event: PlayerJoinEvent) {
        val game = ScGameFramework.instance.game
        if (game == null || !game.getGameRule(GameRule.SHOW_HEALTH)) return

        Bukkit.getScheduler().runTaskLater(ScGameFramework.instance, {
            initialize(event.player)
        }, 1L)

        Bukkit.getScheduler().runTaskTimer(ScGameFramework.instance, { sendAllTo(event.player) }, 20L, 10L)
    }

    private fun sendAllTo(viewer: Player) {
        val game = ScGameFramework.instance.game
        if (game == null || !game.getGameRule(GameRule.SHOW_HEALTH)) return

        val objective = viewer.scoreboard.getObjective(DisplaySlot.BELOW_NAME) ?: return

        game.getPlayers()
            .mapNotNull(GamePlayer::getPlayer)
            .forEach { target ->
                try {
                    val packet = PacketPlayOutScoreboardScore().apply {
                        aField?.set(this, target.name)
                        bField?.set(this, OBJECTIVE_NAME)
                        cField?.set(this, getHealth(target))
                        dField?.set(this, PacketPlayOutScoreboardScore.EnumScoreboardAction.CHANGE)
                    }
                    (viewer as CraftPlayer).handle.playerConnection.sendPacket(packet)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
    }

    private fun sendToAll(target: Player) {
        val game = ScGameFramework.instance.game
        if (game == null || !game.getGameRule(GameRule.SHOW_HEALTH)) return

        try {
            val packet = PacketPlayOutScoreboardScore().apply {
                aField?.set(this, target.name)
                bField?.set(this, OBJECTIVE_NAME)
                cField?.set(this, getHealth(target))
                dField?.set(this, PacketPlayOutScoreboardScore.EnumScoreboardAction.CHANGE)
            }
            Bukkit.getOnlinePlayers().forEach { viewer ->
                (viewer as CraftPlayer).handle.playerConnection.sendPacket(packet)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initialize(player: Player) {
        val game = ScGameFramework.instance.game
        if (game == null || !game.getGameRule(GameRule.SHOW_HEALTH)) return

        if (player.scoreboard.getObjective(DisplaySlot.BELOW_NAME) == null) {
            val objective = player.scoreboard.registerNewObjective(OBJECTIVE_NAME, "dummy")
            objective.displayName = "${ChatColor.DARK_RED}❤"
            objective.displaySlot = DisplaySlot.BELOW_NAME
        }
    }

    private fun clearAll(player: Player) {
        player.scoreboard.getObjective(DisplaySlot.BELOW_NAME)?.unregister()
        player.scoreboard.clearSlot(DisplaySlot.BELOW_NAME)
    }

    private fun clear(viewer: Player, target: Player) {
        viewer.scoreboard.resetScores(target.name)
    }

    private fun getHealth(player: Player): Int {
        return kotlin.math.ceil(player.health + (player as CraftPlayer).handle.absorptionHearts).toInt()
    }
}
