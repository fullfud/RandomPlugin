package com.fullfud.randomlootchest.commands;

import com.fullfud.randomlootchest.RandomLootChest;
import com.fullfud.randomlootchest.utils.LootFiller;
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
            player.sendMessage(ChatColor.YELLOW + "Usage: /loot <create|edit|apply|list|link|unlink>");
            return true;
        }
        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "create":
                handleCreate(player, args);
                break;
            case "edit":
                handleEdit(player, args);
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
                player.sendMessage(ChatColor.YELLOW + "Usage: /loot <create|edit|apply|list|link|unlink>");
                break;
        }
        return true;
    }

    private void handleCreate(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /loot create <template name>");
            return;
        }
        String templateName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        if (plugin.getTemplateManager().getTemplate(templateName) != null) {
            player.sendMessage(ChatColor.RED + "A template with this name already exists. Use '/loot edit " + templateName + "' to change it.");
            return;
        }
        Inventory gui = Bukkit.createInventory(player, 27, "Loot Template: " + templateName);
        player.openInventory(gui);
        player.sendMessage(ChatColor.GREEN + "Place items in the chest. Close to save.");
    }

    private void handleEdit(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /loot edit <template name>");
            return;
        }
        String templateName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        Map<ItemStack, Double> existingTemplate = plugin.getTemplateManager().getTemplate(templateName);

        if (existingTemplate == null) {
            player.sendMessage(ChatColor.RED + "Template '" + templateName + "' does not exist.");
            return;
        }

        Inventory gui = Bukkit.createInventory(player, 27, "Loot Template: " + templateName);
        for (ItemStack item : existingTemplate.keySet()) {
            gui.addItem(item.clone());
        }

        player.openInventory(gui);
        player.sendMessage(ChatColor.GOLD + "Editing mode. Modify items and close to set new chances.");
    }

    private void handleApply(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /loot apply <template name>");
            return;
        }
        String templateName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
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
        LootFiller.fillChest(chest, template);
        player.getWorld().spawnParticle(org.bukkit.Particle.VILLAGER_HAPPY, targetBlock.getLocation().add(0.5, 1, 0.5), 30);
        player.sendMessage(ChatColor.GREEN + "Loot applied!");
    }

    private void handleList(Player player) {
        player.sendMessage(ChatColor.YELLOW + "Available templates:");
        for (String name : plugin.getTemplateManager().getTemplateNames()) {
            player.sendMessage(ChatColor.GREEN + "- " + name);
        }
    }

    private void handleLink(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Usage: /loot link <template name> <timeInSeconds>");
            return;
        }
        Block targetBlock = player.getTargetBlock(null, 5);
        if (targetBlock.getType() != Material.CHEST) {
            player.sendMessage(ChatColor.RED + "You must be looking at a chest.");
            return;
        }

        String timeArg = args[args.length - 1];
        long interval;
        try {
            interval = Long.parseLong(timeArg);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid time provided.");
            return;
        }

        String templateName = String.join(" ", Arrays.copyOfRange(args, 1, args.length - 1));
        if (plugin.getTemplateManager().getTemplate(templateName) == null) {
            player.sendMessage(ChatColor.RED + "Template '" + templateName + "' not found.");
            return;
        }

        if (interval < 60) {
            player.sendMessage(ChatColor.RED + "Respawn time cannot be less than 60 seconds.");
            return;
        }

        plugin.getChestManager().addChest(targetBlock.getLocation(), templateName, interval);
        player.sendMessage(ChatColor.GREEN + "Chest linked to '" + templateName + "' (every " + interval + "s).");
    }

    private void handleUnlink(Player player) {
        Block targetBlock = player.getTargetBlock(null, 5);
        if (targetBlock.getType() != Material.CHEST) {
            player.sendMessage(ChatColor.RED + "You must be looking at a chest.");
            return;
        }
        plugin.getChestManager().removeChest(targetBlock.getLocation());
        player.sendMessage(ChatColor.GREEN + "Chest unlinked.");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], Arrays.asList("create", "edit", "apply", "list", "link", "unlink"), new ArrayList<>());
        }
        if (args.length >= 2) {
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("apply") || subCommand.equals("link") || subCommand.equals("edit")) {
                return new ArrayList<>(plugin.getTemplateManager().getTemplateNames());
            }
        }
        return Collections.emptyList();
    }
}