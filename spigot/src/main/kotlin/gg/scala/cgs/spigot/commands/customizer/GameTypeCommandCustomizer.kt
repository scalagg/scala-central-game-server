package gg.scala.cgs.spigot.commands.customizer

import gg.scala.cgs.common.game.GameType
import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.annotations.commands.customizer.CommandManagerCustomizer
import gg.scala.commons.command.ScalaCommandManager
import net.evilblock.cubed.util.CC

/**
 * @author Topu
 * @date 11/16/2024
 */
object GameTypeCommandCustomizer
{

    @CommandManagerCustomizer
    fun customize(manager: ScalaCommandManager) {
        manager.commandContexts
            .registerContext(
                GameType::class.java
            ) {
                val argument = it.popFirstArg()

                return@registerContext GameType.entries
                    .firstOrNull { type ->
                        type.name.equals(argument, true)
                    }
                    ?: throw ConditionFailedException(
                        "No game type with the name ${CC.YELLOW}$argument${CC.RED} was found."
                    )
            }
    }
}