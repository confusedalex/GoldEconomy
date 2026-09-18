package dev.confusedalex.thegoldeconomy;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;

import static dev.confusedalex.thegoldeconomy.TheGoldEconomy.base;

public class EconomyImplementer implements Economy {
    Bank bank;
    ResourceBundle bundle;
    Util util;

    public EconomyImplementer(Bank bank, Util util, ResourceBundle bundle) {
        this.bank = bank;
        this.util = util;
        this.bundle = bundle;
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
        if (playerOptional.isPresent()) return withdrawPlayer(playerOptional.get(), amount);

        int newBalance = (int) (bank.getFakeBalance(playerName) - amount);
        bank.setFakeAccountBalance(playerName, newBalance);
        return new EconomyResponse(amount, newBalance, EconomyResponse.ResponseType.SUCCESS, "");
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, double amount) {
        // if amount is negative return
        if (amount < 0) return new EconomyResponse(amount, 0, EconomyResponse.ResponseType.FAILURE, "error");

        Integer newBalance = Converter.Companion.spend(bank, util, bundle).invoke(offlinePlayer, (int) amount, base);
        if (newBalance == null) return new EconomyResponse(amount, 0, EconomyResponse.ResponseType.FAILURE, "error");

        return new EconomyResponse(amount, newBalance, EconomyResponse.ResponseType.SUCCESS, "");
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
        if (playerOptional.isPresent()) return depositPlayer(playerOptional.get(), amount);

        int newBalance = (int) (bank.getFakeBalance(playerName) + amount);
        bank.setFakeAccountBalance(playerName, newBalance);
        return new EconomyResponse(amount, newBalance, EconomyResponse.ResponseType.SUCCESS, "");
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        Integer newBalance = Converter.Companion.credit(bank).invoke(player, (int) amount);
        if (newBalance == null) return new EconomyResponse(amount, 0, EconomyResponse.ResponseType.FAILURE, "error");

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