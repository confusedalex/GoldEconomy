package confusedalex.thegoldeconomy

import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.plugin.ServicePriority
import java.util.logging.Level

class VaultHook(var plugin: TheGoldEconomy, private var provider: Economy) {
    fun hook() {
        Bukkit.getServicesManager().register(Economy::class.java, this.provider, plugin, ServicePriority.Normal)
        plugin.logger.log(
            Level.INFO, ChatColor.GREEN.toString() + "VaultAPI hooked into " + ChatColor.AQUA + plugin.name
        )
    }

    fun unhook() {
        Bukkit.getServicesManager().unregister(Economy::class.java, this.provider)
        plugin.logger.log(
            Level.INFO, ChatColor.GREEN.toString() + "VaultAPI unhooked from " + ChatColor.AQUA + plugin.name
        )
    }
}
