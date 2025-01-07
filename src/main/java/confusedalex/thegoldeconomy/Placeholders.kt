package confusedalex.thegoldeconomy;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class Placeholders extends PlaceholderExpansion {
  private final TheGoldEconomy plugin;

  public Placeholders(TheGoldEconomy plugin) {
    this.plugin = plugin;
  }

  @Override
  public @NotNull String getIdentifier() {
    return "thegoldeconomy";
  }

  @Override
  public @NotNull String getAuthor() {
    return "confusedalex";
  }

  @Override
  public @NotNull String getVersion() {
    return plugin.getDescription().getVersion();
  }

  @Override
  public boolean persist() {
    return true;
  }

  @Override
  public String onRequest(OfflinePlayer player, @NotNull String params) {
    if (params.equalsIgnoreCase("inventoryBalance")) {
      return Integer.toString(plugin.eco.converter.getInventoryValue(player.getPlayer()));
    }

    if (params.equalsIgnoreCase("bankBalance")) {
      return Integer.toString(plugin.eco.bank.getAccountBalance(player.getUniqueId()));
    }

    if (params.equalsIgnoreCase("totalBalance")) {
      return Integer.toString(plugin.eco.bank.getTotalPlayerBalance(player.getUniqueId()));
    }

    return null;
  }
}
