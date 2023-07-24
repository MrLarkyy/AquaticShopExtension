package xyz.larkyy.aquaticshopextension.plugin_spigot;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.larkyy.aquaticshopextension.nms_api.ShopCategory;
import xyz.larkyy.aquaticshopextension.nms_api.ShopExtensionPlugin;
import xyz.larkyy.aquaticshopextension.nms_api.ShopSession;

public class AnimationUI implements InventoryHolder {

    private final Inventory inventory;

    public AnimationUI(String title, Player player, ShopSession session, int modelData) {
        this.inventory = Bukkit.createInventory(this,54,title);

        int lowBound = session.getCategoryLowbound();
        {
            ItemStack is = new ItemStack(Material.MAP);
            ItemMeta im = is.getItemMeta();
            im.setCustomModelData(modelData);
            im.setDisplayName(" ");
            is.setItemMeta(im);
            inventory.setItem(40,is);
        }
        int selectedIndex = -1;

        var categories = ShopExtensionPlugin.categories;
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


        int md = 2002+selectedIndex;
        im.setCustomModelData(md);
        im.setDisplayName(" ");
        is.setItemMeta(im);

        if (selectedIndex < 4) {
            inventory.setItem(9,is);
            inventory.setItem(17,new ItemStack(Material.AIR));
        } else {
            inventory.setItem(17,is);
            inventory.setItem(9,new ItemStack(Material.AIR));
        }

        player.openInventory(inventory);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
