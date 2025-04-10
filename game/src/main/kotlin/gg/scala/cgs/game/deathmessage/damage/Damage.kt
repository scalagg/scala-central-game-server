package gg.scala.cgs.game.deathmessage.damage

import gg.scala.cgs.game.deathmessage.DeathMessageService
import java.util.*

/**
 * @author Topu
 * @date 11/17/2024
 */
abstract class Damage(
    val damaged: UUID,
    val damage: Double
) {
    val time: Long = System.currentTimeMillis()

    companion object {
        fun wrapName(player: UUID, wrapFor: UUID): String {
            val config = DeathMessageService.getConfig()
            return config.formatPlayerName(player, wrapFor)
        }

        fun wrapName(player: UUID): String {
            val config = DeathMessageService.getConfig()
            return config.formatPlayerName(player)
        }
    }

    abstract fun getDeathMessage(player: UUID): String

    fun getTimeAgoMillis(): Long {
        return System.currentTimeMillis() - time
    }
}