package gg.scala.cgs.common.game

/**
 * @author Topu
 * @date 11/16/2024
 */
enum class GameType(
    val displayName: String,
    val maxPartySize: Int,
    val serverName: String,
    val pluginName: String,
    val description: List<String>,
    val maxTeamSize: Int,
    val maxTeamServers: Int,
    val autoCreateServers: Boolean,
    val publicStats: Boolean,
    val material: String,
    val materialData: Short
) {
    BUNKERS(
        displayName = "Bunkers",
        maxPartySize = 5,
        serverName = "Bunkers",
        pluginName = "Bunkers",
        description = listOf(
            "Teams of four battle it out in",
            "order to become the very best!",
            "Capture KoTHs or make other",
            "teams raidable by killing them!"
        ),
        maxTeamSize = 5,
        maxTeamServers = 2,
        autoCreateServers = false,
        publicStats = true,
        material = "WOOL",
        materialData = 14
    ),
    POT_SG(
        displayName = "PotSG",
        maxPartySize = 2,
        serverName = "PotSG",
        pluginName = "PotSG",
        description = listOf(
            "A battle royale gamemode!",
            "Players search of chests in",
            "abandoned buildings in order to",
            "obtain items to help you",
            "be the last man standing!"
        ),
        maxTeamSize = 1,
        maxTeamServers = 0,
        autoCreateServers = false,
        publicStats = true,
        material = "CHEST",
        materialData = 0
    ),
    SG(
        displayName = "SurvivalGames",
        maxPartySize = 2,
        serverName = "SG",
        pluginName = "SG",
        description = listOf(
            "The return of a classic gamemode!",
            "Players search of chests in",
            "abandoned buildings in order to",
            "obtain items to help you",
            "be the last man standing!"
        ),
        maxTeamSize = 1,
        maxTeamServers = 0,
        autoCreateServers = false,
        publicStats = true,
        material = "RAW_FISH",
        materialData = 0
    ),
    MEETUP(
        displayName = "Meetup",
        maxPartySize = 2,
        serverName = "Meetup",
        pluginName = "UHC-Meetup",
        description = listOf(
            "A fast paced UHC gamemode",
            "Vote for different scenarios and kill",
            "players to obtain their items",
            "and be the last man standing!"
        ),
        maxTeamSize = 2,
        maxTeamServers = 3,
        autoCreateServers = false,
        publicStats = true,
        material = "GOLDEN_APPLE",
        materialData = 0
    ),
    UHC(
        displayName = "UHC",
        maxPartySize = 100,
        serverName = "UHC",
        pluginName = "UHC",
        description = listOf(
            "Gather resources before PvP is enabled",
            "and kill anyone in your way to be",
            "crowned the winner of the game!"
        ),
        maxTeamSize = 100,
        maxTeamServers = 0,
        autoCreateServers = false,
        publicStats = true,
        material = "GOLDEN_APPLE",
        materialData = 0
    ),
    SKYWARS(
        displayName = "SkyWars",
        maxPartySize = 4,
        serverName = "SkyWars",
        pluginName = "SkyWars",
        description = listOf(
            "Fight on floating islands!",
            "Loot chests, eliminate players,",
            "and be the last one standing!"
        ),
        maxTeamSize = 4,
        maxTeamServers = 0,
        autoCreateServers = false,
        publicStats = true,
        material = "FEATHER",
        materialData = 0
    ),
    BEDWARS(
        displayName = "BedWars",
        maxPartySize = 8,
        serverName = "BedWars",
        pluginName = "BedWars",
        description = listOf(
            "Defend your bed and destroy others' beds.",
            "Gather resources, upgrade items,",
            "and eliminate all opponents!"
        ),
        maxTeamSize = 8,
        maxTeamServers = 0,
        autoCreateServers = false,
        publicStats = true,
        material = "BED",
        materialData = 0
    );

    companion object {
        fun getGameByDisplayName(game: String): GameType? {
            return entries.find { it.displayName.equals(game, ignoreCase = true) }
        }
    }
}
