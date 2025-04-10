package gg.scala.cgs.game.nametag

import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffectType
import gg.scala.cgs.game.game.GameRule
import gg.scala.cgs.game.ScGameFramework
import net.evilblock.cubed.nametag.NametagInfo
import net.evilblock.cubed.nametag.NametagProvider
import net.evilblock.cubed.util.CC

class FrameworkTag : NametagProvider("CGS NameTag Provider", 100) {

    override fun fetchNametag(target: Player, viewer: Player): NametagInfo {
        val game = ScGameFramework.instance.game

        if (game?.isSpectator(target) == true || game?.getGamePlayer(target)?.getAsTeamMember()?.alive == false)
            return getTag(ChatColor.GRAY.toString() + ChatColor.ITALIC)

        val friendlyTags = game?.getGameRule(GameRule.FRIENDLY_NAMETAGS) ?: false
        val teamColorTags = game?.getGameRule(GameRule.TEAM_COLOR_NAMETAGS) ?: false

        val team = game?.getTeam(target)
            ?: return if (friendlyTags) getTag(ChatColor.RED.toString()) else getTag(ChatColor.RESET.toString())

        if (!game.isSpectator(viewer) && game.getTeam(viewer) != team && target.hasPotionEffect(PotionEffectType.INVISIBILITY))
            return createNametag(CC.GRAY, "")

        return getTag(
            when {
                friendlyTags -> if (game.getTeam(viewer) == team) ChatColor.GREEN.toString() else ChatColor.RED.toString()
                teamColorTags -> team.color
                else -> ChatColor.RESET.toString()
            }
        )
    }

    private fun getTag(prefix: String): NametagInfo = createNametag(prefix, "")

}