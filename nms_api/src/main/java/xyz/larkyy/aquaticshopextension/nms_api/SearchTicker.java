package xyz.larkyy.aquaticshopextension.nms_api;

import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class SearchTicker extends BukkitRunnable {

    private final ShopExtensionPlugin plugin;

    public SearchTicker(ShopExtensionPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Map.Entry<UUID, SearchSession> entry : plugin.getSearchHandler().getSearchUIs().entrySet()) {
            UUID uuid = entry.getKey();
            SearchSession session = entry.getValue();
            AnvilGUI anvilGUI = session.getAnvilGUI();
            Player p = Bukkit.getPlayer(uuid);
            if (p == null) continue;
            var is = anvilGUI.getInventory().getItem(2);
            if (is == null) {
                plugin.getSearchHandler().updateSearch(p,"");
                continue;
            };
            ItemMeta im = is.getItemMeta();
            if (im == null) {
                plugin.getSearchHandler().updateSearch(p,"");
                continue;
            }
            if (im.hasDisplayName()) {
                String text = im.getDisplayName();
                if (!text.equals(session.getInput())) {
                    session.setInput(text);
                    plugin.getSearchHandler().updateSearch(p,text);
                }
            }
        }
    }
}
