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
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import java.util.*;

public class LootCommand implements CommandExecutor, TabCompleter {

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
            player.sendMessage(ChatColor.YELLOW + "Usage: /loot <create|apply|list|link|unlink>");
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
            case "link":
                handleLink(player, args);
                break;
            case "unlink":
                handleUnlink(player);
                break;
            default:
                player.sendMessage(ChatColor.YELLOW + "Usage: /loot <create|apply|list|link|unlink>");
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
            if (random.nextDouble() * 100 < entry.getValue()) {
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
    
    private void handleLink(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Usage: /loot link <templateName> <timeInSeconds>");
            return;
        }
        Block targetBlock = player.getTargetBlock(null, 5);
        if (targetBlock.getType() != Material.CHEST) {
            player.sendMessage(ChatColor.RED + "You must be looking at a chest.");
            return;
        }
        String templateName = args[1];
        if (plugin.getTemplateManager().getTemplate(templateName) == null) {
            player.sendMessage(ChatColor.RED + "Template '" + templateName + "' not found.");
            return;
        }
        try {
            long interval = Long.parseLong(args[2]);
            plugin.getChestManager().addChest(targetBlock.getLocation(), templateName, interval);
            player.sendMessage(ChatColor.GREEN + "Chest linked to template '" + templateName + "' with a respawn time of " + interval + " seconds.");
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid time. Please enter a number in seconds.");
        }
    }

    private void handleUnlink(Player player) {
        Block targetBlock = player.getTargetBlock(null, 5);
        if (targetBlock.getType() != Material.CHEST) {
            player.sendMessage(ChatColor.RED + "You must be looking at a chest.");
            return;
        }
        plugin.getChestManager().removeChest(targetBlock.getLocation());
        player.sendMessage(ChatColor.GREEN + "Chest unlinked from the auto-respawn system.");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], Arrays.asList("create", "apply", "list", "link", "unlink"), new ArrayList<>());
        }
        if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("apply") || subCommand.equals("link")) {
                return StringUtil.copyPartialMatches(args[1], plugin.getTemplateManager().getTemplateNames(), new ArrayList<>());
            }
        }
        return Collections.emptyList();
    }
}