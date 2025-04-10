package gg.scala.cgs.game.game

enum class GameRule(val displayName: String) {
    FRIENDLY_FIRE("Friendly Fire"),
    FRIENDLY_NAMETAGS("Friendly Nametags"),
    TEAM_COLOR_NAMETAGS("Team Color Nametags"),
    ADD_TO_EMPTY_TEAM("Add to Empty Team"),
    SHOW_HEALTH("Show Health"),
    BOW_HEALTH_MESSAGE("Bow Health Message"),
    IGNORE_MIN_PLAYER_LIMIT("Ignore Min Player Limit"),
    ALLOW_DAMAGE_IN_ALL_STAGES("Allow Damage in All Stages"),
    DISABLE_PRE_GAME_LISTENER("Disable Pre Game Listener");
}