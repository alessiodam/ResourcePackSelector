package com.tkbstudios.resourcepackselector;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
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
import org.bukkit.plugin.PluginLogger;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ResourcePackSelector extends JavaPlugin implements Listener {
    private Logger pluginLogger = PluginLogger.getLogger("ResourcePackSelector");
    private FileConfiguration config;
    private Map<UUID, Long> cooldowns = new HashMap<>();

    @Override
    public void onEnable() {
        // Check if the configuration file exists
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            // If it doesn't exist, save the default configuration
            saveDefaultConfig();
            pluginLogger.log(Level.WARNING, "Config.yml not found. Creating default configuration file.");
        }

        // Load the configuration
        config = getConfig();

        getServer().getPluginManager().registerEvents(this, this);
        pluginLogger.log(Level.INFO, ChatColor.GREEN + "ResourcePackSelector successfully loaded!");
    }

    @Override
    public void onDisable() {
        System.out.println("Disabled ResourcePackSelector!");
    }

    private void reloadConfigFile() {
        pluginLogger.log(Level.WARNING, "Reloading config!");
        reloadConfig();
        config = getConfig();
        pluginLogger.log(Level.INFO, "Reloaded config!");
    }

    private boolean isCooldownEnabled() {
        return config.getBoolean("cooldown.enabled", true);
    }

    private int getCooldownDuration() {
        return config.getInt("cooldown.duration", 300);
    }

    private boolean hasCooldownExpired(UUID playerId) {
        if (!cooldowns.containsKey(playerId)) {
            return true;
        }
        long lastChangeTimestamp = cooldowns.get(playerId);
        int cooldownDuration = getCooldownDuration();
        long currentTime = System.currentTimeMillis() / 1000L;
        return (currentTime - lastChangeTimestamp) >= cooldownDuration;
    }

    private void setCooldown(UUID playerId) {
        cooldowns.put(playerId, System.currentTimeMillis() / 1000L);
    }

    private void clearCooldown(UUID playerId) {
        cooldowns.remove(playerId);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        if (config.getBoolean("Settings.promptAtJoin", true) && !config.contains("players." + playerUUID)) {
            openResourcePackSelectionGUI(player);
        } else {
            String resourcePackName = config.getString("players." + playerUUID);
            if (resourcePackName != null) {
                sendResourcePack(player, resourcePackName);
            }
        }
    }

    private void openResourcePackSelectionGUI(Player player) {
        List<String> resourcePacks = new ArrayList<>();
        ConfigurationSection packsSection = config.getConfigurationSection("resourcePacks");
        assert packsSection != null;
        for (String pack : packsSection.getKeys(false)) {
            String permission = packsSection.getString(pack + ".permission");
            if (permission != null && player.hasPermission(permission)) {
                resourcePacks.add(pack);
            }
        }

        if (resourcePacks.isEmpty()) {
            player.sendMessage(ChatColor.RED + "No resource packs available for you.");
            return;
        }

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

        if (!event.getView().getOriginalTitle().equals(ChatColor.GOLD + "Resource Pack Selection")) return;
        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        if (isCooldownEnabled() && !hasCooldownExpired(player.getUniqueId())) {
            long cooldownDuration = getCooldownDuration();
            long remainingCooldown = (cooldowns.get(player.getUniqueId()) + cooldownDuration) - (System.currentTimeMillis() / 1000L);
            player.sendMessage(ChatColor.RED + "You are still on cooldown. Please wait " + remainingCooldown + " seconds.");
            return;
        }

        String resourcePackName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
        config.set("players." + player.getUniqueId(), resourcePackName);
        saveConfig();
        sendResourcePack(player, resourcePackName);

        player.closeInventory(); // Close the GUI

        player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_CHAIN, 1.0f, 1.0f);

        if (isCooldownEnabled()) {
            setCooldown(player.getUniqueId());
            Bukkit.getScheduler().runTaskLater(this, () -> clearCooldown(player.getUniqueId()), getCooldownDuration() * 20L);
        }
    }

    private void sendResourcePack(Player player, String resourcePackName) {
        ConfigurationSection packsSection = config.getConfigurationSection("resourcePacks");
        assert packsSection != null;
        if (!packsSection.contains(resourcePackName)) {
            player.sendMessage(ChatColor.RED + "Failed to find the resource pack configuration for " + resourcePackName);
            return;
        }

        String resourcePackUrl = packsSection.getString(resourcePackName + ".url");
        if (resourcePackUrl != null) {
            player.sendMessage(ChatColor.GOLD + "Sending " + resourcePackName + " resource pack!");
            player.setResourcePack(resourcePackUrl);
            player.sendMessage(ChatColor.GREEN + "Sent " + resourcePackName + " resource pack!");
        } else {
            player.sendMessage(ChatColor.RED + "Failed to find the resource pack URL for " + resourcePackName);
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String label, String[] args) {
        if (command.getName().equalsIgnoreCase("changerep")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Only players can use this command.");
                return true;
            }
            Player player = (Player) sender;
            openResourcePackSelectionGUI(player);
            return true;
        } else if (command.getName().equalsIgnoreCase("randomrep")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Only players can use this command.");
                return true;
            }
            Player player = (Player) sender;
            selectRandomResourcePack(player);
            return true;
        } else if (command.getName().equalsIgnoreCase("reloadrpsconf")) {
            if (!sender.hasPermission("resourcepackselector.reloadconf")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }
            reloadConfigFile();
            sender.sendMessage(ChatColor.GOLD + "ResourcePackSelector config reloaded!");
            return true;
        }

        return false;
    }

    private void selectRandomResourcePack(Player player) {
        List<String> resourcePacks = new ArrayList<>();
        ConfigurationSection packsSection = config.getConfigurationSection("resourcePacks");
        assert packsSection != null;
        for (String pack : packsSection.getKeys(false)) {
            if (player.hasPermission(Objects.requireNonNull(packsSection.getString(pack + ".permission")))) {
                resourcePacks.add(pack);
            }
        }

        if (resourcePacks.isEmpty()) {
            player.sendMessage(ChatColor.RED + "No resource packs available for you.");
            return;
        }

        String randomPack = resourcePacks.get(new Random().nextInt(resourcePacks.size()));
        config.set("players." + player.getUniqueId(), randomPack);
        saveConfig();

        sendResourcePack(player, randomPack);
    }
}
