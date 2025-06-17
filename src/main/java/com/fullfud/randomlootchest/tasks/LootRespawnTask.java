package com.fullfud.randomlootchest.tasks;

import com.fullfud.randomlootchest.RandomLootChest;
import com.fullfud.randomlootchest.managers.ChestManager;
import com.fullfud.randomlootchest.managers.TemplateManager;
import com.fullfud.randomlootchest.utils.LootFiller;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class LootRespawnTask implements Runnable {

    private final RandomLootChest plugin;
    private final ChestManager chestManager;
    private final TemplateManager templateManager;
    private static final double PLAYER_CHECK_RADIUS = 50.0;

    public LootRespawnTask(RandomLootChest plugin) {
        this.plugin = plugin;
        this.chestManager = plugin.getChestManager();
        this.templateManager = plugin.getTemplateManager();
    }

    @Override
    public void run() {
        long currentTime = System.currentTimeMillis();
        for (Map.Entry<Location, ChestManager.ChestData> entry : chestManager.getChestDataMap().entrySet()) {
            Location loc = entry.getKey();
            ChestManager.ChestData data = entry.getValue();

            long timeSinceLastRespawn = currentTime - data.getLastRespawn();
            long intervalMillis = TimeUnit.SECONDS.toMillis(data.getInterval());

            if (timeSinceLastRespawn >= intervalMillis) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (loc.getWorld() == null || !loc.isWorldLoaded()) return;

                    boolean playersNearby = loc.getWorld()
                            .getNearbyEntities(loc, PLAYER_CHECK_RADIUS, PLAYER_CHECK_RADIUS, PLAYER_CHECK_RADIUS)
                            .stream()
                            .anyMatch(entity -> entity instanceof Player);

                    if (playersNearby) {
                        return;
                    }

                    if (loc.getBlock().getType() != Material.CHEST) {
                        return;
                    }
                    
                    Chest chest = (Chest) loc.getBlock().getState();
                    LootFiller.fillChest(chest, templateManager.getTemplate(data.getTemplateName()));
                    data.setLastRespawn(System.currentTimeMillis());

                    loc.getWorld().spawnParticle(Particle.END_ROD, loc.clone().add(0.5, 1, 0.5), 50, 0.5, 0.5, 0.5, 0);
                });
            }
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, chestManager::saveChests);
    }
}