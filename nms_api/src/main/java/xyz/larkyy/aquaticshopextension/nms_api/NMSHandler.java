package xyz.larkyy.aquaticshopextension.nms_api;

import org.bukkit.entity.Player;

import java.util.UUID;

public interface NMSHandler {

    void injectPlayer(Player player);
    void ejectPlayer(Player player);

    void updateTitle(Player player, String newTitle);

}
