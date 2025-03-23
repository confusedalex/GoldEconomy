package dev.confusedalex.thegoldeconomy

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.damage.DamageSource
import org.bukkit.damage.DamageType
import org.bukkit.entity.Zombie
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInfo
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock


class RemoveGoldDropsTest {
    private lateinit var server: ServerMock
    private lateinit var plugin: TheGoldEconomy

    @BeforeEach
    fun setUp() {
        server = MockBukkit.mock()
        plugin = MockBukkit.load(TheGoldEconomy::class.java)
    }

    @AfterEach
    fun tearDown(testInfo: TestInfo) { // Stop the mock server
        println(testInfo)
        MockBukkit.unmock()
    }

    @Test
    fun playerDeath() {
        val player = server.addPlayer()
        player.inventory.addItem(ItemStack(Material.GOLD_INGOT))
        player.health = 0.0

        server.pluginManager.firedEvents.forEach {
            if (it is PlayerDeathEvent) {
                assertEquals(true, it.drops.contains(ItemStack(Material.GOLD_INGOT)))
            }
        }
    }

    @Test
    fun entityDeath() {
        var eventHandler = RemoveGoldDrops()
        var world = server.addSimpleWorld("world")
        var location = Location(world, 0.0, 0.0, 0.0)
        var zombie = world.spawn(location, Zombie::class.java)
        var event = EntityDeathEvent(zombie, DamageSource.builder(DamageType.PLAYER_ATTACK).build(), List(0) { ItemStack(Material.GOLDEN_BOOTS) })

        event.drops.add(ItemStack(Material.GOLD_INGOT))
        event.drops.add(ItemStack(Material.GOLD_BLOCK))
        event.drops.add(ItemStack(Material.GOLDEN_HELMET))
        event.drops.add(ItemStack(Material.GOLDEN_PICKAXE))
        event.drops.add(ItemStack(Material.DIAMOND))

        eventHandler.entityDeathEvent(event)

        assertEquals(false, event.drops.contains(ItemStack(Material.GOLD_INGOT)))
        assertEquals(false, event.drops.contains(ItemStack(Material.GOLD_BLOCK)))
        assertEquals(false, event.drops.contains(ItemStack(Material.GOLDEN_HELMET)))
        assertEquals(false, event.drops.contains(ItemStack(Material.GOLDEN_PICKAXE)))
        assertEquals(true, event.drops.contains(ItemStack(Material.DIAMOND)))
    }
}