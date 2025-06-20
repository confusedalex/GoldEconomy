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
    }

    fun isGold(material: Material?, base: Base): Boolean = when (base) {
        Base.INGOTS, Base.NUGGETS -> when (material) {
            Material.GOLD_BLOCK, Material.GOLD_INGOT, Material.GOLD_NUGGET -> true
            else -> false
        }

        Base.RAW -> when (material) {
            Material.RAW_GOLD, Material.RAW_GOLD_BLOCK -> true
            else -> false
        }
    }

    fun getInventoryValue(player: Player?, base: Base): Int =
        player?.inventory
            ?.filter { it != null }
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
        var value = value
        var warning = false

        val blockValue: Int
        val ingotValue: Int
        val block: Material?
        val ingot: Material?

        when (base) {
            Base.INGOTS, Base.NUGGETS -> {
                block = Material.GOLD_BLOCK
                ingot = Material.GOLD_INGOT
            }

            Base.RAW -> {
                block = Material.RAW_GOLD_BLOCK
                ingot = Material.RAW_GOLD
            }
        }

        blockValue = getValue(block, base)
        ingotValue = getValue(ingot, base)

        // Set max. stack size to 64, otherwise the stacks will go up to 99
        player.inventory.maxStackSize = 64

        if (value / blockValue > 0) {
            val blocks = player.inventory.addItem(ItemStack(block, value / blockValue))
            for (item in blocks.values) {
                if (item != null && item.type == block && item.amount > 0) {
                    player.world.dropItem(player.location, item)
                    warning = true
                }
            }
        }

        value -= (value / blockValue) * blockValue

        if (value / ingotValue > 0) {
            val ingots = player.inventory.addItem(ItemStack(ingot, value / ingotValue))
            for (item in ingots.values) {
                if (item != null && item.type == ingot && item.amount > 0) {
                    player.world.dropItem(player.location, item)
                    warning = true
                }
            }
        }

        value -= (value / ingotValue) * ingotValue

        if (base == Base.NUGGETS && value > 0) {
            val nuggets = player.inventory.addItem(ItemStack(Material.GOLD_NUGGET, value))
            for (item in nuggets.values) {
                if (item != null && item.type == Material.GOLD_NUGGET && item.amount > 0) {
                    player.world.dropItem(player.location, item)
                    warning = true
                }
            }
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