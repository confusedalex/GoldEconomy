package dev.confusedalex.thegoldeconomy;

import co.aikar.commands.Locales;
import co.aikar.commands.PaperCommandManager;
import io.papermc.paper.ServerBuildInfo;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;

public class TheGoldEconomy extends JavaPlugin {
    EconomyImplementer eco;
    Util util;
    ResourceBundle bundle;
    public static Base base;
    private VaultHook vaultHook;
    private StorageProvider storageProvider;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        if (isFolia()) {
            getLogger().info("Folia detected.");
        }

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
        localeMap.put("bg_BG", Locales.BULGARIAN);
        localeMap.put("pl_PL", Locales.POLISH);
        localeMap.put("ta", new Locale("ta"));
        localeMap.put("ru", Locales.RUSSIAN);

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
            case "turtle_scute" -> base = Base.TURTLE_SCUTE;
            default -> {
                getLogger().severe(bundle.getString("error.invalidBase"));
                getServer().shutdown();
                return;
            }
        }

        new Metrics(this, 15402);

        storageProvider = createStorageProvider();
        storageProvider.initialize();

        util = new Util(this);
        eco = new EconomyImplementer(this, bundle, util, storageProvider);
        vaultHook = new VaultHook(this, eco);
        vaultHook.hook();

        manager.registerCommand(new BankCommand(eco, bundle, util, this.getConfig()));

        if ("mysql".equalsIgnoreCase(getConfig().getString("storage"))) {
            JsonStorageProvider jsonFallback = new JsonStorageProvider(getDataFolder(), getLogger());
            jsonFallback.initialize();
            manager.registerCommand(new MigrateCommand(this, util, jsonFallback, storageProvider));
        }

        Bukkit.getPluginManager().registerEvents(new Events(eco.bank), this);
        if (getConfig().getBoolean("removeGoldDrop"))
            Bukkit.getPluginManager().registerEvents(new RemoveGoldDrops(), this);

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
        if (eco != null && eco.bank != null) {
            eco.bank.saveAll();
        }

        if (storageProvider != null) {
            storageProvider.shutdown();
        }

        if (vaultHook != null) {
            vaultHook.unhook();
        }

        getLogger().info("TheGoldEconomy disabled.");
    }

    private StorageProvider createStorageProvider() {
        String storageType = getConfig().getString("storage", "json");

        if ("mysql".equalsIgnoreCase(storageType)) {
            ConfigurationSection db = getConfig().getConfigurationSection("database");
            if (db == null) {
                getLogger().severe("storage is set to 'mysql' but there is no 'database' section in config.yml! Falling back to JSON.");
                return new JsonStorageProvider(getDataFolder(), getLogger());
            }

            MysqlConfig mysqlConfig = new MysqlConfig(
                    db.getString("host", "127.0.0.1"),
                    db.getInt("port", 3306),
                    db.getString("name", "thegoldeconomy"),
                    db.getString("username", "root"),
                    db.getString("password", ""),
                    db.getBoolean("use-ssl", false),
                    db.getInt("pool-size", 4)
            );

            return new MysqlStorageProvider(mysqlConfig, getLogger());
        }

        return new JsonStorageProvider(getDataFolder(), getLogger());
    }

    private static boolean isFolia() {
        return ServerBuildInfo.buildInfo().isBrandCompatible(Key.key("papermc", "folia"));
    }
}
