package com.tkbstudios.resourcepackselector;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ResourcePackSelector extends JavaPlugin implements Listener {
    private FileConfiguration config;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();

        // Add default resource pack if it doesn't exist
        if (!config.contains("resourcePacks.Default")) {
            config.set("resourcePacks.Default", "https://mediafilez.forgecdn.net/files/4572/162/VanillaDefault+1.20.zip");
            saveConfig();
        }

        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        saveConfig();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        if (!config.contains("players." + playerUUID)) {
            openResourcePackSelectionGUI(player);
        } else {
            String resourcePackName = config.getString("players." + playerUUID);
            sendResourcePack(player, resourcePackName);
        }
    }

    private void openResourcePackSelectionGUI(Player player) {
        List<String> resourcePacks = new ArrayList<>(config.getConfigurationSection("resourcePacks").getKeys(false));
        int inventorySize = (int) Math.ceil(resourcePacks.size() / 9.0) * 9; // Round up to the nearest multiple of 9
        Inventory inventory = Bukkit.createInventory(null, inventorySize, ChatColor.GOLD + "Resource Pack Selection");

        for (int i = 0; i < resourcePacks.size(); i++) {
            String resourcePackName = resourcePacks.get(i);

            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta itemMeta = item.getItemMeta();
            itemMeta.setDisplayName(ChatColor.GREEN + resourcePackName);
            itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS); // Set the item as unmovable
            item.setItemMeta(itemMeta);
            inventory.setItem(i, item);
        }

        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        if (event.getView().getTitle().equals(ChatColor.GOLD + "Resource Pack Selection")) {
            event.setCancelled(true);

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

            String resourcePackName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
            config.set("players." + player.getUniqueId(), resourcePackName);
            saveConfig();
            sendResourcePack(player, resourcePackName);

            player.closeInventory(); // Close the GUI

            player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_CHAIN, 1.0f, 1.0f);
        }
    }

    private void sendResourcePack(Player player, String resourcePackName) {
        String resourcePackUrl = config.getString("resourcePacks." + resourcePackName);
        if (resourcePackUrl != null) {
            player.sendMessage(ChatColor.YELLOW + "Sending " + resourcePackName + " resource pack!");
            player.setResourcePack(resourcePackUrl);
            player.sendMessage(ChatColor.GREEN + "Sent " + resourcePackName + " resource pack!");
        } else {
            player.sendMessage(ChatColor.RED + "Failed to find the resource pack URL for " + resourcePackName);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("changerep")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Only players can use this command.");
                return true;
            }
            Player player = (Player) sender;
            openResourcePackSelectionGUI(player);
            return true;
        }
        return false;
    }
}
