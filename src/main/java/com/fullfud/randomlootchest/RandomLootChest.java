package com.fullfud.randomlootchest;

import com.fullfud.randomlootchest.commands.LootCommand;
import com.fullfud.randomlootchest.listeners.ChatListener;
import com.fullfud.randomlootchest.listeners.InventoryListener;
import com.fullfud.randomlootchest.managers.ChestManager;
import com.fullfud.randomlootchest.managers.TemplateManager;
import com.fullfud.randomlootchest.model.PlayerSession;
import com.fullfud.randomlootchest.tasks.LootRespawnTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class RandomLootChest extends JavaPlugin {

    private TemplateManager templateManager;
    private ChestManager chestManager;
    private final Map<UUID, PlayerSession> playerSessions = new HashMap<>();
    private BukkitTask respawnTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.templateManager = new TemplateManager(this);
        this.chestManager = new ChestManager(this);

        LootCommand lootCommand = new LootCommand(this);
        getCommand("loot").setExecutor(lootCommand);
        getCommand("loot").setTabCompleter(lootCommand);

        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);

        long intervalTicks = 20L * 60;
        this.respawnTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this, new LootRespawnTask(this), intervalTicks, intervalTicks);

        getLogger().info("RandomLootChest has been enabled!");
    }

    @Override
    public void onDisable() {
        if (this.respawnTask != null && !this.respawnTask.isCancelled()) {
            this.respawnTask.cancel();
        }
        if (this.chestManager != null) {
            this.chestManager.saveChests();
        }
        getLogger().info("RandomLootChest has been disabled.");
    }

    public TemplateManager getTemplateManager() {
        return templateManager;
    }

    public ChestManager getChestManager() {
        return chestManager;
    }

    public Map<UUID, PlayerSession> getPlayerSessions() {
        return playerSessions;
    }
}