package dev.confusedalex.thegoldeconomy

import dev.confusedalex.thegoldeconomy.TheGoldEconomy.base
import kotlinx.serialization.json.Json
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

class Bank(
    private val converter: Converter,
    private val dbManager: DatabaseManager? = null, // Optional, only if MySQL is enabled
    private val useMySQL: Boolean = false // Set from config
) {
    val playerAccounts: HashMap<String, Int> = Json.decodeFromString(createPlayersFile().readText())
    val fakeAccounts: HashMap<String, Int> = Json.decodeFromString(createFakeAccountsFile().readText())

    fun getTotalPlayerBalance(uuid: UUID): Int {
        val player: Player? = Bukkit.getPlayer(uuid)
        if (player?.isOnline == true) {
            return getAccountBalance(uuid) + converter.getInventoryValue(player, base)
        }
        return getAccountBalance(uuid)
    }

    fun getAccountBalance(uuid: UUID): Int {
        if (useMySQL && dbManager != null) {
            return getAccountBalanceSQL(uuid)
        }
        if (playerAccounts.contains(uuid.toString())) return playerAccounts.getValue(uuid.toString())
        playerAccounts[uuid.toString()] = 0
        return 0
    }

    fun setAccountBalance(uuid: UUID, amount: Int) {
        if (useMySQL && dbManager != null) {
            setAccountBalanceSQL(uuid, amount)
        }
        playerAccounts[uuid.toString()] = amount // Keep the map updated for fallback/caching
    }

    fun setFakeAccountBalance(name: String, amount: Int) {
        if (useMySQL && dbManager != null) {
            setFakeBalanceSQL(name, amount)
        }
        fakeAccounts[name] = amount
    }

    fun getFakeBalance(name: String): Int {
        if (useMySQL && dbManager != null) {
            return getFakeBalanceSQL(name)
        }
        if (fakeAccounts.containsKey(name)) return fakeAccounts.getValue(name)
        fakeAccounts[name] = 0
        return 0
    }

    private fun getAccountBalanceSQL(uuid: UUID): Int {
        val sql = "SELECT balance FROM goldeconomy_bank_balances WHERE uuid = ?"
        dbManager?.getConnection()?.prepareStatement(sql)?.use { stmt ->
            stmt.setString(1, uuid.toString())
            stmt.executeQuery().use { rs ->
                if (rs.next()) {
                    return rs.getInt("balance")
                }
            }
        }
        return 0
    }

    private fun setAccountBalanceSQL(uuid: UUID, balance: Int) {
        val sql = "INSERT INTO goldeconomy_bank_balances (uuid, balance) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE balance = VALUES(balance)"
        dbManager?.getConnection()?.prepareStatement(sql)?.use { stmt ->
            stmt.setString(1, uuid.toString())
            stmt.setInt(2, balance)
            stmt.executeUpdate()
        }
    }

    private fun getFakeBalanceSQL(name: String): Int {
        val sql = "SELECT balance FROM goldeconomy_fake_balances WHERE uuid = ?"
        dbManager?.getConnection()?.prepareStatement(sql)?.use { stmt ->
            stmt.setString(1, name)
            stmt.executeQuery().use { rs ->
                if (rs.next()) {
                    return rs.getInt("balance")
                }
            }
        }
        return 0
    }

    private fun setFakeBalanceSQL(name: String, balance: Int) {
        val sql = "INSERT INTO goldeconomy_fake_balances (uuid, balance) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE balance = VALUES(balance)"
        dbManager?.getConnection()?.prepareStatement(sql)?.use { stmt ->
            stmt.setString(1, name)
            stmt.setInt(2, balance)
            stmt.executeUpdate()
        }
    }

}
