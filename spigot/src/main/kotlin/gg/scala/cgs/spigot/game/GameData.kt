package gg.scala.cgs.spigot.game

import com.google.common.cache.CacheBuilder
import com.google.common.cache.LoadingCache
import com.google.common.cache.CacheLoader
import org.bson.Document
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import gg.scala.cgs.common.game.GameType
import gg.scala.cgs.spigot.CgsSpigotPlugin
import gg.scala.cgs.spigot.profile.GameProfile
import gg.scala.store.spigot.ScalaDataStoreSpigot
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit

/**
 * @author Topu
 * @date 11/16/2024
 */
object GameData {

    private val gamePlayerCache: LoadingCache<UUID, Map<String, GameProfile>> = CacheBuilder
        .newBuilder()
        .expireAfterWrite(5, TimeUnit.MINUTES)
        .build(object : CacheLoader<UUID, Map<String, GameProfile>>() {
            override fun load(uuid: UUID): Map<String, GameProfile> {
                return loadData(uuid)
            }
        })

    fun getData(uuid: UUID): Map<String, GameProfile> {
        return try {
            gamePlayerCache.get(uuid)
        } catch (e: ExecutionException) {
            emptyMap()
        }
    }

    private fun loadData(uuid: UUID): Map<String, GameProfile> {
        val data = mutableMapOf<String, GameProfile>()

        for (game in GameType.values()) {
            val databaseName = "${game.displayName}-${CgsSpigotPlugin.season}"
            val playerCollection: MongoCollection<Document> =
                ScalaDataStoreSpigot.INSTANCE.client!!.getDatabase(databaseName).getCollection("players")
            val matchesCollection: MongoCollection<Document> =
                ScalaDataStoreSpigot.INSTANCE.client!!.getDatabase(databaseName).getCollection("matches")

            val playerDocument = playerCollection.find(Filters.eq("uuid", uuid.toString())).first()
            playerDocument?.let { document ->
                val gamePlayer = GameProfile().apply {
                    totalSeasonKills = document.getInteger("totalSeasonKills")
                    totalSeasonDeaths = document.getInteger("totalSeasonDeaths")
                    totalSeasonWins = document.getInteger("totalSeasonWins")
                    totalSeasonLoses = document.getInteger("totalSeasonLoses")
                    totalSeasonDamageDealt = document.getDouble("totalSeasonDamageDealt")
                    totalSeasonDamageTaken = document.getDouble("totalSeasonDamageTaken")
                    totalSeasonPlaytime = document.getLong("totalSeasonPlaytime")
                    totalSeasonElo = document.getInteger("totalSeasonElo").toLong()
                    totalSeasonExtraData.putAll(document["totalSeasonExtraData"] as Document)

                    document["totalSeasonGames"]?.let { games ->
                        totalSeasonGames = (games as List<String>).toMutableList()
                        totalSeasonGamesDocuments.addAll(
                            totalSeasonGames.mapNotNull { matchId ->
                                matchesCollection.find(Filters.eq("matchID", matchId)).first()
                            }
                        )
                    }
                }
                data[game.displayName] = gamePlayer
            }
        }

        return data
    }
}
