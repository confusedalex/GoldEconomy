package dev.confusedalex.thegoldeconomy

import dev.confusedalex.thegoldeconomy.Converter.Companion.deposit
import dev.confusedalex.thegoldeconomy.Converter.Companion.getInventoryValue
import dev.confusedalex.thegoldeconomy.Converter.Companion.getValue
import dev.confusedalex.thegoldeconomy.Converter.Companion.give
import dev.confusedalex.thegoldeconomy.Converter.Companion.isGold
import dev.confusedalex.thegoldeconomy.Converter.Companion.remove
import dev.confusedalex.thegoldeconomy.Converter.Companion.withdraw
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.entity.PlayerMock

class ConverterTest {
    private lateinit var server: ServerMock
    private lateinit var plugin: TheGoldEconomy
    lateinit var give: (Player, Int, Base) -> Unit
    lateinit var remove: (Player, Int, Base) -> Unit
    lateinit var deposit: (Player, Int, Base) -> Unit
    lateinit var withdraw: (Player, Int, Base) -> Unit

    @BeforeEach
    fun setUp() {
        server = MockBukkit.mock()
        plugin = MockBukkit.load(TheGoldEconomy::class.java)

        give = give(plugin.eco, plugin.bundle)
        remove = remove(plugin.eco, plugin.bundle)
        deposit = deposit(plugin.eco, plugin.bundle)
        withdraw = withdraw(plugin.eco, plugin.bundle)
    }

    @AfterEach
    fun tearDown() { // Stop the mock server
        MockBukkit.unmock()
    }

    @Test
    fun getValue_returnsCorrectValues() {
        // nuggets
        assertEquals(1, getValue(Material.GOLD_NUGGET, Base.NUGGETS))
        assertEquals(9, getValue(Material.GOLD_INGOT, Base.NUGGETS))
        assertEquals(81, getValue(Material.GOLD_BLOCK, Base.NUGGETS))
        assertEquals(0, getValue(Material.STONE, Base.NUGGETS))

        // ingots
        assertEquals(0, getValue(Material.GOLD_NUGGET, Base.INGOTS))
        assertEquals(1, getValue(Material.GOLD_INGOT, Base.INGOTS))
        assertEquals(9, getValue(Material.GOLD_BLOCK, Base.INGOTS))
        assertEquals(0, getValue(Material.STONE, Base.INGOTS))

        // raw
        assertEquals(1, getValue(Material.RAW_GOLD, Base.RAW))
        assertEquals(9, getValue(Material.RAW_GOLD_BLOCK, Base.RAW))
        assertEquals(0, getValue(Material.GOLD_BLOCK, Base.RAW))

        // null
        assertEquals(0, getValue(null, Base.NUGGETS))
        assertEquals(0, getValue(null, Base.INGOTS))
        assertEquals(0, getValue(null, Base.RAW))
    }

    @Test
    fun isGold_returnsCorrectValues() {
        assertEquals(true, isGold(Material.GOLD_NUGGET, Base.NUGGETS))
        assertEquals(true, isGold(Material.GOLD_INGOT, Base.NUGGETS))
        assertEquals(true, isGold(Material.GOLD_BLOCK, Base.NUGGETS))

        assertEquals(true, isGold(Material.GOLD_INGOT, Base.INGOTS))
        assertEquals(true, isGold(Material.GOLD_BLOCK, Base.INGOTS))

        assertEquals(true, isGold(Material.RAW_GOLD, Base.RAW))
        assertEquals(true, isGold(Material.RAW_GOLD_BLOCK, Base.RAW))

        assertEquals(false, isGold(Material.STONE, Base.NUGGETS))
        assertEquals(false, isGold(Material.STONE, Base.INGOTS))
        assertEquals(false, isGold(Material.STONE, Base.RAW))

        assertEquals(false, isGold(Material.GOLD_BLOCK, Base.RAW))
        assertEquals(false, isGold(Material.RAW_GOLD, Base.NUGGETS))

        assertEquals(false, isGold(null, Base.NUGGETS))
        assertEquals(false, isGold(null, Base.INGOTS))
        assertEquals(false, isGold(null, Base.RAW))
    }

    @Test
    fun getInventoryValue_withEmptyInventory() {
        val player: PlayerMock = server.addPlayer()

        assertEquals(0, getInventoryValue(player, Base.NUGGETS))
        assertEquals(0, getInventoryValue(player, Base.INGOTS))
        assertEquals(0, getInventoryValue(player, Base.RAW))
    }

    @Test
    fun getInventoryValue_withNonGoldItems() {
        val player: PlayerMock = server.addPlayer()

        player.inventory.addItem(ItemStack(Material.STONE, 8))
        assertEquals(0, getInventoryValue(player, Base.NUGGETS))
        assertEquals(0, getInventoryValue(player, Base.INGOTS))
        assertEquals(0, getInventoryValue(player, Base.RAW))
    }

    @Test
    fun getInventoryValue_withGoldNuggets() {
        val player: PlayerMock = server.addPlayer()

        // Add 5 nuggets
        player.inventory.addItem(ItemStack(Material.GOLD_NUGGET, 5))
        assertEquals(5, getInventoryValue(player, Base.NUGGETS))
        assertEquals(0, getInventoryValue(player, Base.INGOTS))
        assertEquals(0, getInventoryValue(player, Base.RAW))

        // Add raw gold
        player.inventory.addItem(ItemStack(Material.RAW_GOLD, 4))
        assertEquals(5, getInventoryValue(player, Base.NUGGETS))
        assertEquals(0, getInventoryValue(player, Base.INGOTS))
    }

    @Test
    fun getInventoryValue_withGoldIngots() {
        val player: PlayerMock = server.addPlayer()

        // Add 1 ingot
        player.inventory.addItem(ItemStack(Material.GOLD_INGOT, 1))
        assertEquals(9, getInventoryValue(player, Base.NUGGETS))
        assertEquals(1, getInventoryValue(player, Base.INGOTS))
        assertEquals(0, getInventoryValue(player, Base.RAW))

        player.inventory.addItem(ItemStack(Material.RAW_GOLD, 4))
        assertEquals(9, getInventoryValue(player, Base.NUGGETS))
        assertEquals(1, getInventoryValue(player, Base.INGOTS))
    }

    @Test
    fun getInventoryValue_withGoldBlocks() {
        val player: PlayerMock = server.addPlayer()

        player.inventory.addItem(ItemStack(Material.GOLD_BLOCK, 1))
        assertEquals(81, getInventoryValue(player, Base.NUGGETS))
        assertEquals(9, getInventoryValue(player, Base.INGOTS))
        assertEquals(0, getInventoryValue(player, Base.RAW))

        player.inventory.addItem(ItemStack(Material.GOLD_BLOCK, 1)) // sum: 2 Blocks
        assertEquals(162, getInventoryValue(player, Base.NUGGETS))
        assertEquals(18, getInventoryValue(player, Base.INGOTS))
        assertEquals(0, getInventoryValue(player, Base.RAW))

        player.inventory.addItem(ItemStack(Material.RAW_GOLD, 4))
        assertEquals(162, getInventoryValue(player, Base.NUGGETS))
        assertEquals(18, getInventoryValue(player, Base.INGOTS))
    }

    @Test
    fun getInventoryValue_withRawGold() {
        val player: PlayerMock = server.addPlayer()

        player.inventory.addItem(ItemStack(Material.RAW_GOLD, 4))
        assertEquals(0, getInventoryValue(player, Base.NUGGETS))
        assertEquals(0, getInventoryValue(player, Base.INGOTS))
        assertEquals(4, getInventoryValue(player, Base.RAW))

        player.inventory.addItem(ItemStack(Material.GOLD_NUGGET, 1))
        player.inventory.addItem(ItemStack(Material.GOLD_INGOT, 1))
        player.inventory.addItem(ItemStack(Material.GOLD_BLOCK, 1))
        assertEquals(4, getInventoryValue(player, Base.RAW))


        player.inventory.addItem(ItemStack(Material.RAW_GOLD_BLOCK, 1))
        assertEquals(13, getInventoryValue(player, Base.RAW))
    }

    @Test
    fun remove_withSufficientGold() {
        val player: PlayerMock = server.addPlayer()

        player.inventory.addItem(ItemStack(Material.GOLD_NUGGET, 10))
        remove(player, 5, Base.NUGGETS)
        assertEquals(5, getInventoryValue(player, Base.NUGGETS))

        player.inventory.addItem(ItemStack(Material.GOLD_INGOT, 1)) // -> 5 nuggets + 1 ingot = 14 nuggets
        remove(player, 5, Base.NUGGETS)
        assertEquals(9, getInventoryValue(player, Base.NUGGETS))
        assertEquals(1, getInventoryValue(player, Base.INGOTS))

        remove(player, 1, Base.INGOTS)
        assertEquals(0, getInventoryValue(player, Base.NUGGETS))
        assertEquals(0, getInventoryValue(player, Base.INGOTS))
    }

    @Test
    fun remove_withInsufficientGold() {
        val player: PlayerMock = server.addPlayer()

        player.inventory.addItem(ItemStack(Material.GOLD_NUGGET, 3))
        remove(player, 5, Base.NUGGETS)
        assertEquals(3, getInventoryValue(player, Base.NUGGETS))
    }

    @Test
    fun remove_withMixedGoldItems() {
        val player: PlayerMock = server.addPlayer()

        player.inventory.addItem(ItemStack(Material.GOLD_NUGGET, 5))
        player.inventory.addItem(ItemStack(Material.GOLD_INGOT, 1))
        remove(player, 14, Base.NUGGETS)
        assertEquals(0, getInventoryValue(player, Base.NUGGETS))
        assertEquals(0, getInventoryValue(player, Base.INGOTS))
    }

    @Test
    fun remove_withNoGoldItems() {
        val player: PlayerMock = server.addPlayer()

        player.inventory.addItem(ItemStack(Material.STONE, 10))
        remove(player, 5, Base.NUGGETS)
        assertEquals(0, getInventoryValue(player, Base.NUGGETS))
    }

    @Test
    fun deposit_withSufficientGold() {
        val player: PlayerMock = server.addPlayer()

        player.inventory.addItem(ItemStack(Material.GOLD_NUGGET, 10))
        deposit(player, 5, Base.NUGGETS)
        assertEquals(5, getInventoryValue(player, Base.NUGGETS))
        assertEquals(5, plugin.eco.bank.getAccountBalance(player.uniqueId))
    }

    @Test
    fun deposit_withInsufficientGold() {
        val player: PlayerMock = server.addPlayer()
        var base: Base = Base.NUGGETS

        player.inventory.addItem(ItemStack(Material.GOLD_NUGGET, 3))
        deposit(player, 5, base)
        assertEquals(3, getInventoryValue(player, base))
        assertEquals(0, plugin.eco.bank.getAccountBalance(player.uniqueId))

        base = Base.RAW
        player.inventory.addItem(ItemStack(Material.RAW_GOLD_BLOCK, 1))
        deposit(player, 5, base)
        assertEquals(4, getInventoryValue(player, base))
        assertEquals(5, plugin.eco.bank.getAccountBalance(player.uniqueId))
    }

    @Test
    fun deposit_withMixedGoldItems() {
        val player: PlayerMock = server.addPlayer()

        player.inventory.addItem(ItemStack(Material.GOLD_NUGGET, 5))
        player.inventory.addItem(ItemStack(Material.GOLD_INGOT, 1))
        deposit(player, 14, Base.NUGGETS)
        assertEquals(0, getInventoryValue(player, Base.NUGGETS))
        assertEquals(14, plugin.eco.bank.getAccountBalance(player.uniqueId))
    }

    @Test
    fun deposit_withNoGoldItems() {
        val player: PlayerMock = server.addPlayer()

        player.inventory.addItem(ItemStack(Material.STONE, 10))
        deposit(player, 5, Base.NUGGETS)
        assertEquals(0, getInventoryValue(player, Base.NUGGETS))
        assertEquals(0, plugin.eco.bank.getAccountBalance(player.uniqueId))
    }

    @Test
    fun withdraw_withSufficientGold() {
        val player: PlayerMock = server.addPlayer()
        val uuid = player.uniqueId
        val bank = plugin.eco.bank

        bank.setAccountBalance(player.uniqueId, 92)
        withdraw(player, 91, Base.NUGGETS)
        assertEquals(91, getInventoryValue(player, Base.NUGGETS))
        assertEquals(1, bank.getAccountBalance(uuid))
        assertEquals(92, bank.getTotalPlayerBalance(uuid))

        val inventory = player.inventory
        assertEquals(true, inventory.contains(ItemStack(Material.GOLD_NUGGET, 1)))
        assertEquals(true, inventory.contains(ItemStack(Material.GOLD_INGOT, 1)))
        assertEquals(true, inventory.contains(ItemStack(Material.GOLD_BLOCK, 1)))

        // Fill the whole inventory with dirt, test that no gold will be added
        inventory.clear()
        inventory.contents = Array(inventory.size) { ItemStack(Material.DIRT, 64) }
        bank.setAccountBalance(player.uniqueId, 92)
        withdraw(player, 91, Base.NUGGETS)
        assertEquals(0, getInventoryValue(player, Base.NUGGETS))
        assertEquals(1, bank.getAccountBalance(uuid))
        assertEquals(1, bank.getTotalPlayerBalance(uuid))

        assertEquals(false, inventory.contains(ItemStack(Material.GOLD_NUGGET, 1)))
        assertEquals(false, inventory.contains(ItemStack(Material.GOLD_INGOT, 1)))
        assertEquals(false, inventory.contains(ItemStack(Material.GOLD_BLOCK, 1)))
    }

    @Test
    fun withdraw_withInsufficientGold() {
        val player: PlayerMock = server.addPlayer()

        withdraw(player, 5, Base.NUGGETS)
        assertEquals(0, getInventoryValue(player, Base.NUGGETS))
        assertEquals(0, plugin.eco.bank.getAccountBalance(player.uniqueId))
    }
}
