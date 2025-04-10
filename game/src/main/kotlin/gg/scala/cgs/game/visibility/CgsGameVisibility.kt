package gg.scala.cgs.game.visibility

import org.bukkit.entity.Player
import gg.scala.cgs.game.ScGameFramework
import gg.scala.cgs.game.setting.SettingContainer
import net.evilblock.cubed.visibility.VisibilityAction
import net.evilblock.cubed.visibility.VisibilityAdapter

class CgsGameVisibility : VisibilityAdapter {

    override fun getAction(toRefresh: Player, refreshFor: Player): VisibilityAction {
        val game = ScGameFramework.instance.game ?: return VisibilityAction.NEUTRAL

        val viewerGamePlayer = game.getGamePlayer(refreshFor) ?: return VisibilityAction.NEUTRAL
        val viewerTeamMember = viewerGamePlayer.getAsTeamMember()

        val targetGamePlayer = game.getGamePlayer(toRefresh) ?: return VisibilityAction.NEUTRAL
        val targetTeamMember = targetGamePlayer.getAsTeamMember()

        var shouldShow = false
        if (SettingContainer.get(refreshFor, "SEE_SPECTATORS"))
            shouldShow = viewerTeamMember?.alive == false || viewerTeamMember?.disqualified == true || viewerGamePlayer.spectator

        if ((targetTeamMember?.alive == false || targetTeamMember?.disqualified == true) && !shouldShow) return VisibilityAction.HIDE

        return if (game.isSpectator(toRefresh) && !shouldShow) VisibilityAction.HIDE else VisibilityAction.NEUTRAL
    }

}