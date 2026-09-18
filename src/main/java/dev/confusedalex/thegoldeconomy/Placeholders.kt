package dev.confusedalex.thegoldeconomy

import dev.confusedalex.thegoldeconomy.TheGoldEconomy.base
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer

class Placeholders(private val plugin: TheGoldEconomy) : PlaceholderExpansion() {
    override fun getIdentifier() = "thegoldeconomy"

    override fun getAuthor() = "confusedalex"

    override fun getVersion() = plugin.description.version

    override fun persist() = true

    override fun onRequest(player: OfflinePlayer, params: String) = when (params.lowercase()) {
        "inventorybalance" -> Converter.getInventoryValue(player.player, base).toString()
        "bankbalance" -> plugin.bank.getAccountBalance(player.uniqueId).toString()
        "totalbalance" -> plugin.bank.getTotalPlayerBalance(player.uniqueId).toString()
        else -> null
    }
}