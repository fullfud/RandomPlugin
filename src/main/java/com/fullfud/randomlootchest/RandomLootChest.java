package com.fullfud.randomlootchest;

import com.fullfud.randomlootchest.commands.LootCommand;
import com.fullfud.randomlootchest.listeners.ChatListener;
import com.fullfud.randomlootchest.listeners.InventoryListener;
import com.fullfud.randomlootchest.managers.TemplateManager;
import com.fullfud.randomlootchest.model.PlayerSession;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class RandomLootChest extends JavaPlugin {

    private TemplateManager templateManager;
    private final Map<UUID, PlayerSession> playerSessions = new HashMap<>();

    @Override
    public void onEnable() {
        this.templateManager = new TemplateManager(this);
        templateManager.loadTemplates();

        getCommand("loot").setExecutor(new LootCommand(this));

        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);

        getLogger().info("RandomLootChest has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("RandomLootChest has been disabled.");
    }

    public TemplateManager getTemplateManager() {
        return templateManager;
    }

    public Map<UUID, PlayerSession> getPlayerSessions() {
        return playerSessions;
    }
}