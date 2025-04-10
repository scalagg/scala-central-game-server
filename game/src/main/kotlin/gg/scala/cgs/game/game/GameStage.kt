package gg.scala.cgs.game.game

enum class GameStage(val displayName: String) {
    SETUP("Setup"),
    PRE_GAME("Pre-game"),
    VOTING("Voting"),
    STARTING("Starting"),
    IN_GAME("In-game"),
    REBOOTING("Rebooting")
}