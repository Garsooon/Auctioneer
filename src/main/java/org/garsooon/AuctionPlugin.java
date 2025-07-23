package org.garsooon;

import org.bukkit.Bukkit;
import org.garsooon.Commands.AuctionCommand;
import org.garsooon.Commands.AuctionResetCommand;
import org.garsooon.Commands.BidCommand;
import org.garsooon.Economy.Method;
import org.garsooon.Economy.Methods;
import org.bukkit.plugin.java.JavaPlugin;
import org.garsooon.Listener.PlayerJoinListener;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.*;

import static org.bukkit.Bukkit.getLogger;

@SuppressWarnings({"ResultOfMethodCallIgnored", "CallToPrintStackTrace", "IOStreamConstructor"})
public class AuctionPlugin extends JavaPlugin {
    private static AuctionPlugin instance;
    private AuctionManager auctionManager;
    private Map<String, Object> config;
    @SuppressWarnings("FieldCanBeLocal")
    private Method economy;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfigYaml();
        loadConfigYaml();

        // Load economy before AuctionManager
        boolean economyLoaded = Methods.setMethod(getServer().getPluginManager());

        if (!economyLoaded || Methods.getMethod() == null) {
            getLogger().severe("[Auctioneer] No compatible economy plugin found! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        } else {
            this.economy = Methods.getMethod();
            getLogger().info("[Auctioneer] Economy method loaded: " + economy.getName() + " v" + economy.getVersion());
        }

        // Initialize commands
        auctionManager = new AuctionManager(this, this.economy);
        getCommand("auction").setExecutor(new AuctionCommand(this));
        getCommand("bid").setExecutor(new BidCommand(this));
        getCommand("auctionreset").setExecutor(new AuctionResetCommand(this));

        // Register listener
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this, auctionManager), this);

        getServer().getLogger().info("[Auctioneer] Plugin enabled.");

        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> auctionManager.cleanupStuckAuction(), 20L * 30, 20L * 30); // 30 Seconds
    }

    @Override
    public void onDisable() {
        getServer().getLogger().info("[Auctioneer] Plugin disabled.");
    }

    private void saveDefaultConfigYaml() {
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            getDataFolder().mkdirs();
            try (InputStream in = getClass().getResourceAsStream("/config.yml");
                 OutputStream out = new FileOutputStream(configFile)) {
                if (in == null) {
                    System.out.println("Default config.yml missing from JAR!");
                    return;
                }
                byte[] buffer = new byte[1024];
                int len;
                while ((len = in.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
                System.out.println("Default config.yml copied to plugin folder.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("VulnerableCodeUsages")
    private void loadConfigYaml() {
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            System.out.println("config.yml not found on disk!");
            config = new HashMap<>();
            return;
        }
        try (InputStream in = new FileInputStream(configFile)) {
            Yaml yaml = new Yaml();
            Object loaded = yaml.load(in);
            if (loaded instanceof Map) {
                //noinspection unchecked
                config = (Map<String, Object>) loaded;
            } else {
                config = new HashMap<>();
                System.out.println("config.yml was not a valid YAML map.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            config = new HashMap<>();
        }
    }

    public Map<String, Object> getCustomConfig() {
        return config;
    }

    @SuppressWarnings("unused")
    public static AuctionPlugin getInstance() {
        return instance;
    }

    public AuctionManager getAuctionManager() {
        return auctionManager;
    }
}
