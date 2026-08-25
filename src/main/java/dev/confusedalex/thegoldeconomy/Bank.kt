package dev.confusedalex.thegoldeconomy

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class Bank(private val storage: StorageProvider) {

    val playerAccounts: ConcurrentHashMap<String, Int> = ConcurrentHashMap()
    val fakeAccounts: ConcurrentHashMap<String, Int> = ConcurrentHashMap()

    init {
        playerAccounts.putAll(storage.loadPlayerAccounts())
        fakeAccounts.putAll(storage.loadFakeAccounts())
    }

    fun getTotalPlayerBalance(uuid: UUID): Int {
        val player: Player? = Bukkit.getPlayer(uuid)

        if (player?.isOnline == true) {
            return getAccountBalance(uuid) + Converter.getInventoryValue(player, TheGoldEconomy.base)
        }
        return getAccountBalance(uuid)
    }

    fun getAccountBalance(uuid: UUID): Int {
        if (playerAccounts.containsKey(uuid.toString())) return playerAccounts.getValue(uuid.toString())

        playerAccounts[uuid.toString()] = 0
        return 0
    }

    fun setAccountBalance(uuid: UUID, amount: Int) {
        playerAccounts[uuid.toString()] = amount
        storage.savePlayerAccount(uuid.toString(), amount)
    }

    fun setFakeAccountBalance(s: String, amount: Int) {
        fakeAccounts[s] = amount
        storage.saveFakeAccount(s, amount)
    }

    fun getFakeBalance(s: String): Int {
        if (fakeAccounts.containsKey(s)) return fakeAccounts.getValue(s)

        fakeAccounts[s] = 0
        return 0
    }

    fun saveAll() {
        storage.savePlayerAccounts(HashMap(playerAccounts))
        storage.saveFakeAccounts(HashMap(fakeAccounts))
    }
}
