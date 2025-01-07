package confusedalex.thegoldeconomy;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.ServicePriority;

import static java.util.logging.Level.INFO;

public class VaultHook {
  TheGoldEconomy plugin;
  Economy provider;

  public VaultHook(TheGoldEconomy plugin, Economy provider) {
    this.plugin = plugin;
    this.provider = provider;
  }

  public void hook() {
    Bukkit.getServicesManager().register(Economy.class, this.provider, plugin, ServicePriority.Normal);
    plugin.getLogger().log(INFO, ChatColor.GREEN + "VaultAPI hooked into " + ChatColor.AQUA + plugin.getName());
  }

  public void unhook() {
    Bukkit.getServicesManager().unregister(Economy.class, this.provider);
    plugin.getLogger().log(INFO, ChatColor.GREEN + "VaultAPI unhooked from " + ChatColor.AQUA + plugin.getName());
  }
}
