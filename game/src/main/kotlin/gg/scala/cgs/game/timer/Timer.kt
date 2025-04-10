package gg.scala.cgs.game.timer

abstract class Timer(val name: String, val displayName: String, val hidden: Boolean) {

    fun getVisibleName(): String? = if (hidden) null else displayName
    fun isVisible() = !hidden

}