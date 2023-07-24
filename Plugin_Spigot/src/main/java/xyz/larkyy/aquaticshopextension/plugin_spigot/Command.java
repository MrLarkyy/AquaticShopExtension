package xyz.larkyy.aquaticshopextension.plugin_spigot;

import net.brcdev.shopgui.ShopGuiPlusApi;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.larkyy.aquaticshopextension.nms_api.ShopSession;

import java.util.function.Consumer;

public class Command implements CommandExecutor {

    private final Plugin_Spigot plugin;

    public Command(Plugin_Spigot plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, org.bukkit.command.Command command, String s, String[] strs) {

        if (commandSender instanceof Player) {
            return false;
        }

        if (strs.length < 1) return false;
        if (strs[0].equalsIgnoreCase("category")) {
            if (strs.length < 3) return false;
            String category = strs[2];
            Player target = Bukkit.getPlayer(strs[1]);
            if (target == null) return false;

            StringBuilder title = new StringBuilder();
            title.append("§f");
            title.append("\uF000".repeat(106));
            title.append("\uF042");
            title.append("\uF041");

            String baseTitle = title.toString();

            ShopSession session = plugin.getShopSessionHandler().getOrCreateSession(target);
            AnimationUI animationUI  = new AnimationUI(baseTitle,target,session,2101);

            ItemStack is = new ItemStack(Material.MAP);
            ItemMeta im = is.getItemMeta();
            im.setCustomModelData(2102);
            im.setDisplayName(" ");
            is.setItemMeta(im);

            runTaskLater(r -> {
                animationUI.getInventory().setItem(40,is);
            },2);
            runTaskLater(r -> {
                im.setCustomModelData(2103);
                is.setItemMeta(im);
                animationUI.getInventory().setItem(40,is);
            },4);
            runTaskLater(r -> {
                plugin.getShopSessionHandler().getSessions().put(target.getUniqueId(),session);
                ShopGuiPlusApi.openShop(target,category,1);
            },6);
        }
        return true;
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
