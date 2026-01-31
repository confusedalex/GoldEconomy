package dev.confusedalex.thegoldeconomy

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

class Converter(var eco: EconomyImplementer, var bundle: ResourceBundle) {
    fun getValue(material: Material?, base: Base): Int = when (base) {
        Base.NUGGETS -> when (material) {
            Material.GOLD_NUGGET -> 1
            Material.GOLD_INGOT -> 9
            Material.GOLD_BLOCK -> 81
            else -> 0
        }

        Base.INGOTS -> when (material) {
            Material.GOLD_INGOT -> 1
            Material.GOLD_BLOCK -> 9
            else -> 0
        }

        Base.RAW -> when (material) {
            Material.RAW_GOLD -> 1
            Material.RAW_GOLD_BLOCK -> 9
            else -> 0
        }

        Base.TURTLE_SCUTE -> when (material) {
            Material.TURTLE_SCUTE -> 1
            else -> 0
        }
    }

    fun isGold(material: Material?, base: Base): Boolean = getValue(material, base) > 0

    fun getInventoryValue(player: Player?, base: Base): Int =
        player?.inventory
            ?.filterNotNull()
            ?.filter { isGold(it.type, base) }
            ?.sumOf { getValue(it.type, base) * it.amount } ?: 0

    fun remove(player: Player, amount: Int, base: Base) {
        val value = getInventoryValue(player, base)
        // Checks if the value of the items is greater than the amount to deposit
        if (value < amount) return

        player.inventory.filterNotNull().filter { getValue(it.type, base) > 0 }.forEach { item ->
            item.amount = 0
            item.type = Material.AIR
        }

        val newBalance = value - amount
        give(player, newBalance, base)
    }

    fun give(player: Player, value: Int, base: Base) {
        var warning = false

        val materials = when (base) {
            Base.NUGGETS -> {
                linkedMapOf(
                    Material.GOLD_BLOCK to getValue(Material.GOLD_BLOCK, base),
                    Material.GOLD_INGOT to getValue(Material.GOLD_INGOT, base),
                    Material.GOLD_NUGGET to getValue(Material.GOLD_NUGGET, base)
                )
            }
            Base.INGOTS -> {
                 linkedMapOf(
                     Material.GOLD_BLOCK to getValue(Material.GOLD_BLOCK, base),
                     Material.GOLD_INGOT to getValue(Material.GOLD_INGOT, base),
                )
            }
            Base.RAW -> {
                linkedMapOf(
                   Material.RAW_GOLD_BLOCK to getValue(Material.RAW_GOLD_BLOCK, base),
                   Material.RAW_GOLD to getValue(Material.RAW_GOLD, base),
                )
            }
            Base.TURTLE_SCUTE -> {
                linkedMapOf(
                    Material.TURTLE_SCUTE to getValue(Material.TURTLE_SCUTE, base),
                )
            }
        }

        // Set max. stack size to 64, otherwise the stacks will go up to 99
        player.inventory.maxStackSize = 64

        fun removeMaterial(material: Material, materialValue: Int, value: Int): Int {
            if (value / materialValue > 0) {
                val itemMaterials = player.inventory.addItem(ItemStack(material, value / materialValue))
                for (item in itemMaterials.values) {
                    if (item != null && item.type == material && item.amount > 0) {
                        player.world.dropItem(player.location, item)
                        warning = true
                    }
                }
            }
            return value - (value / materialValue) * materialValue
        }

        materials.entries.fold(value) { acc, entry ->
            removeMaterial(entry.key, entry.value, acc)
        }

        if (warning) player.sendMessage(eco.util.formatMessage(String.format(bundle.getString("warning.drops"))))
    }

    fun withdraw(player: Player, nuggets: Int, base: Base) {
        val uuid = player.uniqueId
        val oldBalance = eco.bank.getAccountBalance(player.uniqueId)

        // Checks balance in hashmap
        if (nuggets > eco.bank.getAccountBalance(uuid)) {
            player.sendMessage(eco.util.formatMessage(bundle.getString("error.notEnoughMoneyWithdraw")))
            return
        }
        eco.bank.setAccountBalance(uuid, (oldBalance - nuggets))

        give(player, nuggets, base)
    }

    fun deposit(player: Player, nuggets: Int, base: Base) {
        if (nuggets <= 0) return
        if (getInventoryValue(player, base) < nuggets) return
        val op = Bukkit.getOfflinePlayer(player.uniqueId)

        remove(player, nuggets, base)
        eco.depositPlayer(op, nuggets.toDouble())
    }
}