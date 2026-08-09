package dev.confusedalex.thegoldeconomy

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BundleMeta
import java.util.*

class Converter {
    companion object {
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

        fun getMaterials(base: Base) = when (base) {
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

        private fun isBundle(item: ItemStack): Boolean = item.itemMeta is BundleMeta

        fun isGold(material: Material?, base: Base): Boolean = getValue(material, base) > 0

        private fun expandBundle(item: ItemStack): List<ItemStack> {
            val meta = item.itemMeta
            return if (meta is BundleMeta) meta.items.filterNotNull()
            else listOf(item)
        }

        fun getInventoryValue(items: Iterable<ItemStack?>, base: Base): Int =
            items
                .filterNotNull()
                .flatMap { expandBundle(it) }
                .filter { isGold(it.type, base) }
                .sumOf { getValue(it.type, base) * it.amount }

        fun removeGoldFromBundle(item: ItemStack, base: Base) {
            val meta = item.itemMeta
            if (meta is BundleMeta) {
                val kept = meta.items.filterNotNull().filterNot { isGold(it.type, base) }
                meta.setItems(kept)
                item.itemMeta = meta
            }
        }

        fun remove(eco: EconomyImplementer, bundle: ResourceBundle): (Player, Int, Base) -> Unit {
            return fun(player: Player, amount: Int, base: Base) {
                val totalInventoryValue = getInventoryValue(player.inventory, base)
                // Checks if the value of the items is greater than the amount to deposit
                if (totalInventoryValue < amount) return

                player.inventory.filterNotNull().filter { getValue(it.type, base) > 0 }.forEach { item ->
                    item.amount = 0
                    item.type = Material.AIR
                }

                val newBalance = totalInventoryValue - amount
                give(eco, bundle)(player, newBalance, base)
        fun buildGoldItems(base: Base, value: Int): List<ItemStack> {
            val items = mutableListOf<ItemStack>()

            fun giveMaterial(material: Material, materialValue: Int, value: Int): Int {
                if (value / materialValue > 0) {
                    items.add(ItemStack(material, value / materialValue))
                }
                return value - (value / materialValue) * materialValue
            }

            getMaterials(base).entries
                .fold(value) { acc, entry ->
                    giveMaterial(entry.key, entry.value, acc)
                }

            return items
        }

        fun replaceGoldInBundle(base: Base, item: ItemStack, leftOver: Int) {
            val meta = item.itemMeta
            if (meta is BundleMeta) {
                val nonGold = meta.items.filterNotNull().filterNot { isGold(it.type, base) }
                meta.setItems(nonGold + buildGoldItems(base, leftOver))
                item.itemMeta = meta
            }
        }

        fun give(eco: EconomyImplementer, bundle: ResourceBundle): (Player, Int, Base) -> Unit {
            return fun(player: Player, value: Int, base: Base) {
                var warning = false

                // Set max. stack size to 64, otherwise the stacks will go up to 99
                player.inventory.maxStackSize = 64

                val goldItems = buildGoldItems(base, value);
                val given = player.inventory.addItem(*goldItems.toTypedArray())

                for (item in given.values) {
                    if (item.amount > 0) {
                        player.world.dropItem(player.location, item)
                        warning = true
                    }
                }

                if (warning) player.sendMessage(eco.util.formatMessage(String.format(bundle.getString("warning.drops"))))
            }
        }

        fun withdraw(eco: EconomyImplementer, bundle: ResourceBundle): (Player, Int, Base) -> Unit {
            return fun(player: Player, value: Int, base: Base) {
                val uuid = player.uniqueId
                val oldBalance = eco.bank.getAccountBalance(player.uniqueId)

                // Checks balance in hashmap
                if (value > eco.bank.getAccountBalance(uuid)) {
                    player.sendMessage(eco.util.formatMessage(bundle.getString("error.notEnoughMoneyWithdraw")))
                    return
                }
                eco.bank.setAccountBalance(uuid, (oldBalance - value))

                give(eco, bundle)(player, value, base)
            }
        }

        fun deposit(eco: EconomyImplementer, bundle: ResourceBundle): (Player, Int, Base) -> Unit {
            return fun(player: Player, value: Int, base: Base) {
                if (value <= 0) return
                if (getInventoryValue(player.inventory, base) < value) return
                val op = Bukkit.getOfflinePlayer(player.uniqueId)

                remove(eco, bundle)(player, value, base)
                eco.depositPlayer(op, value.toDouble())
            }
        }
    }
}
