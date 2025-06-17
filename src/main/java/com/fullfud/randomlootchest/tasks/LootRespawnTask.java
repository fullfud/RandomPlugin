package com.fullfud.randomlootchest.tasks;

import com.fullfud.randomlootchest.RandomLootChest;
import com.fullfud.randomlootchest.managers.ChestManager;
import com.fullfud.randomlootchest.managers.TemplateManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class LootRespawnTask implements Runnable {

    private final RandomLootChest plugin;
    private final ChestManager chestManager;
    private final TemplateManager templateManager;
    private final Random random = new Random();
    private static final int PLAYER_CHECK_RADIUS = 50;

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
                    if (!loc.getWorld().getNearbyPlayers(loc, PLAYER_CHECK_RADIUS).isEmpty()) {
                        return;
                    }
                    if (loc.getBlock().getType() != Material.CHEST) {
                        return;
                    }

                    fillChest(loc, data.getTemplateName());
                    data.setLastRespawn(System.currentTimeMillis());

                    loc.getWorld().spawnParticle(Particle.END_ROD, loc.clone().add(0.5, 1, 0.5), 50, 0.5, 0.5, 0.5, 0);
                });
            }
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, chestManager::saveChests);
    }
    
    private void fillChest(Location loc, String templateName) {
        Map<ItemStack, Double> template = templateManager.getTemplate(templateName);
        if (template == null) return;

        Chest chest = (Chest) loc.getBlock().getState();
        Inventory chestInventory = chest.getBlockInventory();
        chestInventory.clear();

        for (Map.Entry<ItemStack, Double> entry : template.entrySet()) {
            if (random.nextDouble() * 100 < entry.getValue()) {
                int slot = random.nextInt(chestInventory.getSize());
                chestInventory.setItem(slot, entry.getKey().clone());
            }
        }
    }
}