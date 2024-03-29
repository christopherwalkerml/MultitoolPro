package me.darkolythe.multitool;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;


public class ConfigManager {

    public Multitool main;
    public ConfigManager(Multitool plugin) {
        this.main = plugin;
    }
    public Multitool plugin = Multitool.getPlugin(Multitool.class);

    private FileConfiguration playerDataConfig;
    private File playerData;

    public void setup() {
        playerData = new File(plugin.getDataFolder(), "PlayerData.yml");

        if (!playerData.exists()) {
            try {
                if (playerData.createNewFile()) {
                    main.getLogger().log(Level.INFO, (main.prefix + ChatColor.GREEN + "PlayerData.yml has been created"));
                } else {
                    main.getLogger().log(Level.INFO, (main.prefix + ChatColor.RED + "Could not create PlayerData.yml"));
                }
            } catch (IOException e) {
                main.getLogger().log(Level.INFO, (main.prefix + ChatColor.RED + "Could not create PlayerData.yml"));
            }
        }
        playerDataConfig = YamlConfiguration.loadConfiguration(playerData);
    }

    public void playerLoad(UUID uuid, String invdir) {
        if (invdir.equals("toolinv.")) {
            Inventory inv = Bukkit.getServer().createInventory(null, InventoryType.DISPENSER, main.mtoinv); //create the mv inv
            if (playerDataConfig.contains("toolinv." + uuid)) {
                int index = 0;
                for (String item : playerDataConfig.getConfigurationSection("toolinv." + uuid).getKeys(false)) { //load all the itemstacks from config.yml
                    if (playerDataConfig.getConfigurationSection("toolinv." + uuid + "." + item) != null) {
                        inv.setItem(index, loadItem(playerDataConfig.getConfigurationSection("toolinv." + uuid + "." + item)));
                    }
                    if (inv.getItem(index) == null || (index == 4 && inv.getItem(index).getType() == Material.FEATHER)) {
                        inv.setItem(index, main.placeholders.get(index));
                    }
                    index += 1;
                }
            }
            for (int index = 0; index < 9; index++) {
                if (inv.getItem(index) == null || (index == 4 && inv.getItem(index).getType() == Material.FEATHER)) {
                    inv.setItem(index, main.placeholders.get(index));
                }
            }
            main.toolinv.put(uuid, inv);
        } else {
            Inventory winv = Bukkit.getServer().createInventory(null, InventoryType.HOPPER, main.mtwinv); //create the mv inv
            if (playerDataConfig.contains("winginv." + uuid)) {
                int index = 0;
                for (String item : playerDataConfig.getConfigurationSection("winginv." + uuid).getKeys(false)) { //load all the itemstacks from config.yml
                    if (playerDataConfig.getConfigurationSection("winginv." + uuid + "." + item) != null) {
                        winv.setItem(index, loadItem(playerDataConfig.getConfigurationSection("winginv." + uuid + "." + item)));
                    }
                    if (winv.getItem(index) == null || (index == 4 && winv.getItem(index).getType() == Material.FEATHER)) {
                        winv.setItem(index, main.wingholders.get(index));
                    }
                    index += 1;
                }
            }
            for (int index = 0; index < 5; index++) {
                if (winv.getItem(index) == null || (index == 4 && winv.getItem(index).getType() == Material.FEATHER)) {
                    winv.setItem(index, main.wingholders.get(index));
                }
            }
            main.winginv.put(uuid, winv);
        }
    }
    
    public void playerSave(UUID uuid, Inventory inv, String invdir) {

        if (!playerDataConfig.contains(invdir + uuid)) {
            playerDataConfig.createSection(invdir + uuid); //if the player's mt inv doesnt exist in config.yml ,create it
        }

        playerDataConfig.set(invdir + uuid, null);

        char c = 'a';

        if (inv == null) {
            if (invdir.equals("toolinv.")) {
                inv = main.toolinv.get(uuid);
            } else {
                inv = main.winginv.get(uuid);
            }
        }

        if ((main.toolinv.containsKey(uuid) && invdir.equals("toolinv.")) || (main.winginv.containsKey(uuid) && invdir.equals("winginv."))) {
            for (ItemStack itemstack : inv) { //save the player's mt inventory
                if (itemstack != null) {
                    saveItem(playerDataConfig.createSection(invdir + uuid + "." + c++), itemstack);
                } else {
                    ItemStack airstack = new ItemStack(Material.AIR, 0);
                    saveItem(playerDataConfig.createSection(invdir + uuid + "." + c++), airstack); //if there's nothing in a slot, save it as air
                }
            }
        } else if (invdir.equals("migration")) {
            for (ItemStack itemstack : inv) { //save the player's mt inventory
                if (itemstack != null) {
                    saveItem(playerDataConfig.createSection("toolinv." + uuid + "." + c++), itemstack);
                }
            }
        }

        try {
            playerDataConfig.save(playerData);
        } catch (IOException e) {

        }
    }

    private void saveItem(ConfigurationSection section, ItemStack itemstack) {/////////////////////////////////////////////Save Load Inv
        section.set("itemstack", itemstack);
    }

    public ItemStack loadItem(ConfigurationSection section) {
        if (section.getItemStack("itemstack") == null) {
            return new ItemStack(Material.AIR);
        }
        return new ItemStack(section.getItemStack("itemstack"));
    }
}
