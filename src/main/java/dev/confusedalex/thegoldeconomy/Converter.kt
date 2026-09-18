package dev.confusedalex.thegoldeconomy

import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
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

        fun isGold(material: Material?, base: Base): Boolean = getValue(material, base) > 0

        fun getInventoryValue(player: Player?, base: Base): Int =
            player?.inventory?.filterNotNull()?.filter { isGold(it.type, base) }
                ?.sumOf { getValue(it.type, base) * it.amount } ?: 0

        fun remove(util: Util, bundle: ResourceBundle): (Player, Int, Base) -> Unit {
            return fun(player: Player, amount: Int, base: Base) {
                val currentValue = getInventoryValue(player, base)
                // Checks if the value of the items is greater than the amount to deposit
                if (currentValue < amount) return

                player.inventory.filterNotNull().filter { getValue(it.type, base) > 0 }.forEach { item ->
                    item.amount = 0
                    item.type = Material.AIR
                }

                val newBalance = currentValue - amount
                give(util, bundle)(player, newBalance, base)
            }
        }

        fun give(util: Util, bundle: ResourceBundle): (Player, Int, Base) -> Unit {
            return fun(player: Player, value: Int, base: Base) {
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

                if (warning) player.sendMessage(util.formatMessage(String.format(bundle.getString("warning.drops"))))
            }
        }

        fun withdraw(bank: Bank, util: Util, bundle: ResourceBundle): (Player, Int, Base) -> Unit {
            return fun(player: Player, value: Int, base: Base) {
                val uuid = player.uniqueId
                val oldBalance = bank.getAccountBalance(player.uniqueId)

                // Checks balance in hashmap
                if (value > bank.getAccountBalance(uuid)) {
                    player.sendMessage(util.formatMessage(bundle.getString("error.notEnoughMoneyWithdraw")))
                    return
                }
                bank.setAccountBalance(uuid, (oldBalance - value))

                give(util, bundle)(player, value, base)
            }
        }

        fun deposit(bank: Bank, util: Util, bundle: ResourceBundle): (Player, Int, Base) -> Unit {
            return fun(player: Player, value: Int, base: Base) {
                if (value <= 0) return
                if (getInventoryValue(player, base) < value) return

                remove(util, bundle)(player, value, base)
                credit(bank)(player, value)
            }
        }

        fun credit(bank: Bank): (OfflinePlayer, Int) -> Int? {
            return fun(offlinePlayer: OfflinePlayer, amount: Int): Int? {
                if (amount < 0) return null

                val uuid = offlinePlayer.uniqueId
                val newBalance = bank.getAccountBalance(uuid) + amount
                bank.setAccountBalance(uuid, newBalance)
                return newBalance
            }
        }

        fun spend(bank: Bank, util: Util, bundle: ResourceBundle): (OfflinePlayer, Int, Base) -> Int? {
            return fun(offlinePlayer: OfflinePlayer, amount: Int, base: Base): Int? {
                if (amount < 0) return null

                val uuid = offlinePlayer.uniqueId

                if (!offlinePlayer.isOnline) {
                    val newBalance = bank.getTotalPlayerBalance(uuid) - amount
                    bank.setAccountBalance(uuid, newBalance)
                    return newBalance
                }

                val player = offlinePlayer.player ?: return null
                val oldBankBalance = bank.getAccountBalance(uuid)
                val oldInventoryBalance = getInventoryValue(player, base)

                if (amount > oldBankBalance + oldInventoryBalance) return null

                if (oldBankBalance - amount > 0) {
                    val newBalance = oldBankBalance - amount
                    bank.setAccountBalance(uuid, newBalance)
                    return newBalance
                }

                val diff = amount - oldBankBalance
                bank.setAccountBalance(uuid, 0)
                remove(util, bundle)(player, diff, base)
                return oldInventoryBalance - amount
            }
        }
    }
}