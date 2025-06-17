package com.fullfud.randomlootchest.listeners;

import com.fullfud.randomlootchest.RandomLootChest;
import com.fullfud.randomlootchest.model.PlayerSession;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    private final RandomLootChest plugin;

    public ChatListener(RandomLootChest plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getPlayerSessions().containsKey(player.getUniqueId())) {
            return;
        }

        event.setCancelled(true);

        PlayerSession session = plugin.getPlayerSessions().get(player.getUniqueId());
        String message = event.getMessage();

        try {
            double chance = Double.parseDouble(message);
            if (chance < 0 || chance > 100) {
                player.sendMessage(ChatColor.RED + "Please enter a number between 0 and 100.");
                return;
            }

            session.setCurrentItemChance(chance);
            player.sendMessage(ChatColor.AQUA + "Set chance for " + session.getCurrentItem().getType().name() + " to " + chance + "%");

            session.promptNextItem();

            if (session.getCurrentItem() != null) {
                player.sendMessage(ChatColor.GREEN + "Enter chance for: " + session.getCurrentItem().getType().name() + " (" + session.getCurrentItem().getAmount() + ")");
            } else {
                plugin.getTemplateManager().saveTemplate(session.getTemplateName(), session.getItemsWithChances());
                player.sendMessage(ChatColor.BOLD + "" + ChatColor.GREEN + "Template '" + session.getTemplateName() + "' has been successfully created!");
                plugin.getPlayerSessions().remove(player.getUniqueId());
            }

        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "That's not a valid number. Please try again.");
        }
    }
}