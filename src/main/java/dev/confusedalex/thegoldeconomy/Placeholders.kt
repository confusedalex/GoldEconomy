package dev.confusedalex.thegoldeconomy

import dev.confusedalex.thegoldeconomy.TheGoldEconomy.base
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

class Placeholders(private val plugin: TheGoldEconomy) : PlaceholderExpansion() {
    override fun getIdentifier() = "thegoldeconomy"

    override fun getAuthor() = "confusedalex"

    override fun getVersion() = plugin.description.version

    override fun persist() = true

    override fun onRequest(player: OfflinePlayer, params: String) = when (params.lowercase()) {
        "bankbalance" -> plugin.eco.bank.getAccountBalance(player.uniqueId).toString()
        "totalbalance" -> plugin.eco.bank.getTotalPlayerBalance(player.uniqueId).toString()
        else -> null
    }

    override fun onPlaceholderRequest(player: Player?, params: String): String? {
        if (player == null) return null;
        return when (params.lowercase()) {
            "inventorybalance" -> Converter.getInventoryValue(player.inventory, base).toString()
            else ->  null
        }
    }
}