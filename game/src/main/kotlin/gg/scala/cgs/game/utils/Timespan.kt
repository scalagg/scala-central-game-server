package gg.scala.cgs.game.utils

import java.util.concurrent.TimeUnit

class Timespan(val amount: Long, val unit: TimeUnit) {

    companion object {
        @JvmStatic
        fun of(unit: TimeUnit, amount: Long) = Timespan(amount, unit)
    }

    fun toMillis() = unit.toMillis(amount)
    fun toSeconds() = unit.toSeconds(amount)

}