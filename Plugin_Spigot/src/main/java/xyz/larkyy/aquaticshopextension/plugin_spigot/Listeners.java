package xyz.larkyy.aquaticshopextension.plugin_spigot;

import net.brcdev.shopgui.ShopGuiPlusApi;
import net.brcdev.shopgui.core.BInventoryHolder;
import net.brcdev.shopgui.inventory.ShopInventoryHolder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.larkyy.aquaticshopextension.nms_api.ShopCategory;
import xyz.larkyy.aquaticshopextension.nms_api.ShopExtensionPlugin;
import xyz.larkyy.aquaticshopextension.nms_api.ShopSession;

import java.util.function.Consumer;

public class Listeners implements Listener {

    private final Plugin_Spigot plugin;

    public Listeners(Plugin_Spigot plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInvClick(InventoryClickEvent e) {

        if (e.getClickedInventory() != null && e.getClickedInventory().getHolder() instanceof AnimationUI) {
            e.setCancelled(true);
        }

        if (!(e.getClick() == ClickType.LEFT || e.getClick() == ClickType.RIGHT)) {
            return;
        }
        if (!plugin.getShopSessionHandler().getSessions().containsKey(e.getWhoClicked().getUniqueId())) {
            return;
        }

        ShopSession session = plugin.getShopSessionHandler().getOrCreateSession((Player) e.getWhoClicked());
        boolean update = false;
        if (e.getRawSlot() == 0) {
            int lowBound = session.getCategoryLowbound();
            lowBound--;
            if (lowBound < 0) {
                lowBound = ShopExtensionPlugin.categories.size()-1;
            }
            session.setCategoryLowbound(lowBound);

            update = true;
        } else if (e.getRawSlot() == 8) {
            int lowBound = session.getCategoryLowbound();
            lowBound++;
            if (lowBound >= ShopExtensionPlugin.categories.size()) {
                lowBound = 0;
            }
            session.setCategoryLowbound(lowBound);

            update = true;
        } else if (e.getRawSlot() < 8 && e.getRawSlot() > 0) {
            int index = session.getCategoryLowbound()+e.getRawSlot()-1;
            if (index >= ShopExtensionPlugin.categories.size()) {
                index = index - ShopExtensionPlugin.categories.size();
            }
            ShopCategory category = ShopExtensionPlugin.categories.get(index);
            ShopGuiPlusApi.openShop((Player) e.getWhoClicked(),category.getCategoryId(),1);
            session.setSelectedCategory(category.getCategoryId());
            update = true;
        }
        if (update) {
            plugin.updateInventory((Player) e.getWhoClicked(),e.getInventory(),session);
        }
    }

    @EventHandler
    public void onInvClose(InventoryCloseEvent e) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (e.getPlayer().getOpenInventory().getTopInventory().getHolder() instanceof ShopInventoryHolder) {
                    return;
                }
                plugin.getShopSessionHandler().removeSession(e.getPlayer().getUniqueId());
            }
        }.runTaskLater(plugin,1);
    }
}
