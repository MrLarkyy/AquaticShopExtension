package xyz.larkyy.aquaticshopextension.nms_api;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShopSessionHandler {

    private final Map<UUID,ShopSession> sessions = new HashMap<>();

    public Map<UUID, ShopSession> getSessions() {
        return sessions;
    }

    public ShopSession getOrCreateSession(Player player) {
        ShopSession session = sessions.get(player.getUniqueId());
        if (session == null) {
            session = new ShopSession("Blocks",player);
            sessions.put(player.getUniqueId(),session);
        }
        return session;
    }

    public void removeSession(UUID uuid) {
        sessions.remove(uuid);
    }
}
