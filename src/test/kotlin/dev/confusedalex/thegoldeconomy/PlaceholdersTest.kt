package dev.confusedalex.thegoldeconomy

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInfo
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock

class PlaceholdersTest {
    private lateinit var server: ServerMock
    private lateinit var plugin: TheGoldEconomy

    @BeforeEach
    fun setUp() {
        // Start the mock server
        server = MockBukkit.mock()
        // Load your plugin
        plugin = MockBukkit.load(TheGoldEconomy::class.java)
    }

    @AfterEach
    fun tearDown(testInfo: TestInfo) { // Stop the mock server
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
}