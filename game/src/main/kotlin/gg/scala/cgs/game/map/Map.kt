package gg.scala.cgs.game.map

open class Map(val name: String) {
    private var initialized: Boolean = false

    open fun initialize() {
        if (initialized)
            throw IllegalStateException("Map already initialized")

        initialized = true
    }

    open fun startGame() {}

    open fun activate() {}
}