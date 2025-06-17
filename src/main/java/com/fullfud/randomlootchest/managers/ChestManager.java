package com.fullfud.randomlootchest.managers;

import com.fullfud.randomlootchest.RandomLootChest;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ChestManager {

    private final RandomLootChest plugin;
    private File configFile;
    private FileConfiguration config;
    private final Map<Location, ChestData> chestDataMap = new HashMap<>();

    public ChestManager(RandomLootChest plugin) {
        this.plugin = plugin;
        setupConfig();
    }

    private void setupConfig() {
        configFile = new File(plugin.getDataFolder(), "chests.yml");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create chests.yml!");
            }
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        loadChests();
    }

    public void loadChests() {
        chestDataMap.clear();
        if (!config.isConfigurationSection("chests")) {
            return;
        }
        ConfigurationSection chestsSection = config.getConfigurationSection("chests");
        for (String key : chestsSection.getKeys(false)) {
            Location loc = chestsSection.getSerializable(key, Location.class);
            if (loc == null) continue;
            String templateName = chestsSection.getString(key + ".template");
            long interval = chestsSection.getLong(key + ".interval");
            long lastRespawn = chestsSection.getLong(key + ".last-respawn", 0);
            chestDataMap.put(loc, new ChestData(templateName, interval, lastRespawn));
        }
        plugin.getLogger().info("Loaded " + chestDataMap.size() + " auto-respawning chests.");
    }

    public void saveChests() {
        config.set("chests", null);
        int index = 0;
        for (Map.Entry<Location, ChestData> entry : chestDataMap.entrySet()) {
            String path = "chests." + index;
            config.set(path, entry.getKey());
            config.set(path + ".template", entry.getValue().getTemplateName());
            config.set(path + ".interval", entry.getValue().getInterval());
            config.set(path + ".last-respawn", entry.getValue().getLastRespawn());
            index++;
        }
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save chests.yml!");
        }
    }

    public void addChest(Location loc, String templateName, long interval) {
        chestDataMap.put(loc, new ChestData(templateName, interval, System.currentTimeMillis()));
        saveChests();
    }

    public void removeChest(Location loc) {
        chestDataMap.remove(loc);
        saveChests();
    }

    public Map<Location, ChestData> getChestDataMap() {
        return chestDataMap;
    }

    public static class ChestData {
        private final String templateName;
        private final long interval;
        private long lastRespawn;

        public ChestData(String templateName, long interval, long lastRespawn) {
            this.templateName = templateName;
            this.interval = interval;
            this.lastRespawn = lastRespawn;
        }

        public String getTemplateName() { return templateName; }
        public long getInterval() { return interval; }
        public long getLastRespawn() { return lastRespawn; }
        public void setLastRespawn(long lastRespawn) { this.lastRespawn = lastRespawn; }
    }
}