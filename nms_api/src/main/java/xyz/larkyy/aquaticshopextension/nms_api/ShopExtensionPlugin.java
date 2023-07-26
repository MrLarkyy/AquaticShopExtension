package xyz.larkyy.aquaticshopextension.nms_api;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class ShopExtensionPlugin extends JavaPlugin {

    public static final List<ShopCategory> categories = Arrays.asList(
            new ShopCategory("Blocks", new ItemStack(Material.GRASS_BLOCK)),
            new ShopCategory("Ores", new ItemStack(Material.IRON_ORE)),
            new ShopCategory("Farming",new ItemStack(Material.IRON_HOE)),
            new ShopCategory("Food",new ItemStack(Material.COOKED_BEEF)),
            new ShopCategory("Redstone",new ItemStack(Material.PISTON)),
            new ShopCategory("Decorations",new ItemStack(Material.POPPY)),
            new ShopCategory("Drops", new ItemStack(Material.BONE)),
            new ShopCategory("Miscellaneous", new ItemStack(Material.MUSIC_DISC_11))
    );

    public static final List<String> subCategories = Arrays.asList("logs");

    public abstract NMSHandler getNMSHandler();

    public abstract ShopSessionHandler getShopSessionHandler();

    public abstract SearchHandler getSearchHandler();

    public void updateInventory(Player player, Inventory inventory, ShopSession session) {
        int lowBound = session.getCategoryLowbound();

        int selectedIndex = -1;

        for (int i = 0; i < 7; i++) {
            int index = i + lowBound;
            if (index >= categories.size()) {
                index = index -  ShopExtensionPlugin.categories.size();
            }
            ShopCategory category = categories.get(index);
            ItemStack is = category.getItemStack().clone();

            ItemMeta im = is.getItemMeta();
            if (session.getSelectedCategory().equalsIgnoreCase(category.getCategoryId())) {
                selectedIndex = i;
                im.addEnchant(Enchantment.DURABILITY,1,true);
                im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            is.setItemMeta(im);
            inventory.setItem(i+1,is);
        }

        if (selectedIndex < 0) {
            inventory.setItem(9,new ItemStack(Material.AIR));
            inventory.setItem(17,new ItemStack(Material.AIR));
            return;
        };
        session.setSelectedIndex(selectedIndex);
        ItemStack is = new ItemStack(Material.MAP);
        ItemMeta im = is.getItemMeta();

        int modelData = 2002+selectedIndex;
        im.setCustomModelData(modelData);
        im.setDisplayName(" ");
        is.setItemMeta(im);

        if (selectedIndex < 4) {
            inventory.setItem(9,is);
            inventory.setItem(17,new ItemStack(Material.AIR));
        } else {
            inventory.setItem(17,is);
            inventory.setItem(9,new ItemStack(Material.AIR));
        }

    }
}
