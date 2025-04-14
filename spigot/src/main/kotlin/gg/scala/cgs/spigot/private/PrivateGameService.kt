package gg.scala.cgs.spigot.private

import gg.scala.cgs.common.game.GameType
import gg.scala.cgs.spigot.*
import gg.scala.commons.agnostic.sync.ServerSync
import gg.scala.commons.agnostic.sync.server.AbstractServer
import gg.scala.commons.agnostic.sync.server.ServerContainer
import gg.scala.flavor.service.Service
import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.player.LemonPlayer
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*


/**
 * @author Topu
 * @date 4/14/2025
 */
@Service
object PrivateGameService {

 private val psPlayers: List<PGProfile> = ArrayList<PGProfile>()
 private const val publicPrivateServer: Boolean = false

 fun hasPermissionToHost(player: Player, gameType: GameType): Boolean {
  return player.isOp || player.hasPermission("gamemanager.request") || player.hasPermission("privateserver.host.*") || player.hasPermission(
   "privateserver.host." + gameType.serverName.toLowerCase()
  )
 }

 fun getPlayerPrivateServer(profile: LemonPlayer): AbstractServer? {
  return ServerContainer.getServer("PS-" + profile.name)
 }

 fun isPrivateServer(server: AbstractServer): Boolean {
  return server.id.startsWith("PS-")
 }

 fun isPrivateServer(): Boolean {
  return isPrivateServer(ServerSync.getLocalGameServer())
 }

 fun isPrivateServerOwner(profile: LemonPlayer): Boolean {
  val player: Player = Bukkit.getPlayer(profile.identifier)
  return isPrivateServer() && (getPlayerPrivateServer(profile)?.id == "PS-" + profile.name || player.hasPermission("gamemanager.request"))
 }

 fun isModerator(profile: LemonPlayer): Boolean {
  val player: Player = Bukkit.getPlayer(profile.identifier)
  return isPrivateServer() && isPrivateServerOwner(profile) || getPSPlayer(profile.identifier).moderator || player != null && player.hasPermission(
   "gamemanager.request"
  )
 }

 fun getPrivateServerOwner(server: AbstractServer): LemonPlayer? {
  return PlayerHandler.findOnlinePlayer(server.id.replace("PS-", ""))
  //return BukkitAPI.getProfile(server.id.replace("PS-", ""))
 }

 fun getPrivateServerOwner(): LemonPlayer? {
  return getPrivateServerOwner(ServerSync.getLocalGameServer())
 }

 fun getPSPlayer(uuid: UUID?): PGProfile {
  return psPlayers.stream().filter { psPlayer -> psPlayer.uuid == uuid }.findAny().orElse(null)
 }
}