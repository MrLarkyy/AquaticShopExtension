package xyz.larkyy.aquaticshopextension.nms_api;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class ShopSession {

    private int page;
    private String selectedCategory;
    private int categoryLowbound;
    private Inventory inventory;
    private int selectedIndex = 0;

    public ShopSession(String selectedCategory, Player player) {
        this.page = 0;
        this.selectedCategory = selectedCategory;
        this.categoryLowbound = 0;
        this.inventory = player.getOpenInventory().getTopInventory();
    }

    public int getCategoryLowbound() {
        return categoryLowbound;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public int getPage() {
        return page;
    }

    public String getSelectedCategory() {
        return selectedCategory;
    }

    public void setCategoryLowbound(int categoryLowbound) {
        this.categoryLowbound = categoryLowbound;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public void setSelectedCategory(String selectedCategory) {
        this.selectedCategory = selectedCategory;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;
    }
}
