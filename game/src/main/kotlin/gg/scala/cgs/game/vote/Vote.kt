package gg.scala.cgs.game.vote

import org.bukkit.Bukkit
import org.bukkit.ChatColor.*
import org.bukkit.entity.Player
import gg.scala.cgs.game.utils.ServerUtils
import java.util.*
import java.util.concurrent.ConcurrentHashMap

open class Vote(val topics: List<VoteTopic>) {

    val votes: MutableMap<UUID, String> = ConcurrentHashMap()

    fun getVote(player: Player) = getVote(player.uniqueId)
    fun getVote(uuid: UUID): VoteTopic? = getTopic(votes.getOrDefault(uuid, "§"))

    fun getTopic(name: String) = topics.find { it.name == name }

    fun countVotes(): List<VoteEntry> {
        val map: MutableMap<VoteTopic, Int> = HashMap()
        map.putAll(topics.map { it to 0 })

        for (vote in votes) {
            val topic = getTopic(vote.value) ?: continue
            map[topic] = map[topic]!! + (Bukkit.getPlayer(vote.key)?.let { ServerUtils.getVoteWeight(it) } ?: 1)
        }

        return map.entries.toList().sortedByDescending { it.value }.map { VoteEntry(it.key, it.value) }
    }

    fun reset() = votes.clear()
    fun remove(player: Player) = votes.remove(player.uniqueId)
    fun remove(uuid: UUID) = votes.remove(uuid)

    fun vote(player: Player, name: String) {
        val vote = getVote(player)

        if (vote != null) {
            player.sendMessage("${RED}You have already voted for " + vote.name + ".")
            return
        }

        var topic = topics.find { it.name.equals(name, true) }

        if(topic == null) {
            for (topicFind in topics) {
                if(topicFind.alternativeNames?.contains(name.toLowerCase()) == true) topic = topicFind
            }
        }

        if (topic == null) {
            player.sendMessage("$RED\"$name\" is not a valid vote option.")
            return
        }

        votes[player.uniqueId] = topic.name

        val weight = ServerUtils.getVoteWeight(player)
        player.sendMessage("${YELLOW}You have used your ${LIGHT_PURPLE}$weight vote${if (weight > 1) "s" else ""} ${YELLOW}on $LIGHT_PURPLE" + topic.name + "${YELLOW}.")
    }

}