package gg.scala.cgs.game.deathmessage

import gg.scala.lemon.util.QuickAccess
import net.evilblock.cubed.util.CC
import java.util.*

/**
 * @author Topu
 * @date 11/17/2024
 */
interface DeathMessageConfig {

    companion object {
        val DEFAULT_CONFIG = object : DeathMessageConfig {
            override fun shouldShowDeathMessage(checkFor: UUID, died: UUID, killer: UUID): Boolean {
                return true
            }

            override fun formatPlayerName(player: UUID): String {
                return "${CC.RED}${QuickAccess.nameOrConsole(player)}"
            }

            override fun extraMessage(player: UUID): String {
                return "slain by"
            }
        }
    }

    fun shouldShowDeathMessage(checkFor: UUID, died: UUID, killer: UUID): Boolean

    fun formatPlayerName(player: UUID): String

    fun formatPlayerName(player: UUID, formatFor: UUID): String {
        return formatPlayerName(player)
    }

    fun extraMessage(player: UUID): String {
        return "slain by"
    }

    fun hideWeapons(): Boolean {
        return true
    }
}
