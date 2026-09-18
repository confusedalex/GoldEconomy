package dev.confusedalex.thegoldeconomy

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.entity.PlayerMock
import java.util.*

class BankTest {
    private lateinit var server: ServerMock
    private lateinit var plugin: TheGoldEconomy

    @BeforeEach
    fun setUp() {
        server = MockBukkit.mock()
        plugin = MockBukkit.load(TheGoldEconomy::class.java)
        plugin.bank.playerAccounts.clear()
        plugin.bank.fakeAccounts.clear()
    }

    @AfterEach
    fun tearDown() { // Stop the mock server
        MockBukkit.unmock()
    }

    @Test
    fun getTotalPlayerBalance() {
        val player: PlayerMock = server.addPlayer()
        val uuid = player.uniqueId
        val bank = plugin.bank

        bank.setAccountBalance(uuid, 1000)
        assertEquals(1000, bank.getAccountBalance(uuid))
        assertEquals(1000, bank.getTotalPlayerBalance(uuid))
        Converter.withdraw(plugin.bank, plugin.util, plugin.bundle)(player, 500, Base.NUGGETS)
        assertEquals(500, bank.getAccountBalance(uuid))
        assertEquals(1000, bank.getTotalPlayerBalance(uuid))
        player.disconnect()
        assertEquals(500, bank.getTotalPlayerBalance(uuid))
    }

    @Test
    fun playerAccount() {
        val player: PlayerMock = server.addPlayer()
        val uuid = player.uniqueId
        val bank = plugin.bank

        bank.setAccountBalance(uuid, 1000)
        assertEquals(1000, bank.playerAccounts[uuid.toString()])
        assertEquals(1000, bank.getAccountBalance(uuid))
        assertEquals(0, bank.getAccountBalance(UUID.randomUUID()))
    }

    @Test
    fun fakeAccount() {
        val bank = plugin.bank
        val fakeName = "test-name"

        bank.setFakeAccountBalance(fakeName, 1000)
        assertEquals(1000, bank.fakeAccounts[fakeName])
        assertEquals(0, bank.getFakeBalance("other-name"))
    }
}