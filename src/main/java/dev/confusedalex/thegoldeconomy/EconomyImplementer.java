package dev.confusedalex.thegoldeconomy;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;

import static dev.confusedalex.thegoldeconomy.TheGoldEconomy.base;

public class EconomyImplementer implements Economy {
    TheGoldEconomy plugin;
    Bank bank;
    Converter converter;
    ResourceBundle bundle;
    Util util;

    public EconomyImplementer(TheGoldEconomy plugin, ResourceBundle bundle, Util util) {
        this.plugin = plugin;
        this.bundle = bundle;
        this.util = util;
        converter = new Converter(this, bundle);
        bank = new Bank(converter);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getName() {
        return "TheGoldEconomy";
    }

    @Override
    public boolean hasBankSupport() {
        return false;
    }

    @Override
    public int fractionalDigits() {
        return 0;
    }

    @Override
    public String format(double amount) {
        return amount + " Gold";
    }

    @Override
    public String currencyNamePlural() {
        return "Gold";
    }

    @Override
    public String currencyNameSingular() {
        return "Gold";
    }

    @Override
    public boolean hasAccount(String playerName) {
        if (util.isOfflinePlayer(playerName).isPresent()) return true;
        if (bank.getFakeAccounts().containsKey(playerName)) return true;

        bank.setFakeAccountBalance(playerName, 0);
        return true;
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        return true;
    }

    @Override
    public boolean hasAccount(String playerName, String worldName) {
        return hasAccount(playerName);
    }

    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName) {
        return hasAccount(player);
    }

    @Override
    public double getBalance(String playerName) {
        try {
            UUID uuid = UUID.fromString(playerName);
            if (Bukkit.getPlayer(uuid) != null) return bank.getTotalPlayerBalance(uuid);
        } catch (IllegalArgumentException e) {
            // String is not UUID
        }
        Optional<OfflinePlayer> playerOptional = util.isOfflinePlayer(playerName);
        return playerOptional.map(offlinePlayer -> bank.getTotalPlayerBalance(offlinePlayer.getUniqueId())).orElseGet(() -> bank.getFakeBalance(playerName));
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        if (player != null) return bank.getTotalPlayerBalance(player.getUniqueId());
        return 0;
    }

    @Override
    public double getBalance(String playerName, String world) {
        return getBalance(playerName);
    }

    @Override
    public double getBalance(OfflinePlayer player, String world) {
        return getBalance(player);
    }

    @Override
    public boolean has(String playerName, double amount) {
        if (util.isOfflinePlayer(playerName).isPresent())
            return amount < bank.getTotalPlayerBalance(Bukkit.getOfflinePlayer(playerName).getUniqueId());
        else return amount < bank.getFakeBalance(playerName);
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return amount < bank.getTotalPlayerBalance(player.getUniqueId());
    }

    @Override
    public boolean has(String playerName, String worldName, double amount) {
        return has(playerName, amount);
    }

    @Override
    public boolean has(OfflinePlayer player, String worldName, double amount) {
        return has(player, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        // if amount is negative return
        if (amount < 0) return new EconomyResponse(amount, 0, EconomyResponse.ResponseType.FAILURE, "error");

        Optional<OfflinePlayer> playerOptional = util.isOfflinePlayer(playerName);
        if (playerOptional.isPresent()) {
            OfflinePlayer offlinePlayer = playerOptional.get();
            UUID uuid = offlinePlayer.getUniqueId();

            // if player is online
            if (offlinePlayer.isOnline()) {
                Player player = offlinePlayer.getPlayer();

                if (player == null)
                    return new EconomyResponse(amount, 0, EconomyResponse.ResponseType.FAILURE, "error");

                // get balance and InventoryValue from Player
                int oldBankBalance = bank.getAccountBalance(uuid);
                int oldInventoryBalance = converter.getInventoryValue(player, base);


                // If balance + InventoryValue is < amount, return
                if (amount > oldBankBalance + oldInventoryBalance)
                    return new EconomyResponse(amount, 0, EconomyResponse.ResponseType.FAILURE, "Not enough money!");
                // If bank balances is enough to cover amount
                if (oldBankBalance - amount > 0) {
                    bank.setAccountBalance(uuid, (int) (oldBankBalance - amount));
                    return new EconomyResponse(amount, (oldBankBalance - amount), EconomyResponse.ResponseType.SUCCESS, "");
                } else {
                    // Set balance to 0 and cover rest of the costs with Inventory Funds
                    int diff = (int) (amount - oldBankBalance);
                    bank.setAccountBalance(uuid, 0);
                    converter.remove(player, diff, base);

                    return new EconomyResponse(amount, oldInventoryBalance - amount, EconomyResponse.ResponseType.SUCCESS, "");
                }
            } else {
                // When player is offline
                int oldBalance = bank.getTotalPlayerBalance(uuid);
                int newBalance = (int) (oldBalance - amount);
                bank.setAccountBalance(uuid, newBalance);
                return new EconomyResponse(amount, newBalance, EconomyResponse.ResponseType.SUCCESS, "");
            }
        } else {
            int oldBalance = bank.getFakeBalance(playerName);
            int newBalance = (int) (oldBalance - amount);
            bank.setFakeAccountBalance(playerName, newBalance);
            return new EconomyResponse(amount, newBalance, EconomyResponse.ResponseType.SUCCESS, "");
        }
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, double amount) {
        UUID uuid = offlinePlayer.getUniqueId();
        Player player;

        // if amount is negative return
        if (amount < 0) return new EconomyResponse(amount, 0, EconomyResponse.ResponseType.FAILURE, "error");

        // if player is online
        if (offlinePlayer.isOnline()) {
            player = offlinePlayer.getPlayer();

            if (player == null)
                return new EconomyResponse(amount, 0, EconomyResponse.ResponseType.FAILURE, "error");

            // get Balance and InventoryValue
            int oldBankBalance = bank.getAccountBalance(uuid);
            int oldInventoryBalance = converter.getInventoryValue(player, base);

            // If balance + InventoryValue is < amount, return
            if (amount > oldBankBalance + oldInventoryBalance)
                return new EconomyResponse(amount, 0, EconomyResponse.ResponseType.FAILURE, "error");
            // If bank balances is enough to cover amount
            if (oldBankBalance - amount > 0) {
                bank.setAccountBalance(uuid, (int) (oldBankBalance - amount));
                return new EconomyResponse(amount, (oldBankBalance - amount), EconomyResponse.ResponseType.SUCCESS, "");
            } else {
                // Set balance to 0 and cover rest of the costs with Inventory Funds
                int diff = (int) (amount - oldBankBalance);
                bank.setAccountBalance(uuid, 0);
                converter.remove(player, diff, base);
                return new EconomyResponse(amount, oldInventoryBalance - amount, EconomyResponse.ResponseType.SUCCESS, "");
            }
        } else {
            // if offline or fakeAccount
            int oldBalance = bank.getTotalPlayerBalance(uuid);
            int newBalance = (int) (oldBalance - amount);
            bank.setAccountBalance(uuid, newBalance);

            return new EconomyResponse(amount, newBalance, EconomyResponse.ResponseType.SUCCESS, "");
        }
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        return withdrawPlayer(playerName, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, String worldName, double amount) {
        return withdrawPlayer(offlinePlayer, amount);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        // If amount is negative -> return
        if (amount < 0) return new EconomyResponse(amount, 0, EconomyResponse.ResponseType.FAILURE, "error");

        Optional<OfflinePlayer> playerOptional = util.isOfflinePlayer(playerName);
        if (playerOptional.isPresent()) {
            OfflinePlayer offlinePlayer = playerOptional.get();
            UUID uuid = offlinePlayer.getUniqueId();

            // Getting balance and calculating new Balance
            int oldBalance = bank.getAccountBalance(uuid);
            int newBalance = (int) (oldBalance + amount);
            bank.setAccountBalance(uuid, newBalance);
            return new EconomyResponse(amount, newBalance, EconomyResponse.ResponseType.SUCCESS, "");
        } else {
            int oldBalance = bank.getFakeBalance(playerName);
            int newBalance = (int) (oldBalance + amount);
            bank.setFakeAccountBalance(playerName, newBalance);
            return new EconomyResponse(amount, newBalance, EconomyResponse.ResponseType.SUCCESS, "");
        }
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        UUID uuid = player.getUniqueId();
        int oldBalance = bank.getAccountBalance(uuid);
        int newBalance = (int) (oldBalance + amount);

        // If amount is negative -> return
        if (amount < 0) return new EconomyResponse(amount, 0, EconomyResponse.ResponseType.FAILURE, "error");

        bank.setAccountBalance(uuid, newBalance);
        return new EconomyResponse(amount, newBalance, EconomyResponse.ResponseType.SUCCESS, "");
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
        return depositPlayer(playerName, amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
        return depositPlayer(player, amount);
    }

    @Override
    public EconomyResponse createBank(String name, String player) {
        return null;
    }

    @Override
    public EconomyResponse createBank(String name, OfflinePlayer player) {
        return null;
    }

    @Override
    public EconomyResponse deleteBank(String name) {
        return null;
    }

    @Override
    public EconomyResponse bankBalance(String name) {
        return null;
    }

    @Override
    public EconomyResponse bankHas(String name, double amount) {
        return null;
    }

    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
        return null;
    }

    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        return null;
    }

    @Override
    public EconomyResponse isBankOwner(String name, String playerName) {
        return null;
    }

    @Override
    public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
        return null;
    }

    @Override
    public EconomyResponse isBankMember(String name, String playerName) {
        return null;
    }

    @Override
    public EconomyResponse isBankMember(String name, OfflinePlayer player) {
        return null;
    }

    @Override
    public List<String> getBanks() {
        return null;
    }

    @Override
    public boolean createPlayerAccount(String playerName) {
        return false;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        return false;
    }

    @Override
    public boolean createPlayerAccount(String playerName, String worldName) {
        return false;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
        return false;
    }
}