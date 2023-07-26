package xyz.larkyy.aquaticshopextension.nms_api;

import net.brcdev.shopgui.shop.item.ShopItem;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class SearchSession {

    private final AnvilGUI anvilGUI;

    private List<ShopItem> foundItems;
    private Map<Integer, Consumer<InventoryClickEvent>> clickActions = new HashMap<>();
    private int page = 0;

    private String input = null;

    public SearchSession(AnvilGUI anvilGUI) {
        this.anvilGUI = anvilGUI;
    }

    public String getInput() {
        return input;
    }

    public AnvilGUI getAnvilGUI() {
        return anvilGUI;
    }

    public int getPage() {
        return page;
    }

    public List<ShopItem> getFoundItems() {
        return foundItems;
    }

    public void setFoundItems(List<ShopItem> foundItems) {
        this.foundItems = foundItems;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public Map<Integer, Consumer<InventoryClickEvent>> getClickActions() {
        return clickActions;
    }

    public boolean hasNextPage() {
        return (21*(page+1) < foundItems.size());
    }
}
