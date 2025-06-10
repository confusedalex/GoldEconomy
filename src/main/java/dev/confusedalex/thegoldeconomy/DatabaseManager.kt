package dev.confusedalex.thegoldeconomy;

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.*

class DatabaseManager(
    private val host: String,
    private val port: String,
    private val database: String,
    private val username: String,
    private val password: String
) {
    private var connection: Connection? = null

    @Throws(SQLException::class)
    fun connect() {
        if (connection != null && !connection!!.isClosed) return
        Class.forName("com.mysql.cj.jdbc.Driver")
        val url = "jdbc:mysql://$host:$port/$database"
        val props = Properties().apply {
            setProperty("user", username)
            setProperty("password", password)
        }
        connection = DriverManager.getConnection(url, props)
    }

    @Throws(SQLException::class)
    fun getConnection(): Connection {
        if (connection == null || connection!!.isClosed) connect()
        return connection!!
    }

    @Throws(SQLException::class)
    fun disconnect() {
        connection?.takeIf { !it.isClosed }?.close()
    }
}
