package dev.confusedalex.thegoldeconomy

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock

class PlaceholdersTest {
    private lateinit var server: ServerMock
    private lateinit var plugin: TheGoldEconomy

    @BeforeEach
    fun setUp() {
        server = MockBukkit.mock()
        plugin = MockBukkit.load(TheGoldEconomy::class.java)
    }

    @AfterEach
    fun tearDown() { // Stop the mock server
        MockBukkit.unmock()
    }

    @Test
    fun getIdentifier() {
        val placeholders = Placeholders(plugin)
        assertEquals("thegoldeconomy", placeholders.identifier)
    }

    @Test
    fun getAuthor() {
        val placeholders = Placeholders(plugin)
        assertEquals("confusedalex", placeholders.author)
    }

    @Test
    fun getVersion() {
        val placeholders = Placeholders(plugin)
        assertEquals(plugin.pluginMeta.version, placeholders.version)
    }

    @Test
    fun persist() {
        val placeholders = Placeholders(plugin)
        assertTrue(placeholders.persist())
    }

    @Test
    fun onRequest() {
        val placeholders = Placeholders(plugin)
        val player = server.addPlayer()
        val uuid = player.uniqueId
        val converter = plugin.eco.converter
        val bank = plugin.eco.bank

        bank.setAccountBalance(uuid, 1000)
        assertEquals("0", placeholders.onRequest(player, "inventorybalance"))
        assertEquals("1000", placeholders.onRequest(player, "bankbalance"))
        assertEquals("1000", placeholders.onRequest(player, "totalbalance"))
        converter.withdraw(player, 500, Base.NUGGETS)
        assertEquals("500", placeholders.onRequest(player, "inventorybalance"))
        assertEquals("500", placeholders.onRequest(player, "bankbalance"))
        assertEquals("1000", placeholders.onRequest(player, "totalbalance"))

        assertEquals(null, placeholders.onRequest(player, "someNoneExistingPlaceholder"))
    }
}