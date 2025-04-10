package gg.scala.cgs.game.team

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import gg.scala.cgs.game.player.GamePlayer
import gg.scala.cgs.game.ScGameFramework
import java.util.*

class TeamMember(val uuid: UUID) {

    constructor(uuid: UUID, canPlay: Boolean) : this(uuid) {
        this.disqualified = !canPlay
    }

    var alive = true
        private set

    var disqualified = false
        private set

    fun markDead() {
        this.alive = false
    }

    fun markAlive() {
        this.alive = true
    }

    fun getPlayer(): Player? = Bukkit.getPlayer(uuid)
    fun sendMessage(msg: String) = getPlayer()?.sendMessage(msg)

    fun markDisqualified() {
        this.disqualified = true
    }

    fun markQualified() {
        this.disqualified = false
    }

    fun isOnline() = getPlayer() != null && getPlayer()!!.isOnline
    fun isPlayer(uuid: UUID) = this.uuid == uuid || this.uuid.toString() == uuid.toString()
    fun isAlive() = alive
    fun isDead() = !alive
    fun isDisqualified() = disqualified
    fun getGamePlayer(): GamePlayer? = ScGameFramework.instance.game?.getGamePlayer(uuid)
    fun getGamePlayerSafe(): GamePlayer? = ScGameFramework.instance.game?.getGamePlayerSafe(uuid)
}