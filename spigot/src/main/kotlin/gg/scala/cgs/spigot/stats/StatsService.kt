package gg.scala.cgs.spigot.stats

import gg.scala.flavor.service.Service
import org.bukkit.event.Listener

/**
 * @author Topu
 * @date 11/16/2024
 */
@Service
class StatsService : Listener {

    val providers: MutableList<StatsProvider> = mutableListOf()
    var initiated: Boolean = false

    fun configure() {
        check(!initiated) { "Already initiated" }
        initiated = true
    }

    fun registerProvider(vararg newProvider: StatsProvider) {
        providers.addAll(newProvider)
    }

    fun getProvider(name: String): StatsProvider? {
        return providers.firstOrNull { it.name.equals(name, ignoreCase = true) }
    }
}