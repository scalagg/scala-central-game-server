package gg.scala.cgs.game.team

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import gg.scala.cgs.game.game.GameRule
import gg.scala.cgs.game.ScGameFramework
import net.evilblock.cubed.util.bukkit.FancyMessage
import java.util.*

open class Team(val name: String, val color: String, val systemTeam: Boolean, val systemType: SystemTeamType) {
    constructor(name: String, color: String) : this(name, color, false, SystemTeamType.GENERAL)

    private var initialized: Boolean = false

    val members: MutableList<TeamMember> = ArrayList()

    open fun initialize() {
        if (initialized)
            throw IllegalStateException("Team already initialized")

        initialized = true
    }

    open fun startGame() {}

    fun getColoredName() = color + name

    fun sendMessage(msg: String) {
        forEachAlive { it.sendMessage(ChatColor.translateAlternateColorCodes('&', msg)) }
    }

    fun sendMessage(msg: FancyMessage) {
        forEachAlive { msg.sendToPlayer(it) }
    }

    fun forEachAlive(action: (Player) -> Unit) {
        getAlivePlayers().filterNotNull().forEach { action.invoke(it) }
    }

    fun getMaxPlayers() = ScGameFramework.instance.game!!.playerSettings.maxPlayers / ScGameFramework.instance.game!!.getPlayerTeams().size
    fun getPlayerCount() = members.size
    fun getOpenSlots() = if (systemTeam) 0 else (getMaxPlayers() - getPlayerCount())

    fun getPlayers() = members.map { it.getPlayer() }.toList()
    fun getAlivePlayers() = members.filter { !it.disqualified }.map { it.getPlayer() }.toList()

    fun isTeamAlive() = members.any { !it.disqualified }
    fun isTeamDisqualified() = members.all { it.disqualified }

    open fun getRelationColor(target: UUID): String {
        if (systemTeam) return ChatColor.DARK_GREEN.toString()
        if (isMember(target)) return ChatColor.GREEN.toString()
        return ChatColor.RED.toString()
    }

    open fun addMember(uuid: UUID, alive: Boolean = true): Boolean {
        if (systemTeam) return false
        if(!ScGameFramework.instance.game!!.getGameRule(GameRule.ADD_TO_EMPTY_TEAM))
            if (getOpenSlots() <= 0 && Bukkit.getPlayer(uuid)?.hasPermission("basic.staff") != true) return false

        if (isMember(uuid)) return false

        members.add(TeamMember(uuid, alive))
        ScGameFramework.instance.game!!.getGamePlayer(uuid)?.teamName = this.name
        return true
    }

    open fun removeMember(uuid: UUID) {
        if (systemTeam) return
        if (!isMember(uuid)) return

        members.removeIf { it.isPlayer(uuid) }
    }

    open fun getTeamKills(): Int {
        var kills = 0
        for (member in members) {
            kills += member.getGamePlayer()?.kills ?: 0
        }
        return kills
    }

    open fun getTeamDeaths(): Int {
        var deaths = 0
        for (member in members) {
            deaths += member.getGamePlayer()?.deaths ?: 0
        }
        return deaths
    }

    open fun getTeamDamageDealt(): Double {
        var damageDealt = 0.0
        for (member in members) {
            damageDealt += member.getGamePlayer()?.damageDealt ?: 0.0
        }
        return damageDealt
    }

    open fun getTeamDamageTaken(): Double {
        var damageTaken = 0.0
        for (member in members) {
            damageTaken += member.getGamePlayer()?.damageTaken ?: 0.0
        }
        return damageTaken
    }

    fun isMember(uuid: UUID) = !systemTeam && members.any { it.isPlayer(uuid) }
}