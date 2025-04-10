package gg.scala.cgs.game.player

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import gg.scala.cgs.game.ScGameFramework
import gg.scala.cgs.game.team.Team
import gg.scala.cgs.game.team.TeamMember
import net.evilblock.cubed.util.bukkit.FancyMessage
import java.util.*

open class GamePlayer(val uuid: UUID) {
    var spectator = false

    var kills = 0
    var deaths = 0
    var damageDealt = 0.0
    var damageTaken = 0.0
    var hasWon = false
    var teamName = ""
    var joinTime = 0L
    var loggedPlayTime = 0L
    var placedAt = -1
    var loadedGameElo = 1400
    var endedGameElo = 1400
    var balance: Int = 500


    @Transient var totalSeasonKills = 0
    @Transient var totalSeasonDeaths = 0
    @Transient var totalSeasonLoses = 0
    @Transient var totalSeasonWins = 0
    @Transient var totalSeasonDamageTaken = 0.0
    @Transient var totalSeasonDamageDealt = 0.0
    @Transient var totalSeasonPlaytime = 0L
    @Transient var totalSeasonElo = 1400
    @Transient var totalSeasonGames = arrayListOf<String>()
    @Transient var totalSeasonExtraData = hashMapOf<String, Any>()

    fun addKill() = kills++
    fun addDeath() {
        deaths++
        this.placedAt = ScGameFramework.instance.game!!.getPlayerCount()
    }

    fun addDamageDealt(dmg: Double) {
        damageDealt += dmg
    }

    fun addDamageTaken(dmg: Double) {
        damageTaken += dmg
    }

    open fun tick() {}
    open fun fullTick() {}

    fun getTeam(): Team? = ScGameFramework.instance.game?.getTeam(uuid)

    fun setTeam(team: Team) {
        ScGameFramework.instance.game ?: return
        val oldTeam = getTeam()

        if (oldTeam != null)
            oldTeam.removeMember(uuid)

        team.addMember(uuid)
    }

    fun getAsTeamMember(): TeamMember? = getTeam()?.members?.find { it.isPlayer(this.uuid) }

    fun setBalance(amount: Int) = this.balance+amount.toDouble()

    fun getPlayer(): Player? = Bukkit.getPlayer(uuid)
    fun sendMessage(msg: String) {
        getPlayer()?.sendMessage(ChatColor.translateAlternateColorCodes('&', msg))
    }

    fun sendMessage(msg: FancyMessage) {
        msg.sendToPlayer(getPlayer()!!)
    }

    fun isPlayer(uuid: UUID) = this.uuid == uuid || this.uuid.toString() == uuid.toString()
    fun isSpectator() = spectator
    fun isOnline() = Bukkit.getPlayer(uuid) != null && Bukkit.getPlayer(uuid).isOnline

    fun addSeasonGame(matchID: String): ArrayList<String> {
        totalSeasonGames.add(matchID)
        return totalSeasonGames
    }

    fun getExtraSeasonData(key: String): Any? {
        return totalSeasonExtraData[key]
    }

    fun setExtraSeasonData(key: String, value: Any) {
        totalSeasonExtraData[key] = value
    }

    fun getExtraSeasonDataString(key: String, defaultValue: String?): String? {
        if(!totalSeasonExtraData.containsKey(key)) return defaultValue
        return totalSeasonExtraData[key] as String
    }

    fun getExtraSeasonDataString(key: String): String? {
        return getExtraSeasonDataString(key, null)
    }

    fun getExtraSeasonDataInteger(key: String, defaultValue: Int?): Int? {
        if(!totalSeasonExtraData.containsKey(key)) return defaultValue
        return totalSeasonExtraData[key] as Int
    }

    fun getExtraSeasonDataInteger(key: String): Int? {
        return getExtraSeasonDataInteger(key, null)
    }

    fun getCurrentSessionPlaytime(): Long {
        return System.currentTimeMillis() - joinTime
    }

    fun updatePlaytime() {
        this.loggedPlayTime = (System.currentTimeMillis() - joinTime) / 1000
    }


}