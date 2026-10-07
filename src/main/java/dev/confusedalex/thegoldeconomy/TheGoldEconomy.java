package dev.confusedalex.thegoldeconomy;

import co.aikar.commands.PaperCommandManager;
import dev.confusedalex.thegoldeconomy.vault.VaultEconomyImplementer;
import dev.confusedalex.thegoldeconomy.vault.VaultHook;
import dev.confusedalex.thegoldeconomy.vault.VaultUnlockedEconomyImplementer;
import dev.confusedalex.thegoldeconomy.vault.VaultUnlockedHook;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class TheGoldEconomy extends JavaPlugin {
    Bank bank;
    Util util;
    ResourceBundle bundle;
    public static Base base;
    private VaultHook vaultHook;
    private VaultUnlockedHook vaultUnlockedHook;

    @Override
    public void onEnable() {
        // Config
        saveDefaultConfig();

        // Registering Command using ACF
        PaperCommandManager manager = new PaperCommandManager(this);
        manager.enableUnstableAPI("help");

        // Language
        String language = getConfig().getString("language", "en");
        Locale locale = Locale.of(language.split("_")[0]);
        ResourceBundle.Control noFallback = ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES);
        try {
            bundle = ResourceBundle.getBundle("messages", locale, noFallback);
            getLogger().info("Language is " + locale.getLanguage());
        } catch (MissingResourceException e) {
            getLogger().warning("Invalid language '" + language + "' in config. Defaulting to English.");
            locale = Locale.ENGLISH;
            bundle = ResourceBundle.getBundle("messages", locale, noFallback);
        }

        manager.addSupportedLanguage(locale);
        manager.getLocales().addMessageBundle("messages", locale);
        manager.getLocales().setDefaultLocale(locale);

        switch (getConfig().getString("base")) {
            case "nuggets" -> base = Base.NUGGETS;
            case "ingots" -> base = Base.INGOTS;
            case "raw" -> base = Base.RAW;
            case "turtle_scute" -> base = Base.TURTLE_SCUTE;
            default -> {
                getLogger().severe(bundle.getString("error.invalidBase"));
                getServer().shutdown();
            }
        }

        FileUtilsKt.migrateFakeAccounts();

        // bStats
        int pluginId = 15402;
        new Metrics(this, pluginId);

        // Vault shit
        util = new Util(this);
        bank = new Bank();

        vaultHook = new VaultHook(this, new VaultEconomyImplementer(bank, util, bundle));
        vaultHook.hook();

        if (isVaultUnlockedAvailable()) {
            vaultUnlockedHook = new VaultUnlockedHook(this, new VaultUnlockedEconomyImplementer(bank, util, bundle));
            vaultUnlockedHook.hook();
        }

        manager.registerCommand(new BankCommand(bank, bundle, util));

        // Event class registering
        Bukkit.getPluginManager().registerEvents(new Events(this, bank), this);
        // If removeGoldDrop is true, register Listener
        if (getConfig().getBoolean("removeGoldDrop"))
            Bukkit.getPluginManager().registerEvents(new RemoveGoldDrops(), this);

        // Update Checker
        if (getConfig().getBoolean("updateCheck")) {
            new UpdateChecker(this, 102242).getVersion(version -> {
                if (!this.getDescription().getVersion().equals(version)) {
                    getLogger().info(bundle.getString("warning.update"));
                }
            });
        }

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new Placeholders(this).register();
        }
    }

    @Override
    public void onDisable() {
        FileUtilsKt.writeToFiles(bank.getPlayerAccounts());

        if (vaultUnlockedHook != null) vaultUnlockedHook.unhook();
        if (vaultHook != null) vaultHook.unhook();

        getLogger().info("TheGoldEconomy disabled.");
    }

    private static boolean isVaultUnlockedAvailable() {
        try {
            Class.forName("net.milkbowl.vault2.economy.Economy");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
