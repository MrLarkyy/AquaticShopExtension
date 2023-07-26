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
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
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
            return;
        }

        if (!(e.getClick() == ClickType.LEFT || e.getClick() == ClickType.RIGHT)) {
            return;
        }

        if (plugin.getSearchHandler().isSearch(e.getWhoClicked().getUniqueId(),e.getWhoClicked().getOpenInventory().getTopInventory())) {
            Bukkit.broadcastMessage("Is search!");
            e.setCancelled(true);
            if (!(e.getClick() == ClickType.LEFT || e.getClick() == ClickType.RIGHT)) {
                return;
            }

            var session = plugin.getSearchHandler().getSession(e.getWhoClicked().getUniqueId());

            Consumer<InventoryClickEvent> clickConsumer = session.getClickActions().get(e.getSlot());
            if(clickConsumer != null) {
                clickConsumer.accept(e);
            }
            return;
        }

        if (!plugin.getShopSessionHandler().getSessions().containsKey(e.getWhoClicked().getUniqueId())) {
            return;
        }

        ShopSession session = plugin.getShopSessionHandler().getOrCreateSession((Player) e.getWhoClicked());

        if (session.getPreviousCategory() != null) {
            if (e.getRawSlot() > 45 && e.getRawSlot() < 53) {

                StringBuilder title = new StringBuilder();
                title.append("§f");
                title.append("\uF000".repeat(106));
                title.append("\uF042");
                title.append("\uF041");

                String baseTitle = title.toString();

                AnimationUI animationUI  = new AnimationUI(baseTitle, (Player) e.getWhoClicked(),session,2103);

                ItemStack is = new ItemStack(Material.MAP);
                ItemMeta im = is.getItemMeta();
                im.setCustomModelData(2102);
                im.setDisplayName(" ");
                is.setItemMeta(im);

                runTaskLater(r -> {
                    animationUI.getInventory().setItem(40,is);
                },2);
                runTaskLater(r -> {
                    im.setCustomModelData(2101);
                    is.setItemMeta(im);
                    animationUI.getInventory().setItem(40,is);
                },4);
                runTaskLater(r -> {
                    plugin.getShopSessionHandler().getSessions().put(e.getWhoClicked().getUniqueId(),session);
                    ShopGuiPlusApi.openShop((Player) e.getWhoClicked(),session.getPreviousCategory(),session.getPreviousPage());
                    session.setPreviousCategory(null);
                },6);

                return;
            }
        }

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

        if (plugin.getSearchHandler().isSearch(e.getPlayer().getUniqueId(),e.getInventory())) {
            plugin.getSearchHandler().removeSearch(e.getPlayer().getUniqueId());
        }

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

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        plugin.getNMSHandler().injectPlayer(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        plugin.getNMSHandler().ejectPlayer(e.getPlayer());
    }

    private void runTaskLater(Consumer<BukkitRunnable> consumer, int delay) {
        new BukkitRunnable() {
            @Override
            public void run() {
                consumer.accept(this);
            }
        }.runTaskLater(plugin,delay);
    }
}
