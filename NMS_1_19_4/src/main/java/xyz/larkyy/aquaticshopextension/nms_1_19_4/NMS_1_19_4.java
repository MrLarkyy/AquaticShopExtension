package xyz.larkyy.aquaticshopextension.nms_1_19_4;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import xyz.larkyy.aquaticshopextension.nms_api.NMSHandler;
import xyz.larkyy.aquaticshopextension.nms_api.ShopExtensionPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NMS_1_19_4 implements NMSHandler {

    private final ShopExtensionPlugin plugin;
    private final Map<UUID, ChannelPipeline> pipelines = new HashMap<>();

    public NMS_1_19_4(ShopExtensionPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void injectPlayer(Player player) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        ServerGamePacketListenerImpl packetListener = craftPlayer.getHandle().connection;
        Connection connection;
        try {
            var field = packetListener.getClass().getDeclaredField("h");
            field.setAccessible(true);
            connection = (Connection) field.get(packetListener);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        var pipeline = connection.channel.pipeline();
        pipelines.put(player.getUniqueId(), pipeline);

        PacketCDH cdh = new PacketCDH(plugin,player);

        for (String str : pipeline.toMap().keySet()) {
            if (pipeline.get(str) instanceof Connection) {
                pipeline.addBefore("packet_handler", "AquaticShopExtension_packet_reader", cdh);
                break;
            }
        }
    }

    @Override
    public void ejectPlayer(Player player) {
        plugin.getShopSessionHandler().removeSession(player.getUniqueId());
        CraftPlayer craftPlayer = (CraftPlayer) player;
        ServerGamePacketListenerImpl packetListener = craftPlayer.getHandle().connection;
        Connection connection;
        try {
            var field = packetListener.getClass().getDeclaredField("h");
            field.setAccessible(true);
            connection = (Connection) field.get(packetListener);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        Channel channel = connection.channel;
        if (channel != null) {
            try {
                if (channel.pipeline().names().contains("AquaticShopExtension_packet_reader")) {
                    channel.pipeline().remove("AquaticShopExtension_packet_reader");
                }
            } catch (Exception ignored) {
            }
            pipelines.remove(player.getUniqueId());
        }
    }

    @Override
    public void updateTitle(Player player, String newTitle) {
        CraftPlayer cp = (CraftPlayer)player;
        ServerPlayer sp = cp.getHandle();

        int id = sp.containerMenu.containerId;

        ClientboundOpenScreenPacket packet = new ClientboundOpenScreenPacket(id,sp.containerMenu.getType(), Component.literal(newTitle));
        sendPacket(player,packet);

    }

    public ChannelPipeline getPipeline(UUID uuid) {
        return pipelines.get(uuid);
    }

    public void sendPacket(Player player, Packet<?> packet) {
        // Gets the player's pipeline
        var pipeline = getPipeline(player.getUniqueId());
        if (pipeline == null) {
            return;
        }
        if (packet == null) {
            return;
        }
        // Compresses the packet
        var compressed = compressPacket(packet);
        pipeline.write(compressed);
        pipeline.flush();
    }

    // Compresses the packet
    private FriendlyByteBuf compressPacket(Packet<?> packet) {
        var packetId = getPacketId(packet);
        var buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeByte(packetId);
        packet.write(buf);

        return buf;
    }
    private int getPacketId(Packet<?> packet) {
        return ConnectionProtocol.PLAY.getPacketId(PacketFlow.CLIENTBOUND, packet);
    }
}
