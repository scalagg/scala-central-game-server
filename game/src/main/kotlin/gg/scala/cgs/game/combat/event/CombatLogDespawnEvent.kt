package gg.scala.cgs.game.combat.event

import gg.scala.cgs.common.util.BaseEvent
import gg.scala.cgs.game.combat.CombatLog

/**
 * @author Topu
 * @date 11/17/2024
 */
data class CombatLogDespawnEvent(val logger: CombatLog) : BaseEvent()