package gg.scala.cgs.lobby.queue

import gg.scala.cgs.lobby.ScGameLobby
import gg.scala.parties.model.Party
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit
import org.bukkit.entity.Player

/**
 * @author Topu
 * @date 11/16/2024
 */
class Queue(
    val gameType: String,
    val teamSize: Int,
    val minPlayers: Int
) {
    private var members = mutableListOf<QueueEntry>()

    private var lastTriedConnection: Long = 0
    private var lastServerRequest: Long = 0

    fun tick() {
        connectPlayers(forced = false)
    }

    val requiredPlayers: Int
        get() = if (minPlayers < 1) teamSize * 2 else minPlayers

    fun connectPlayers(forced: Boolean) {
        if (getQueued() < requiredPlayers && !forced) return // Need at least 2 full teams to start a game

        val timeSinceLastConnection = System.currentTimeMillis() - lastTriedConnection
        if (timeSinceLastConnection <= 5000 && !forced) return

        val selectedServer = ScGameLobby.instance.servers.values
            .firstOrNull { it.isAvailable() && it.isGameType(gameType) }
            ?: return

        sendToServer(selectedServer.serverDisplayName)
        lastTriedConnection = System.currentTimeMillis()
    }

    private fun fixMembers() {
        members = members.filter { it.shouldStay }
            .distinct()
            .sortedBy { it.priority }
            .toMutableList()
    }

    fun contains(player: Player): Boolean {
        fixMembers()
        return members.any { it.contains(player.uniqueId) }
    }

    fun contains(party: Party): Boolean {
        fixMembers()
        return members.any { it.isPartyQueue && it.party == party }
    }

    fun add(player: Player): Boolean {
        if (contains(player)) return false
        members.add(QueueEntry(player.uniqueId, null))
        fixMembers()
        return true
    }

    fun add(party: Party): Boolean {
        if (contains(party)) return false
        members.add(QueueEntry(null, party))
        fixMembers()
        return true
    }

    fun remove(player: Player): Boolean {
        if (!contains(player)) return false
        members.filter { it.contains(player.uniqueId) }.forEach { it.markForRemoval() }
        fixMembers()
        return true
    }

    fun remove(party: Party): Boolean {
        if (!contains(party)) return false
        members.filter { it.isPartyQueue && it.party == party }.forEach { it.markForRemoval() }
        fixMembers()
        return true
    }

    fun getQueued(): Int {
        fixMembers()
        return members.sumOf { it.totalSize }
    }

    fun sendToServer(server: String, instant: Boolean = false) {
        // Send parties first
        forEachEntry {
            if (!it.isSingleQueue) {
                it.sendToServer(server, instant)
            }
        }

        // Send singles
        forEachEntry {
            if (it.isSingleQueue) {
                it.sendToServer(server, instant)
            }
        }
    }

    fun forEachEntry(consumer: (QueueEntry) -> Unit) {
        fixMembers()
        members.forEach(consumer)
    }

    fun forEachMember(consumer: (Player) -> Unit) {
        fixMembers()
        forEachEntry { entry ->
            if (entry.isSingleQueue) {
                Bukkit.getPlayer(entry.player)?.takeIf { it.isOnline }?.let(consumer)
            } else {
                entry.party?.includedMembersOnline()?.mapNotNull { Bukkit.getPlayer(it) }?.filter { it.isOnline }?.forEach(consumer)
            }
        }
    }

    fun destroy() {
        forEachMember { it.sendMessage("${CC.RED}The queue you were in was destroyed - you have been removed.") }
        members.clear()
    }
}