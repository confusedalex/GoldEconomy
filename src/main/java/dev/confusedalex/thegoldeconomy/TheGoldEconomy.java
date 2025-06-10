package dev.confusedalex.thegoldeconomy;

import co.aikar.commands.Locales;
import co.aikar.commands.PaperCommandManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;

public class TheGoldEconomy extends JavaPlugin {
    EconomyImplementer eco;
    Util util;
    ResourceBundle bundle;
    public static Base base;
    private VaultHook vaultHook;
    private DatabaseManager dbManager;

    @Override
    public void onEnable() {
        // Config
        saveDefaultConfig();

        // Registering Command using ACF
        PaperCommandManager manager = new PaperCommandManager(this);
        manager.enableUnstableAPI("help");

        // Language
        String language = getConfig().getString("language");
        HashMap<String, Locale> localeMap = new HashMap<>();
        localeMap.put("de_DE", Locales.GERMAN);
        localeMap.put("en_US", Locales.ENGLISH);
        localeMap.put("zh_CN", Locales.SIMPLIFIED_CHINESE);
        localeMap.put("es_ES", Locales.SPANISH);
        localeMap.put("tr_TR", Locales.TURKISH);
        localeMap.put("pt_BR", Locales.PORTUGUESE);
        localeMap.put("nb_NO", Locales.NORWEGIAN_BOKMAAL);
        localeMap.put("uk", Locales.UKRANIAN);
        localeMap.put("jp_JP", Locales.JAPANESE);
        // Don't enable the tamil language, because encoding seems broken.
        // localeMap.put("ta", new Locale("ta"));

        if (localeMap.containsKey(language)) {
            Locale locale = localeMap.get(language);
            bundle = ResourceBundle.getBundle("messages", locale);
            manager.addSupportedLanguage(locale);
            manager.getLocales().addMessageBundle("messages", locale);
            manager.getLocales().addMessageBundles("messages");
            manager.getLocales();
            manager.getLocales().setDefaultLocale(locale);
        } else {
            bundle = ResourceBundle.getBundle("messages", Locale.US);
            getLogger().warning("Invalid language in config. Defaulting to English.");
        }

        switch (getConfig().getString("base")) {
            case "nuggets" -> base = Base.NUGGETS;
            case "ingots" -> base = Base.INGOTS;
            case "raw" -> base = Base.RAW;
            default -> {
                getLogger().severe(bundle.getString("error.invalidBase"));
                getServer().shutdown();
            }
        }

        // bStats
        int pluginId = 15402;
        new Metrics(this, pluginId);

        // Mysql
        boolean useMySQL = getConfig().getBoolean("MySQL", false);
        this.dbManager = useMySQL ?
                new DatabaseManager(
                        getConfig().getString("mysql.host"),
                        getConfig().getString("mysql.port"),
                        getConfig().getString("mysql.database"),
                        getConfig().getString("mysql.user"),
                        getConfig().getString("mysql.password")
                ) : null;

        // Table creation shit
        if (useMySQL && dbManager != null) {
            try (Connection conn = dbManager.getConnection();
                 Statement stmt = conn.createStatement()) {
                String sql = "CREATE TABLE IF NOT EXISTS goldeconomy_bank_balances (" +
                        "uuid VARCHAR(36) PRIMARY KEY, " +
                        "balance BIGINT NOT NULL DEFAULT 0" +
                        ")";
                stmt.execute(sql);

                String fakeSql = "CREATE TABLE IF NOT EXISTS goldeconomy_fake_balances (" +
                        "uuid VARCHAR(64) PRIMARY KEY, " + // Or use 'name' if you prefer
                        "balance BIGINT NOT NULL DEFAULT 0" +
                        ")";
                stmt.execute(fakeSql);

            } catch (SQLException e) {
                getLogger().severe("Failed to create bank balances or fake balances table: " + e.getMessage());
            }
        }

        // Vault shit
        util = new Util(this);
        eco = new EconomyImplementer(this, bundle, util, dbManager, useMySQL);

        vaultHook = new VaultHook(this, eco);
        vaultHook.hook();

        manager.registerCommand(new BankCommand(this));

        // Event class registering
        Bukkit.getPluginManager().registerEvents(new Events(eco.bank), this);
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
        FileUtilsKt.writeToFiles(eco.bank.getPlayerAccounts(), eco.bank.getFakeAccounts());

        vaultHook.unhook();

        getLogger().info("TheGoldEconomy disabled.");
    }
}
