package gg.scala.cgs.game.combat

import gg.scala.cgs.game.deathmessage.DeathMessageService
import gg.scala.lemon.util.QuickAccess
import net.evilblock.cubed.util.CC
import java.util.*

/**
 * @author Topu
 * @date 11/17/2024
 */
interface CombatLogConfig {

    companion object {
        val DEFAULT_CONFIG: CombatLogConfig = object : CombatLogConfig {
            override fun formatPlayerName(user: UUID): String {
                return if (DeathMessageService.getConfig() != null) {
                    "${DeathMessageService.getConfig().formatPlayerName(user)}${CC.GRAY} (Combat-Logger)"
                } else {
                    "${CC.RED}${QuickAccess.nameOrConsole(user)}${CC.GRAY} (Combat-Logger)"
                }
            }
        }
    }

    fun formatPlayerName(user: UUID): String

    fun formatPlayerName(user: UUID, formatFor: UUID): String {
        return formatPlayerName(user)
    }
}
