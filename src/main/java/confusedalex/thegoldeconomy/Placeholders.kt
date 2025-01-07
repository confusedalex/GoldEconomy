package confusedalex.thegoldeconomy

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer

class Placeholders(private val plugin: TheGoldEconomy) : PlaceholderExpansion() {
    override fun getIdentifier() = "thegoldeconomy"

    override fun getAuthor() = "confusedalex"

    override fun getVersion() = plugin.description.version

    override fun persist() = true

    override fun onRequest(player: OfflinePlayer, params: String) = when (params.lowercase()) {
        "inventorybalance" -> plugin.eco.converter.getInventoryValue(player.player).toString()
        "bankbalance" -> plugin.eco.bank.getAccountBalance(player.uniqueId).toString()
        "totalbalance" -> plugin.eco.bank.getTotalPlayerBalance(player.uniqueId).toString()
        else -> null
    }
}