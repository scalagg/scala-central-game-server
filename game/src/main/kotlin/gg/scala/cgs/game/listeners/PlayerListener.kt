package gg.scala.cgs.game.listeners

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.*
import org.bukkit.event.player.*
import gg.scala.cgs.game.game.GameRule
import gg.scala.cgs.game.game.GameStage
import gg.scala.cgs.game.ScGameFramework
import gg.scala.cgs.game.deathmessage.DeathMessageService
import gg.scala.cgs.game.deathmessage.damage.UnknownDamage
import gg.scala.cgs.game.utils.DefaultItems
import gg.scala.commons.annotations.Listeners
import gg.scala.lemon.util.QuickAccess
import net.evilblock.cubed.nametag.NametagHandler
import kotlin.math.max

@Listeners
object PlayerListener : Listener {

//    @EventHandler
//    fun onPotion(event: PotionEffectAddEvent) {
//        if (event.entity is Player) NametagHandler.reloadPlayer(event.entity as Player)
//    }
//
//    @EventHandler
//    fun onPotion(event: PotionEffectRemoveEvent) {
//        if (event.entity is Player) NametagHandler.reloadPlayer(event.entity as Player)
//    }
//
//    @EventHandler
//    fun onPotion(event: PotionEffectExpireEvent) {
//        if (event.entity is Player) NametagHandler.reloadPlayer(event.entity as Player)
//    }
//
//    @EventHandler
//    fun onPotion(event: PotionEffectExtendEvent) {
//        if (event.entity is Player) NametagHandler.reloadPlayer(event.entity as Player)
//    }

    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        val game = ScGameFramework.instance.game ?: return
        if(game.gameStage != GameStage.IN_GAME) return

        val record = DeathMessageService.getDamage(event.entity)
        val deathCause = if (record.isNotEmpty()) record[record.size - 1] else UnknownDamage(event.entity.uniqueId, 1.0)

        val deathMessage = deathCause.getDeathMessage(event.entity.uniqueId)
        game.logDeath(ChatColor.stripColor(deathMessage))
    }

    @EventHandler
    fun onLogin(event: AsyncPlayerPreLoginEvent) {
        val game = ScGameFramework.instance.game

        if (game == null && !Bukkit.getServer().operators.any { it.uniqueId.toString() == event.uniqueId.toString() }) {
            event.disallow(PlayerPreLoginEvent.Result.KICK_OTHER, "§cThe game has not been set up yet.")
            return
        }

        if (game == null) return
        if (event.result == PlayerPreLoginEvent.Result.KICK_FULL) event.allow()
        if (event.result != null && event.result != PlayerPreLoginEvent.Result.ALLOWED) return

        when (game.gameStage) {
//            GameStage.SETUP -> event.disallow(PlayerPreLoginEvent.Result.KICK_OTHER, "§cThe server is still setting up.")

            GameStage.PRE_GAME, GameStage.VOTING, GameStage.STARTING -> {
                val starting = game.gameStage == GameStage.STARTING
                var spectate = false

                if (game.getPlayerCount() >= game.playerSettings.maxPlayers) {
                    if (starting) spectate = true
                    else {
                        event.disallow(PlayerPreLoginEvent.Result.KICK_OTHER, "§cThe game is full!")
                        return
                    }
                }

                game.createGamePlayer(event.uniqueId, spectate)
            }

            GameStage.IN_GAME -> {
                game.createGamePlayer(event.uniqueId, game.getTeam(event.uniqueId) == null)
            }

            GameStage.REBOOTING -> event.disallow(PlayerPreLoginEvent.Result.KICK_OTHER, "§cThe server is no longer joinable.")
            GameStage.SETUP -> TODO()
        }
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        event.joinMessage = null

        val player = event.player
        val game = ScGameFramework.instance.game ?: return

        if(game.getGamePlayer(player) == null) game.createGamePlayer(player)

        ScGameFramework.instance.game!!.getGamePlayer(player)!!.joinTime = System.currentTimeMillis()


        when (game.gameStage) {

            GameStage.PRE_GAME, GameStage.VOTING, GameStage.STARTING -> {
                if(game.getGameRule(GameRule.IGNORE_MIN_PLAYER_LIMIT)) return

                val needed =
                    max(0, game.playerSettings.minPlayers - game.getPlayerCount())

                Bukkit.broadcastMessage("${ChatColor.LIGHT_PURPLE}${QuickAccess.coloredName(player)}${ChatColor.YELLOW} has joined the game" + (if(needed > 0)  ", waiting for ${ChatColor.LIGHT_PURPLE}$needed" + " player" + (if (needed == 1) "" else "s") else "")  + "${ChatColor.YELLOW}.")

                if(game.gameStage == GameStage.PRE_GAME && game.getPlayerCount() >= game.playerSettings.minPlayers) game.startVoting()
            }
            else -> {}
        }
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        event.quitMessage = null

        val gamePlayer = ScGameFramework.instance.game!!.getGamePlayer(event.player)
        gamePlayer?.updatePlaytime()

        val player = event.player

        val game = ScGameFramework.instance.game ?: return

        when(game.gameStage) {
            GameStage.PRE_GAME, GameStage.VOTING, GameStage.STARTING -> {
                game.removeGamePlayer(player)
                if((game.gameStage == GameStage.VOTING || game.gameStage == GameStage.STARTING) &&  game.playerSettings.minPlayers > game.getPlayerCount()) {
                    if(game.getGameRule(GameRule.IGNORE_MIN_PLAYER_LIMIT)) return

                    game.gameStage = GameStage.PRE_GAME
                    game.startingSecondsLeft = game.startingSecondsIn + 1
                    game.votingSecondsLeft = game.votingSecondsIn + 1
                    val needed =
                        max(0, game.playerSettings.minPlayers - game.getPlayerCount())
                    Bukkit.broadcastMessage("${ChatColor.LIGHT_PURPLE}${QuickAccess.coloredName(player)}${ChatColor.YELLOW} has left the game" + (if(needed > 0)  ", waiting for ${ChatColor.LIGHT_PURPLE}$needed" + " player" + (if (needed == 1) "" else "s") else "")  + "${ChatColor.YELLOW}.")

                }
                return
            }

            else -> { }
        }
    }

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        val game = ScGameFramework.instance.game ?: return
        val gamePlayer = game.getGamePlayer(event.player) ?: return
        if(!gamePlayer.isSpectator() || !gamePlayer.isSpectator()) return
        val item = event.item ?: return
        if(item.type == Material.AIR) return
        if(item.isSimilar(DefaultItems.returnToLobby)) event.player.kickPlayer("${ChatColor.YELLOW}Sent you back to the lobby!")
    }

}