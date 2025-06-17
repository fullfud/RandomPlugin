package com.fullfud.randomlootchest.model;

import org.bukkit.inventory.ItemStack;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class PlayerSession {

    private final String templateName;
    private final Map<ItemStack, Double> itemsWithChances = new LinkedHashMap<>();
    private final Iterator<ItemStack> itemIterator;
    private ItemStack currentItem;

    public PlayerSession(String templateName, Map<ItemStack, Double> items) {
        this.templateName = templateName;
        this.itemsWithChances.putAll(items);
        this.itemIterator = items.keySet().iterator();
        promptNextItem();
    }

    public String getTemplateName() {
        return templateName;
    }

    public ItemStack getCurrentItem() {
        return currentItem;
    }

    public boolean hasNextItem() {
        return itemIterator.hasNext();
    }

    public void promptNextItem() {
        if (itemIterator.hasNext()) {
            this.currentItem = itemIterator.next();
        } else {
            this.currentItem = null;
        }
    }

    public void setCurrentItemChance(double chance) {
        if (currentItem != null) {
            itemsWithChances.put(currentItem, chance);
        }
    }

    public Map<ItemStack, Double> getItemsWithChances() {
        return itemsWithChances;
    }
}