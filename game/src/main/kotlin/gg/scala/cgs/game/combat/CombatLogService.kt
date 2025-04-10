package gg.scala.cgs.game.combat

import gg.scala.cgs.game.deathmessage.DeathMessageConfig
import gg.scala.cgs.game.deathmessage.DeathMessageService
import gg.scala.flavor.service.Service
import java.util.*

/**
 * @author Topu
 * @date 11/17/2024
 */
@Service
object CombatLogService {

    val combatLogMap: MutableMap<UUID, CombatLog> = mutableMapOf()
    private var config: CombatLogConfig = CombatLogConfig.DEFAULT_CONFIG

    fun getConfig(): CombatLogConfig {
        return config
    }

    fun setConfig(newConfig: CombatLogConfig) {
        config = newConfig
    }
}
