package xyz.larkyy.aquaticshopextension.nms_api;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public interface NMSHandler {

    void injectPlayer(Player player);
    void ejectPlayer(Player player);

    void updateTitle(Player player, String newTitle);
    void setContainerItem(Player player, ItemStack is, int slot);
}
