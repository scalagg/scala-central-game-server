package gg.scala.cgs.game.game

import com.google.gson.Gson
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import com.mongodb.client.model.ReplaceOptions
import org.bson.Document
import org.bukkit.Bukkit
import org.bukkit.ChatColor.*
import org.bukkit.entity.Player
import gg.scala.cgs.game.event.GameStartEvent
import gg.scala.cgs.game.map.Map
import gg.scala.cgs.game.map.MapVote
import gg.scala.cgs.game.player.GamePlayer
import gg.scala.cgs.game.ScGameFramework
import gg.scala.cgs.game.scoreboard.BoardBuilder
import gg.scala.cgs.game.scoreboard.BoardProvider
import gg.scala.cgs.game.scoreboard.FrameworkBoard
import gg.scala.cgs.game.team.Team
import gg.scala.cgs.game.utils.BukkitUtils
import gg.scala.cgs.spigot.CgsSpigotPlugin
import gg.scala.parties.model.Party
import gg.scala.store.spigot.ScalaDataStoreSpigot
import net.evilblock.cubed.scoreboard.ScoreboardAdapter
import net.evilblock.cubed.scoreboard.ScoreboardHandler
import net.evilblock.cubed.visibility.VisibilityHandler
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListMap
import java.util.concurrent.TimeUnit
import java.util.function.Predicate
import kotlin.random.Random

open class Game(val gameName: String, val serverName: String, val gameType: String, val votingSecondsIn: Int, val startingSecondsIn: Int) {
    constructor(gameName: String, serverName: String, gameType: String, votingSecondsIn: Int): this(gameName, serverName, gameType, votingSecondsIn, 15)
    constructor(gameName: String, serverName: String, gameType: String): this(gameName, serverName, gameType, 30)

    @Transient open var gameStage = GameStage.SETUP

    open var gameAnnounceTitle = ""
    var matchID = ""
    var gameStartedAt = -1L
    var gameEndedAt = -1L

    @Transient lateinit var playerSettings: PlayerSettings
    @Transient lateinit var boardSettings: BoardSettings

    lateinit var activeMap: Map
        private set

    @Transient var votingEnabled = false
    @Transient var timerPaused = false

    @Transient lateinit var mapVote: MapVote

    val gamePlayers: MutableMap<UUID, GamePlayer> = ConcurrentHashMap()
    val teams: MutableMap<String, Team> = ConcurrentSkipListMap()
    val deathLog = arrayListOf<String>()
    lateinit var mostValuablePlayer: UUID
    lateinit var mostDamageInflicted: UUID

    @Transient val maps: MutableMap<String, Map> = ConcurrentHashMap()
    val gameRules: MutableMap<GameRule, Boolean> = ConcurrentHashMap()

    @Transient val scoreboards: MutableMap<GameStage, BoardProvider> = ConcurrentHashMap()

    @Transient var votingSecondsLeft = votingSecondsIn + 1 // Need to add 1 second so messages display correctly
        set
    @Transient var startingSecondsLeft = startingSecondsIn + 1 // Need to add 1 second so messages display correctly
        set
    @Transient var playingSeconds = 0
        set

    @Transient
    var canAnnounce = true

    fun setBoardFromBuilder(builder: BoardBuilder) {
        builder.providers.forEach { setBoardStage(it) }
    }

    fun setBoardStage(provider: BoardProvider) {
        scoreboards[provider.forStage] = provider
    }


    private fun createMongoIndexes()
    {
        Bukkit.getLogger().info("Creating indexes...")
        // Create MongoDB Indexes
        val playerCollection = ScalaDataStoreSpigot.INSTANCE.client!!.getDatabase("${gameName}-${CgsSpigotPlugin.season}").getCollection("players") as MongoCollection<Document>
        playerCollection.createIndex(Document("uuid", 1))
        playerCollection.createIndex(Document("totalSeasonKills", 1))
        playerCollection.createIndex(Document("totalSeasonDeaths", 1))
        playerCollection.createIndex(Document("totalSeasonWins", 1))
        playerCollection.createIndex(Document("totalSeasonElo", 1))
        playerCollection.createIndex(Document("totalSeasonGames", 1))
        playerCollection.createIndex(Document("totalSeasonWins", 1))
        playerCollection.createIndex(Document("totalSeasonLoses", 1))

        Bukkit.getLogger().info("Creating indexes done.")

    }

    fun resetFrameworkData()
    {
        this.deathLog.clear()
        gamePlayers.clear()
        teams.clear()

    }

    fun addTeams(vararg teams: Team) {
        for (team in teams) {
            this.teams[team.name] = team
        }
    }

    fun addMaps(vararg maps: Map) {
        for (map in maps) {
            this.maps[map.name] = map
        }
    }

    fun getActiveMapName(): String {
        if (!::activeMap.isInitialized)
            return "None"
        return activeMap.name
    }

    fun isVoteActive() = gameStage == GameStage.VOTING
    fun isCountdownActive() = gameStage == GameStage.STARTING

    fun createGamePlayer(player: Player, spectator: Boolean = false) = createGamePlayer(player.uniqueId, spectator)
    fun createGamePlayer(uuid: UUID, spectator: Boolean = false): GamePlayer {
        var player = getGamePlayer(uuid)

        if (player == null) {
            player = constructGamePlayer(uuid)
            player.spectator = spectator
            gamePlayers[uuid] = player
            loadPlayerData(player)
        }

        player.getPlayer()?.let { VisibilityHandler.update(it) }
        return player
    }

    private fun loadPlayerData(gamePlayer: GamePlayer) {
        Bukkit.getScheduler().runTaskAsynchronously(ScGameFramework.instance) {
            val playerCollection = ScalaDataStoreSpigot.INSTANCE.client!!.getDatabase("${gameName}-${CgsSpigotPlugin.season}").getCollection("players") as MongoCollection<Document>
            val playerDocument = playerCollection.find(Filters.eq("uuid", gamePlayer.uuid.toString())).first()

            if(playerDocument != null) {
                if(playerDocument.containsKey("totalSeasonKills")) gamePlayer.totalSeasonKills = playerDocument.getInteger("totalSeasonKills")
                if(playerDocument.containsKey("totalSeasonDeaths")) gamePlayer.totalSeasonDeaths = playerDocument.getInteger("totalSeasonDeaths")
                if(playerDocument.containsKey("totalSeasonWins")) gamePlayer.totalSeasonWins = playerDocument.getInteger("totalSeasonWins")
                if(playerDocument.containsKey("totalSeasonElo")) {
                    gamePlayer.totalSeasonElo = playerDocument.getInteger("totalSeasonElo", 1400)
                    gamePlayer.loadedGameElo = playerDocument.getInteger("totalSeasonElo", 1400)
                }
                if(playerDocument.containsKey("totalSeasonLoses")) gamePlayer.totalSeasonLoses = playerDocument.getInteger("totalSeasonLoses")
                if(playerDocument.containsKey("totalSeasonDamageDealt")) gamePlayer.totalSeasonDamageDealt = playerDocument.get("totalSeasonDamageDealt") as Double
                if(playerDocument.containsKey("totalSeasonDamageTaken")) gamePlayer.totalSeasonDamageTaken = playerDocument.get("totalSeasonDamageTaken") as Double
                if(playerDocument.containsKey("totalSeasonPlaytime")) gamePlayer.totalSeasonPlaytime = playerDocument.get("totalSeasonPlaytime") as Long
                if(playerDocument.containsKey("totalSeasonGames")) gamePlayer.totalSeasonGames = playerDocument.get("totalSeasonGames") as ArrayList<String>
                if(playerDocument.containsKey("totalSeasonExtraData")) gamePlayer.totalSeasonExtraData.putAll(playerDocument.get("totalSeasonExtraData") as Document);
                println("Loaded ${gamePlayer.uuid}")
            }
        }
    }

    open fun constructGamePlayer(uuid: UUID) = GamePlayer(uuid)

    fun removeGamePlayer(player: Player) = gamePlayers.remove(player.uniqueId)
    fun removeGamePlayer(uuid: UUID) = gamePlayers.remove(uuid)
    fun removeGamePlayerIf(player: Player, predicate: Predicate<GamePlayer>) = removeGamePlayerIf(player.uniqueId, predicate)

    fun removeGamePlayerIf(uuid: UUID, predicate: Predicate<GamePlayer>) {
        val player = getGamePlayer(uuid) ?: return
        if (!predicate.test(player)) return
        gamePlayers.remove(uuid)
    }

    fun getGamePlayer(player: Player) = getGamePlayer(player.uniqueId)
    fun getGamePlayer(uuid: UUID) = gamePlayers.values.find { it.isPlayer(uuid) }


    fun getGamePlayerSafe(player: Player) = getGamePlayerSafe(player.uniqueId)
    fun getGamePlayerSafe(uuid: UUID): GamePlayer? {
        val gamePlayer = getGamePlayer(uuid)

        if (gamePlayer == null)
            Bukkit.getPlayer(uuid)?.kickPlayer("${RED}Your profile failed to load!")

        return gamePlayer
    }

    fun getPlayers() = gamePlayers.values.filter { !it.spectator }
    fun getPlayerCount() = gamePlayers.values.count { !it.spectator }
    fun getSpectators() = gamePlayers.values.filter { it.spectator && it.getPlayer() != null }
    fun getSpectatorCount() = gamePlayers.values.count { it.spectator && it.getPlayer() != null }
    fun isSpectator(player: Player) = gamePlayers.values.any { it.spectator && it.isPlayer(player.uniqueId) }
    fun isSpectator(uuid: UUID) = gamePlayers.values.any { it.spectator && it.isPlayer(uuid) }

    fun isDead(player: Player) = getGamePlayer(player)?.getAsTeamMember()?.alive == false
    fun isDead(uuid: UUID) = getGamePlayer(uuid)?.getAsTeamMember()?.alive == false

    fun getGameRule(rule: GameRule) = gameRules.getOrDefault(rule, false)

    fun setGameRule(rule: GameRule, value: Boolean) {
        gameRules[rule] = value
    }

    fun setGameRule(value: Boolean, rule: Array<GameRule>) {
        for (r in rule) {
            gameRules[r] = value
        }
    }

    fun getPlayerTeams() = teams.values.filter { !it.systemTeam }
    fun getSystemTeams() = teams.values.filter { it.systemTeam }

    fun getTeamByName(name: String) = teams.values.find { it.name.equals(name, true) }
    fun getTeamByNameOrMember(name: String) =
        teams.values.find { it.name.equals(name, true) || Bukkit.getPlayer(name)?.let { p -> it.isMember(p.uniqueId) } == true }

    fun getTeam(player: GamePlayer) = getTeam(player.uuid)
    fun getTeam(player: Player) = getTeam(player.uniqueId)
    fun getTeam(uuid: UUID) = getPlayerTeams().find { it.isMember(uuid) }

    fun addToTeam(party: Party) {
        var targetTeam = getPlayerTeams().find { it.getOpenSlots() >= (party.members.size + 1) }

        if (targetTeam == null)
            targetTeam = getPlayerTeams().sortedByDescending { it.getOpenSlots() }[0]

        for (member in party.members.values.toMutableList()
            .apply { add(party.leader) }) {
            if (getTeam(member.uniqueId) != null) continue

            if (!targetTeam.addMember(member.uniqueId)) {
                Bukkit.getPlayer(member.uniqueId)?.let {
                    it.sendMessage("${RED}Failed to add you to the same team as your Party.")
                    addToTeam(it)
                }
            }
        }
    }

    open fun addToTeam(player: Player) {
        val filtered = getPlayerTeams().filter { if(getGameRule(GameRule.ADD_TO_EMPTY_TEAM)) it.members.size == 0 else it.getOpenSlots() > 0  }
        val targetTeam = if (filtered.isEmpty()) null else filtered.sortedByDescending { it.getOpenSlots() }[0]

        if (targetTeam == null && !player.hasPermission("basic.admin")) {
            player.kickPlayer("${RED}Failed to find a team for you!")
            return
        }

        if (targetTeam == null) {
            player.sendMessage("${RED}${ITALIC}We couldn't find a team for you - you would normally be kicked, but this check is being bypassed due to your rank.")
            return
        }

        targetTeam.addMember(player.uniqueId)
    }

    fun removeFromTeam(player: Player) = removeFromTeam(player.uniqueId)

    fun removeFromTeam(uuid: UUID) {
        getPlayerTeams().forEach { it.removeMember(uuid) }
    }

    fun initialize() {
        log("Setting up $gameName Game...")

        createMongoIndexes()

        if (!::playerSettings.isInitialized)
            throw IllegalStateException("PlayerSettings must be set before initializing the game.")
        if (!::boardSettings.isInitialized)
            throw IllegalStateException("BoardSettings must be set before initializing the game.")

        // Set up the server
        BukkitUtils.setMaxPlayers(playerSettings.maxPlayers)

        ScoreboardHandler.configure(FrameworkBoard())

        // Initialize teams & maps
        teams.values.forEach { it.initialize() }
        maps.values.forEach { it.initialize() }

        if (maps.isEmpty())
            throw IllegalStateException("There must be at least 1 Map to initialize the game.")

        if (maps.size < 2) // Force disable voting if there is only one map
            votingEnabled = false

        gameStage = GameStage.PRE_GAME

        // Provide a unique match id used for website purposes
        matchID = UUID.randomUUID().toString().split("-").toTypedArray()[0]

        log("Game has been setup with id: $matchID, waiting for players..")
    }

    open fun tick() {
        if (gameStage == GameStage.IN_GAME)
            getPlayers().forEach { it.tick() }
    }

    open fun fullTick() {
        when (gameStage) {
            GameStage.VOTING -> {
                votingSecondsLeft--

                if ((votingSecondsLeft in 1..15 && (votingSecondsLeft % 5 == 0 || votingSecondsLeft <= 5)) || votingSecondsLeft == 30)
                    Bukkit.broadcastMessage("${YELLOW}Map voting will end in ${LIGHT_PURPLE}$votingSecondsLeft second${if (votingSecondsLeft == 1) "" else "s"}${YELLOW}.")

                if (votingSecondsLeft <= 0)
                    selectMap()
            }

            GameStage.STARTING -> {
                startingSecondsLeft--

                if (startingSecondsLeft in 1..15 && (startingSecondsLeft % 5 == 0 || startingSecondsLeft <= 5))
                    Bukkit.broadcastMessage("${YELLOW}The game will begin in ${LIGHT_PURPLE}$startingSecondsLeft second${if (startingSecondsLeft == 1) "" else "s"}${YELLOW}.")

                if (startingSecondsLeft <= 0)
                    startGame()
            }

            GameStage.IN_GAME -> {
                if(!timerPaused)
                    playingSeconds++

                getPlayers().forEach { it.fullTick() }
            }

            else -> {
            }
        }
    }

    open fun startGame() {
        gameStage = GameStage.IN_GAME

        teams.values.forEach { it.startGame() }
        activeMap.startGame()
        Bukkit.getPluginManager().callEvent(GameStartEvent())
        gameStartedAt = System.currentTimeMillis()
    }

    open fun startVoting() {
        if (!votingEnabled) {
            setMap(maps.values.toList()[0])
            startCountdown()
            postMapLoad()
            return
        }

        if (!::mapVote.isInitialized)
            mapVote = MapVote()
        mapVote.reset()

        gameStage = GameStage.VOTING
    }

    open fun postMapLoad() {}

    open fun selectMap() {
        val score = mapVote.countVotes()
        val selectedMap: Map

        if (score.isEmpty()) {
            selectedMap = maps.values.toList()[0]
            Bukkit.broadcastMessage("${LIGHT_PURPLE}${selectedMap.name} ${YELLOW}has been chosen as no one voted.")
        } else {
            if (score.size == 1) {
                selectedMap = maps[score[0].topic.name]!!
                Bukkit.broadcastMessage("${LIGHT_PURPLE}${selectedMap.name} ${YELLOW}has been chosen with ${GREEN}${score.toList()[0].amount} votes${YELLOW}.")
            } else {
                val first = score.toList()[0]
                val second = score.toList()[1]

                if (first.amount == second.amount) {
                    selectedMap = if (Random.nextBoolean()) maps[first.topic.name]!! else maps[second.topic.name]!!
                    Bukkit.broadcastMessage("${LIGHT_PURPLE}${selectedMap.name} ${YELLOW}has been randomly chosen because there was a tie.")
                } else {
                    selectedMap = maps[first.topic.name]!!
                    Bukkit.broadcastMessage("${LIGHT_PURPLE}${selectedMap.name} ${YELLOW}has been chosen with ${GREEN}${first.amount} votes${YELLOW}.")
                }
            }
        }

        setMap(selectedMap)
        startCountdown()
        postMapLoad()
    }

    open fun saveData(game: Game, extraDataDocument: Document?): Boolean {
        gameEndedAt = System.currentTimeMillis()
        mostValuablePlayer = gamePlayers.values.maxByOrNull { gamePlayer -> gamePlayer.kills }!!.uuid
        mostDamageInflicted = gamePlayers.values.maxByOrNull { gamePlayer -> gamePlayer.damageDealt }!!.uuid
        gamePlayers.values.filter { gamePlayer -> gamePlayer.isOnline() }.forEach { gamePlayer -> gamePlayer.updatePlaytime() }
        gamePlayers.values.sortedBy { gamePlayer -> gamePlayer.placedAt }

        try {
            val gameCollection = ScalaDataStoreSpigot.INSTANCE.client!!.getDatabase("${gameName}-${CgsSpigotPlugin.season}").getCollection("matches") as MongoCollection<Document>

            val gson = Gson()
            val jsonObj = gson.toJsonTree(game).asJsonObject

            val document = Document.parse(jsonObj.toString())
            if(document == null) {
                log("Failed to create document based on Game")
                return false
            }
            if (extraDataDocument != null) {
                document.putAll(extraDataDocument)
            }
            val updateResult = gameCollection.replaceOne(
                Filters.eq("uuid", matchID),
                document,
                ReplaceOptions().upsert(true)
            )
            log(if(updateResult.wasAcknowledged()) "Successfully saved $matchID to the mongo: ${gameCollection.namespace.fullName}!" else "Failed to save $matchID to the mongo: ${gameCollection.namespace.fullName}, was not acknowledged.")
            return updateResult.wasAcknowledged()
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    open fun savePlayerData(gamePlayer: GamePlayer, extraDataDocument: Document?): Boolean {
        // Add a cooldown to prevent the player to join a new game whilst we're still saving data.
//        CgsSpigotPlugin.addGameCooldown(gamePlayer.uuid, 3, TimeUnit.SECONDS)

        try {
            val playerCollection = ScalaDataStoreSpigot.INSTANCE.client!!.getDatabase("${gameName}-${CgsSpigotPlugin.season}").getCollection("players") as MongoCollection<Document>
            gamePlayer.updatePlaytime()
            val document = Document()
                .append("uuid", gamePlayer.uuid.toString())
                .append("totalSeasonKills", gamePlayer.kills + gamePlayer.totalSeasonKills)
                .append("totalSeasonDeaths", gamePlayer.deaths + gamePlayer.totalSeasonDeaths)
                .append("totalSeasonWins", if(gamePlayer.hasWon) gamePlayer.totalSeasonWins + 1 else gamePlayer.totalSeasonWins)
                .append("totalSeasonLoses", if(!gamePlayer.hasWon) gamePlayer.totalSeasonLoses + 1 else gamePlayer.totalSeasonLoses)
                .append("totalSeasonDamageDealt", gamePlayer.damageDealt + gamePlayer.totalSeasonDamageDealt)
                .append("totalSeasonDamageTaken", gamePlayer.damageTaken + gamePlayer.totalSeasonDamageTaken)
                .append("totalSeasonPlaytime", gamePlayer.totalSeasonPlaytime + gamePlayer.getCurrentSessionPlaytime())
                .append("totalSeasonElo", gamePlayer.totalSeasonElo)
                .append("totalSeasonGames", gamePlayer.addSeasonGame(matchID))
                .append("totalSeasonExtraData", gamePlayer.totalSeasonExtraData)

            if (extraDataDocument != null) {
                document.putAll(extraDataDocument)
            }
            val updateResult = playerCollection.replaceOne(
                Filters.eq("uuid", gamePlayer.uuid.toString()),
                document,
                ReplaceOptions().upsert(true)
            )
            log(if(updateResult.wasAcknowledged()) "Successfully saved ${gamePlayer.uuid} data to the mongo: ${playerCollection.namespace.fullName}!" else "Failed to save ${gamePlayer.uuid} data to the mongo: ${playerCollection.namespace.fullName}, was not acknowledged.")
            return updateResult.wasAcknowledged()
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    open fun startCountdown() {
        gameStage = GameStage.STARTING
    }

    fun setMap(map: Map) {
        map.activate()
        activeMap = map
    }

    fun logDeath(msg: String) = deathLog.add(msg)

    fun log(msg: String) = ScGameFramework.instance.logger.info(msg)
}