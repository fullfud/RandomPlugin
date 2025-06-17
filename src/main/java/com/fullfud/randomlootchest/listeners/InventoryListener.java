package com.fullfud.randomlootchest.listeners;

import com.fullfud.randomlootchest.RandomLootChest;
import com.fullfud.randomlootchest.model.PlayerSession;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;

public class InventoryListener implements Listener {

    private final RandomLootChest plugin;

    public InventoryListener(RandomLootChest plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        String title = event.getView().getTitle();
        if (!title.startsWith("Loot Template: ")) {
            return;
        }

        Player player = (Player) event.getPlayer();
        if (plugin.getPlayerSessions().containsKey(player.getUniqueId())) {
            return;
        }

        String templateName = title.substring("Loot Template: ".length());
        Map<ItemStack, Double> items = new LinkedHashMap<>();

        for (ItemStack item : event.getInventory().getContents()) {
            if (item != null) {
                items.put(item.clone(), 0.0);
            }
        }

        if (items.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Template creation cancelled: no items were added.");
            return;
        }

        PlayerSession session = new PlayerSession(templateName, items);
        plugin.getPlayerSessions().put(player.getUniqueId(), session);

        player.sendMessage(ChatColor.YELLOW + "Please enter the spawn chance (0-100) for each item in the chat.");
        player.sendMessage(ChatColor.GREEN + "Enter chance for: " + session.getCurrentItem().getType().name() + " (" + session.getCurrentItem().getAmount() + ")");
    }
}