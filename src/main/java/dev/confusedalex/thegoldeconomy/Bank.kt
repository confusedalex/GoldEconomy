package dev.confusedalex.thegoldeconomy

import kotlinx.serialization.json.Json
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*
import kotlin.collections.HashMap

class Bank(private val converter: Converter) {
    val playerAccounts : HashMap<String, Int> = Json.decodeFromString(createPlayersFile().readText())
    val fakeAccounts : HashMap<String, Int> = Json.decodeFromString(createFakeAccountsFile().readText())

    fun getTotalPlayerBalance(uuid: UUID): Int {
        val player: Player? = Bukkit.getPlayer(uuid)

        if (player?.isOnline == true) {
                return getAccountBalance(uuid) + converter.getInventoryValue(player)
        }
        return getAccountBalance(uuid)
    }

    fun getAccountBalance(uuid: UUID): Int {
        if (playerAccounts.contains(uuid.toString())) return playerAccounts.getValue(uuid.toString())

        playerAccounts[uuid.toString()] = 0
        return 0
    }

    fun setAccountBalance(uuid: UUID, amount: Int) {
        playerAccounts[uuid.toString()] = amount
    }

    fun setFakeAccountBalance(s: String, amount: Int) {
        fakeAccounts[s] = amount
    }

    fun getFakeBalance(s: String): Int {
        if (fakeAccounts.containsKey(s)) return fakeAccounts.getValue(s)

        fakeAccounts[s] = 0
        return 0
    }
}