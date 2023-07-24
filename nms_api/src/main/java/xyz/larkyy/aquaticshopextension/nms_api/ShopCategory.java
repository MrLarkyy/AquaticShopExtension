package xyz.larkyy.aquaticshopextension.nms_api;

import org.bukkit.inventory.ItemStack;

public class ShopCategory {

    private final ItemStack itemStack;
    private final String categoryId;

    public ShopCategory(String id, ItemStack is) {
        this.categoryId = id;
        this.itemStack = is;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public String getCategoryId() {
        return categoryId;
    }
}
