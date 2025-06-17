package com.fullfud.randomlootchest.utils;

import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Random;

public class LootFiller {

    private static final Random random = new Random();

    public static void fillChest(Chest chest, Map<ItemStack, Double> template) {
        if (chest == null || template == null) {
            return;
        }

        Inventory chestInventory = chest.getBlockInventory();
        chestInventory.clear();

        for (Map.Entry<ItemStack, Double> entry : template.entrySet()) {
            double chance = entry.getValue();
            ItemStack item = entry.getKey();

            if (random.nextDouble() * 100 < chance) {
                int slot = random.nextInt(chestInventory.getSize());
                chestInventory.setItem(slot, item.clone());
            }
        }
    }
}