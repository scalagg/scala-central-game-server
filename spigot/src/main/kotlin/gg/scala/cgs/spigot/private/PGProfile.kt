package gg.scala.cgs.spigot.private

import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.player.LemonPlayer
import java.util.*


/**
 * @author Topu
 * @date 4/14/2025
 */

class PGProfile(
    val uuid: UUID? = null,
    val permissions: Map<String, String> = HashMap(),
    val allowedAccess: Boolean = false,
    val removed: Boolean = false,
    val moderator: Boolean = false,
    val grantedAccessBy: UUID? = null,
    val removedBy: UUID? = null
) {

    fun getProfile(): LemonPlayer? {
        return PlayerHandler.find(uuid!!)
    }

    fun getRemovedByProfile(): LemonPlayer? {
        return PlayerHandler.find(removedBy!!)
    }

    fun getGrantedByProfile(): LemonPlayer? {
        return PlayerHandler.find(grantedAccessBy!!)
    }
}
