package dev.confusedalex.thegoldeconomy

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection
import java.util.*
import java.util.logging.Logger

class MysqlStorageProvider(private val config: MysqlConfig, private val logger: Logger) : StorageProvider {

    private var dataSource: HikariDataSource? = null

    override fun initialize() {
        val hikari = HikariConfig().apply {
            jdbcUrl = "jdbc:mysql://${config.host}:${config.port}/${config.database}?useSSL=${config.useSsl}&allowPublicKeyRetrieval=true&serverTimezone=UTC"
            username = config.username
            password = config.password
            driverClassName = "com.mysql.cj.jdbc.Driver"
            maximumPoolSize = config.poolSize
            minimumIdle = 1
            connectionTimeout = 10_000
            idleTimeout = 600_000
            maxLifetime = 1_800_000
            poolName = "GoldEconomy-HikariPool"
        }

        dataSource = HikariDataSource(hikari)

        connection { conn ->
            conn.createStatement().use { stmt ->
                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS gold_economy_balances (
                        uuid VARCHAR(36) NOT NULL PRIMARY KEY,
                        balance INT NOT NULL DEFAULT 0
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                """.trimIndent())
                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS gold_economy_fake_accounts (
                        name VARCHAR(255) NOT NULL PRIMARY KEY,
                        balance INT NOT NULL DEFAULT 0
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                """.trimIndent())
            }
        }

        logger.info("Connected to MySQL/MariaDB at ${config.host}:${config.port}/${config.database}")
    }

    override fun shutdown() {
        dataSource?.close()
    }

    override fun loadPlayerAccounts(): Map<String, Int> {
        val map = HashMap<String, Int>()
        connection { conn ->
            conn.prepareStatement("SELECT uuid, balance FROM gold_economy_balances").use { ps ->
                ps.executeQuery().use { rs ->
                    while (rs.next()) {
                        map[rs.getString("uuid")] = rs.getInt("balance")
                    }
                }
            }
        }
        return map
    }

    override fun loadFakeAccounts(): Map<String, Int> {
        val map = HashMap<String, Int>()
        connection { conn ->
            conn.prepareStatement("SELECT name, balance FROM gold_economy_fake_accounts").use { ps ->
                ps.executeQuery().use { rs ->
                    while (rs.next()) {
                        map[rs.getString("name")] = rs.getInt("balance")
                    }
                }
            }
        }
        return map
    }

    override fun savePlayerAccount(uuid: String, balance: Int) {
        connection { conn ->
            conn.prepareStatement(
                "INSERT INTO gold_economy_balances (uuid, balance) VALUES (?, ?) ON DUPLICATE KEY UPDATE balance = VALUES(balance)"
            ).use { ps ->
                ps.setString(1, uuid)
                ps.setInt(2, balance)
                ps.executeUpdate()
            }
        }
    }

    override fun saveFakeAccount(name: String, balance: Int) {
        connection { conn ->
            conn.prepareStatement(
                "INSERT INTO gold_economy_fake_accounts (name, balance) VALUES (?, ?) ON DUPLICATE KEY UPDATE balance = VALUES(balance)"
            ).use { ps ->
                ps.setString(1, name)
                ps.setInt(2, balance)
                ps.executeUpdate()
            }
        }
    }

    override fun savePlayerAccounts(accounts: Map<String, Int>) {
        connection { conn ->
            conn.prepareStatement(
                "INSERT INTO gold_economy_balances (uuid, balance) VALUES (?, ?) ON DUPLICATE KEY UPDATE balance = VALUES(balance)"
            ).use { ps ->
                conn.autoCommit = false
                for ((uuid, balance) in accounts) {
                    ps.setString(1, uuid)
                    ps.setInt(2, balance)
                    ps.addBatch()
                }
                ps.executeBatch()
                conn.commit()
                conn.autoCommit = true
            }
        }
    }

    override fun saveFakeAccounts(accounts: Map<String, Int>) {
        connection { conn ->
            conn.prepareStatement(
                "INSERT INTO gold_economy_fake_accounts (name, balance) VALUES (?, ?) ON DUPLICATE KEY UPDATE balance = VALUES(balance)"
            ).use { ps ->
                conn.autoCommit = false
                for ((name, balance) in accounts) {
                    ps.setString(1, name)
                    ps.setInt(2, balance)
                    ps.addBatch()
                }
                ps.executeBatch()
                conn.commit()
                conn.autoCommit = true
            }
        }
    }

    private fun connection(block: (Connection) -> Unit) {
        try {
            dataSource?.connection?.use { conn -> block(conn) }
        } catch (e: Exception) {
            logger.severe("Database error: ${e.message}")
            e.printStackTrace()
        }
    }
}

data class MysqlConfig(
    val host: String,
    val port: Int,
    val database: String,
    val username: String,
    val password: String,
    val useSsl: Boolean,
    val poolSize: Int
)
