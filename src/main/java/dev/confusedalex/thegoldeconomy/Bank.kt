package dev.confusedalex.thegoldeconomy

import dev.confusedalex.thegoldeconomy.TheGoldEconomy.base
import kotlinx.serialization.json.Json
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class Bank {
    val playerAccounts: ConcurrentHashMap<String, Int> =
        ConcurrentHashMap(Json.decodeFromString<Map<String, Int>>(createPlayersFile().readText()))

    fun getTotalPlayerBalance(uuid: UUID): Int {
        val player: Player? = Bukkit.getPlayer(uuid)

        if (player?.isOnline == true) {
            return getAccountBalance(uuid) + Converter.getInventoryValue(player, base)
        }
        return getAccountBalance(uuid)
    }

    fun getAccountBalance(uuid: UUID): Int = playerAccounts.getOrPut(uuid.toString()) { 0 }

    fun setAccountBalance(uuid: UUID, amount: Int) {
        playerAccounts[uuid.toString()] = amount
    }

    fun addToAccount(uuid: UUID, amount: Int): Int = playerAccounts.merge(uuid.toString(), amount, Int::plus)!!

    fun removeFromAccount(uuid: UUID, amount: Int): Boolean {
        var removed = false
        playerAccounts.compute(uuid.toString()) { _, balance ->
            val current = balance ?: 0
            if (current >= amount) {
                removed = true
                current - amount
            } else current
        }
        return removed
    }

    fun takeFromAccount(uuid: UUID, amount: Int): Int {
        var taken = 0
        playerAccounts.compute(uuid.toString()) { _, balance ->
            val current = balance ?: 0
            taken = minOf(current, amount)
            current - taken
        }
        return taken
    }
}
