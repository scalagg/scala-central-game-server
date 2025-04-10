package gg.scala.cgs.spigot.config

import gg.scala.cgs.spigot.CgsSpigotPlugin
import gg.scala.commons.config.annotations.Config

/**
 * @author Topu
 * @date 11/17/2024
 */
@Config("config", crossSync = false)
object CgsSpigotConfig {

    val gameType = CgsSpigotPlugin.gameType.displayName
}