package com.fullfud.randomlootchest.commands;

import com.fullfud.randomlootchest.RandomLootChest;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Random;

public class LootCommand implements CommandExecutor {

    private final RandomLootChest plugin;
    private final Random random = new Random();

    public LootCommand(RandomLootChest plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by a player.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(ChatColor.YELLOW + "Usage: /loot <create|apply|list>");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create":
                handleCreate(player, args);
                break;
            case "apply":
                handleApply(player, args);
                break;
            case "list":
                handleList(player);
                break;
            default:
                player.sendMessage(ChatColor.YELLOW + "Usage: /loot <create|apply|list>");
                break;
        }

        return true;
    }

    private void handleCreate(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /loot create <templateName>");
            return;
        }
        String templateName = args[1];
        if (plugin.getTemplateManager().getTemplate(templateName) != null) {
            player.sendMessage(ChatColor.RED + "A template with this name already exists.");
            return;
        }

        Inventory gui = Bukkit.createInventory(player, 27, "Loot Template: " + templateName);
        player.openInventory(gui);
        player.sendMessage(ChatColor.GREEN + "Place items in the chest to create the template '" + templateName + "'. Close inventory when done.");
    }

    private void handleApply(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /loot apply <templateName>");
            return;
        }
        String templateName = args[1];
        Map<ItemStack, Double> template = plugin.getTemplateManager().getTemplate(templateName);

        if (template == null) {
            player.sendMessage(ChatColor.RED + "Template '" + templateName + "' not found.");
            return;
        }

        Block targetBlock = player.getTargetBlock(null, 5);
        if (targetBlock.getType() != Material.CHEST) {
            player.sendMessage(ChatColor.RED + "You must be looking at a chest.");
            return;
        }

        Chest chest = (Chest) targetBlock.getState();
        Inventory chestInventory = chest.getBlockInventory();
        chestInventory.clear();

        for (Map.Entry<ItemStack, Double> entry : template.entrySet()) {
            double chance = entry.getValue();
            if (random.nextDouble() * 100 < chance) {
                int slot = random.nextInt(chestInventory.getSize());
                chestInventory.setItem(slot, entry.getKey().clone());
            }
        }
        
        player.getWorld().spawnParticle(org.bukkit.Particle.VILLAGER_HAPPY, targetBlock.getLocation().add(0.5, 1, 0.5), 30);
        player.sendMessage(ChatColor.GREEN + "Loot from template '" + templateName + "' has been applied to the chest!");
    }

    private void handleList(Player player) {
        player.sendMessage(ChatColor.YELLOW + "Available templates:");
        for (String name : plugin.getTemplateManager().getTemplateNames()) {
            player.sendMessage(ChatColor.GREEN + "- " + name);
        }
    }
}