package xyz.larkyy.aquaticshopextension.nms_1_19_4;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.brcdev.shopgui.ShopGuiPlugin;
import net.brcdev.shopgui.core.BInventoryHolder;
import net.brcdev.shopgui.exception.player.PlayerDataNotLoadedException;
import net.brcdev.shopgui.inventory.ShopInventoryHolder;
import net.brcdev.shopgui.player.PlayerData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.larkyy.aquaticshopextension.nms_api.ShopExtensionPlugin;
import xyz.larkyy.aquaticshopextension.nms_api.ShopSession;

public class PacketCDH extends ChannelDuplexHandler {

    private final Player player;
    private final ShopExtensionPlugin plugin;

    public PacketCDH(ShopExtensionPlugin plugin, Player player) {
        this.player = player;
        this.plugin = plugin;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object packetObj, ChannelPromise promise) throws Exception {
        if (!(packetObj instanceof Packet<?> pkt)) {
            super.write(ctx,packetObj,promise);
            return;
        }
        if (pkt instanceof ClientboundOpenScreenPacket packet) {
            if (!packet.getTitle().getString().contains("%aquaticshopextension%")) {
                super.write(ctx,packetObj,promise);
                return;
            }

            try {
                InventoryHolder invHolder = player.getOpenInventory().getTopInventory().getHolder();

                if (invHolder instanceof BInventoryHolder holder) {

                    if (!(invHolder instanceof ShopInventoryHolder)) {
                        PacketCDH.super.write(ctx,packetObj,promise);
                        return;
                    }

                    boolean existed = plugin.getShopSessionHandler().getSessions().containsKey(player.getUniqueId());
                    ShopSession session = plugin.getShopSessionHandler().getOrCreateSession(player);
                    if (existed) {
                        session.setInventory(holder.getInventory());
                    }
                    String category;
                    String[] strs = holder.getTitle().split("%aquaticshopextension%");
                    if (strs.length < 2) {
                        category = "Blocks";
                    } else {
                        category = strs[1];
                    }
                    StringBuilder title = new StringBuilder();

                    title.append("§f");
                    title.append("\uF000".repeat(106));
                    title.append("\uF042");
                    title.append("\uF041");
                    if (ShopExtensionPlugin.subCategories.contains(category)) {
                        title.append("\uF000".repeat(214)).append("\uF053");
                        category = session.getSelectedCategory();
                    } else {
                        session.setPreviousCategory(null);
                    }

                    session.setSelectedCategory(category);

                    plugin.updateInventory(player,invHolder.getInventory(),session);
                    PacketCDH.super.write(ctx, new ClientboundOpenScreenPacket(packet.getContainerId(),packet.getType(),Component.literal(title.toString())),promise);
                }
            } catch (Exception ignored) {
                PacketCDH.super.write(ctx,packetObj,promise);
            }
        } else if(pkt instanceof ClientboundContainerSetContentPacket packet) {
            if (!plugin.getSearchHandler().isSearch(player.getUniqueId(),player.getOpenInventory().getTopInventory())) {
                super.write(ctx,packetObj,promise);
                return;
            }
            return;
        } else {
            super.write(ctx,packetObj,promise);
        }

    }
}
