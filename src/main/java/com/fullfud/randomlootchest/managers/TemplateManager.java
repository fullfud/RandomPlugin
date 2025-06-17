package com.fullfud.randomlootchest.managers;

import com.fullfud.randomlootchest.RandomLootChest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class TemplateManager {

    private final RandomLootChest plugin;
    private File configFile;
    private FileConfiguration config;
    private final Map<String, Map<ItemStack, Double>> templates = new HashMap<>();

    public TemplateManager(RandomLootChest plugin) {
        this.plugin = plugin;
        setupConfig();
    }

    private void setupConfig() {
        configFile = new File(plugin.getDataFolder(), "templates.yml");
        if (!configFile.exists()) {
            plugin.saveResource("templates.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        loadTemplates();
    }

    public void saveTemplate(String name, Map<ItemStack, Double> items) {
        String path = "templates." + name.toLowerCase();
        config.set(path, null);

        List<Map<String, Object>> itemList = new ArrayList<>();
        for (Map.Entry<ItemStack, Double> entry : items.entrySet()) {
            Map<String, Object> itemData = new LinkedHashMap<>();
            itemData.put("item", entry.getKey());
            itemData.put("chance", entry.getValue());
            itemList.add(itemData);
        }
        config.set(path + ".items", itemList);

        try {
            config.save(configFile);
            templates.put(name.toLowerCase(), items);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save template to file: " + e.getMessage());
        }
    }

    public void loadTemplates() {
        templates.clear();
        if (!config.isConfigurationSection("templates")) {
            return;
        }
        ConfigurationSection templatesSection = config.getConfigurationSection("templates");
        for (String name : templatesSection.getKeys(false)) {
            List<Map<?, ?>> itemList = templatesSection.getMapList(name + ".items");
            Map<ItemStack, Double> itemsWithChances = new LinkedHashMap<>();
            for (Map<?, ?> itemData : itemList) {
                ItemStack item = (ItemStack) itemData.get("item");
                Double chance = (Double) itemData.get("chance");
                if (item != null && chance != null) {
                    itemsWithChances.put(item, chance);
                }
            }
            templates.put(name.toLowerCase(), itemsWithChances);
        }
        plugin.getLogger().info("Loaded " + templates.size() + " loot templates.");
    }

    public Map<ItemStack, Double> getTemplate(String name) {
        return templates.get(name.toLowerCase());
    }

    public Set<String> getTemplateNames() {
        return templates.keySet();
    }
}