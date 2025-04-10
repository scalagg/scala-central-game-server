package gg.scala.cgs.game.deathmessage.damage

import net.evilblock.cubed.util.CC
import java.util.*

/**
 * @author Topu
 * @date 11/17/2024
 */
class UnknownDamage(
    damaged: UUID,
    damage: Double
) : Damage(damaged, damage) {

    override fun getDeathMessage(getFor: UUID): String {
        return "${wrapName(damaged, getFor)}${CC.YELLOW} died."
    }
}
