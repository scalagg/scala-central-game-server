package gg.scala.cgs.spigot.profile

import org.bson.Document

/**
 * @author Topu
 * @date 11/16/2024
 */
data class GameProfile(
    var joinTime: Long = System.currentTimeMillis(),
    var totalSeasonKills: Int = 0,
    var totalSeasonDeaths: Int = 0,
    var totalSeasonLoses: Int = 0,
    var totalSeasonWins: Int = 0,
    var totalSeasonDamageTaken: Double = 0.0,
    var totalSeasonDamageDealt: Double = 0.0,
    var totalSeasonPlaytime: Long = 0L,
    var totalSeasonElo: Long = 0,
    var totalSeasonGames: MutableList<String> = mutableListOf(),
    var totalSeasonGamesDocuments: MutableList<Document> = mutableListOf(),
    var totalSeasonExtraData: MutableMap<String, Any> = mutableMapOf()
) {

    fun getExtraSeasonData(key: String): Any? {
        return totalSeasonExtraData[key]
    }

    fun getExtraSeasonDataString(key: String, defaultValue: String? = null): String? {
        return totalSeasonExtraData[key] as? String ?: defaultValue
    }

    fun getExtraSeasonDataInteger(key: String, defaultValue: Int = 0): Int {
        return totalSeasonExtraData[key] as? Int ?: defaultValue
    }
}
