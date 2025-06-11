package dev.confusedalex.thegoldeconomy

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection

enum class DatabaseType {
    MYSQL, MARIADB, POSTGRESQL
}

class DatabaseManager(
    private val type: DatabaseType,
    private val host: String,
    private val port: String,
    private val database: String,
    private val username: String,
    private val password: String,
    private val poolEnabled: Boolean = true,
    private val maxPoolSize: Int = 5,
    private val connectionTimeout: Long = 30000,
    private val idleTimeout: Long = 600000,
    private val maxLifetime: Long = 1800000
) {
    private val dataSource: HikariDataSource?

    init {
        dataSource = if (poolEnabled) {
            val config = HikariConfig().apply {
                jdbcUrl = when (type) {
                    DatabaseType.MYSQL -> "jdbc:mysql://$host:$port/$database"
                    DatabaseType.MARIADB -> "jdbc:mariadb://$host:$port/$database"
                    DatabaseType.POSTGRESQL -> "jdbc:postgresql://$host:$port/$database"
                }
                username = this@DatabaseManager.username
                password = this@DatabaseManager.password
                maximumPoolSize = this@DatabaseManager.maxPoolSize
                connectionTimeout = this@DatabaseManager.connectionTimeout
                idleTimeout = this@DatabaseManager.idleTimeout
                maxLifetime = this@DatabaseManager.maxLifetime
                if (type == DatabaseType.MYSQL || type == DatabaseType.MARIADB) {
                    addDataSourceProperty("cachePrepStmts", "true")
                    addDataSourceProperty("prepStmtCacheSize", "250")
                    addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
                }
            }
            HikariDataSource(config)
        } else {
            null
        }
    }

    fun getConnection(): Connection {
        if (dataSource != null) {
            return dataSource.connection
        } else {
            // Fallback to non-pooled connection (not recommended for production)
            val url = when (type) {
                DatabaseType.MYSQL -> "jdbc:mysql://$host:$port/$database"
                DatabaseType.MARIADB -> "jdbc:mariadb://$host:$port/$database"
                DatabaseType.POSTGRESQL -> "jdbc:postgresql://$host:$port/$database"
            }
            val props = java.util.Properties().apply {
                setProperty("user", username)
                setProperty("password", password)
            }
            return java.sql.DriverManager.getConnection(url, props)
        }
    }

    fun close() {
        dataSource?.close()
    }
}
