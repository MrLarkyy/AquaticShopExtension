package xyz.larkyy.aquaticshopextension.plugin_spigot;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.larkyy.aquaticshopextension.nms_1_19_4.NMS_1_19_4;
import xyz.larkyy.aquaticshopextension.nms_api.*;

public final class Plugin_Spigot extends ShopExtensionPlugin {

    private NMSHandler nmsHandler;
    private ShopSessionHandler shopSessionHandler;
    private SearchHandler searchHandler;

    @Override
    public void onEnable() {
        shopSessionHandler = new ShopSessionHandler();
        nmsHandler = new NMS_1_19_4(this);
        searchHandler = new SearchHandler(this);

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            nmsHandler.injectPlayer(onlinePlayer);
        }

        getCommand("aquaticse").setExecutor(new Command(this));
        getServer().getPluginManager().registerEvents(new Listeners(this),this);

    }

    @Override
    public void onDisable() {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            nmsHandler.ejectPlayer(onlinePlayer);
        }
    }

    @Override
    public NMSHandler getNMSHandler() {
        return nmsHandler;
    }

    @Override
    public ShopSessionHandler getShopSessionHandler() {
        return shopSessionHandler;
    }

    @Override
    public SearchHandler getSearchHandler() {
        return searchHandler;
    }
}
