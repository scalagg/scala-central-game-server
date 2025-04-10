package gg.scala.cgs.lobby.queue

import com.google.gson.Gson
import gg.scala.cgs.common.packet.CgsPacketService
import gg.scala.cgs.lobby.ScGameLobby
import gg.scala.cgs.lobby.command.JoinFastServerCommand
import gg.scala.cgs.lobby.util.BukkitUtil
import gg.scala.lemon.util.QuickAccess
import gg.scala.parties.model.Party
import gg.scala.parties.model.PartyRole
import net.evilblock.cubed.util.bungee.BungeeUtil
import org.bukkit.Bukkit
import java.util.*

/**
 * @author Topu
 * @date 11/16/2024
 */
class QueueEntry(
    val player: UUID?,
    val party: Party?
) {
    private var shouldBeRemoved: Boolean = false

    val isSingleQueue: Boolean
        get() = player != null

    val isPartyQueue: Boolean
        get() = party != null

    val totalSize: Int
        get() = when {
            player != null -> 1
            party != null -> party.members.size + 1
            else -> throw IllegalStateException("Invalid entry")
        }

    fun markForRemoval() {
        shouldBeRemoved = true
    }

    fun sendToServer(server: String, instant: Boolean = false) {
        when {
            isSingleQueue -> {
                val onlinePlayer = Bukkit.getPlayer(player!!)
                if (onlinePlayer == null || !onlinePlayer.isOnline) return

                if (instant) {
                    BungeeUtil.sendToServer(onlinePlayer, server)
                } else {
                    JoinFastServerCommand.join(onlinePlayer, server)
                }
            }
            isPartyQueue -> {
                CgsPacketService.buildMessage(
                    "setup-party",
                    mapOf(
                        "party" to Gson().toJson(party),
                        "server" to server
                    )
                ).publish()
                val leader = Bukkit.getPlayer(party!!.leader.uniqueId)
                leader?.takeIf { it.isOnline }?.let {
                    if (instant) {
                        BukkitUtil.sendPlayer(ScGameLobby.instance, it, server)
                    } else {
                        JoinFastServerCommand.join(it, server)
                    }
                }

                party.includedMembersOnline().forEach { onlineMember ->
                    if (instant) {
                        BukkitUtil.sendPlayer(ScGameLobby.instance, Bukkit.getPlayer(onlineMember), server)
                    } else {
                        JoinFastServerCommand.join(Bukkit.getPlayer(onlineMember), server)
                    }
                }
            }
            else -> throw IllegalStateException("Invalid entry")
        }
    }

    fun contains(uuid: UUID): Boolean {
        return when {
            isSingleQueue -> player == uuid
            isPartyQueue -> party!!.findMember(uuid)!!.role == PartyRole.LEADER || party.members.contains(uuid)
            else -> false
        }
    }

    val shouldStay: Boolean
        get() = isOnline && !shouldBeRemoved

    val isOnline: Boolean
        get() = when {
            isSingleQueue -> Bukkit.getPlayer(player) != null
            isPartyQueue -> true
            else -> false
        }

    val priority: Int
        get() = if (!isOnline) -1 else calculatePriority(this)

    private fun calculatePriority(entry: QueueEntry): Int {
        if (!entry.isOnline) return -1
        val priorities = mutableListOf<Int>()

        try {
            if (entry.isSingleQueue) {
                priorities.add(QuickAccess.realRank(Bukkit.getPlayer(entry.player)).weight)
            } else {
                priorities.add(QuickAccess.realRank(Bukkit.getPlayer(entry.party!!.leader.uniqueId)).weight)
                entry.party.includedMembersOnline().forEach { onlineMember ->
                    priorities.add(QuickAccess.realRank(Bukkit.getPlayer(onlineMember)).weight)
                }
            }
        } catch (ex: Exception) {
            priorities.add(0)
        }

        return priorities.minOrNull() ?: 0
    }
}
