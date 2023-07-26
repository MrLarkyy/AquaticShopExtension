package xyz.larkyy.aquaticshopextension.nms_api;

import net.brcdev.shopgui.ShopGuiPlugin;
import net.brcdev.shopgui.config.Lang;
import net.brcdev.shopgui.config.Settings;
import net.brcdev.shopgui.event.ShopPostTransactionEvent;
import net.brcdev.shopgui.event.ShopPreTransactionEvent;
import net.brcdev.shopgui.gui.click.GuiClickAction;
import net.brcdev.shopgui.gui.gui.AmountSelectionBulkGui;
import net.brcdev.shopgui.gui.gui.AmountSelectionGui;
import net.brcdev.shopgui.gui.gui.OpenGui;
import net.brcdev.shopgui.permission.PermissionManager;
import net.brcdev.shopgui.player.PlayerData;
import net.brcdev.shopgui.shop.Shop;
import net.brcdev.shopgui.shop.ShopManager;
import net.brcdev.shopgui.shop.ShopTransactionResult;
import net.brcdev.shopgui.shop.item.ShopItem;
import net.brcdev.shopgui.shop.item.ShopItemPermission;
import net.brcdev.shopgui.shop.item.ShopItemType;
import net.brcdev.shopgui.sound.SoundAction;
import net.brcdev.shopgui.util.ChatUtils;
import net.brcdev.shopgui.util.InventoryUtils;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class SearchHandler {

    private final ShopExtensionPlugin plugin;
    public SearchHandler(ShopExtensionPlugin plugin) {
        this.plugin = plugin;
        new SearchTicker(plugin).runTaskTimer(plugin,20,20);
    }

    private final Map<UUID, SearchSession> searchUIs = new HashMap<>();

    public void openSearch(Player player) {


        ItemStack is = new ItemStack(Material.MAP);
        ItemMeta im = is.getItemMeta();
        im.setCustomModelData(1010);
        im.setDisplayName("§f");
        is.setItemMeta(im);

        var anvil = new AnvilGUI.Builder()
                .plugin(plugin)
                .title("Search Inventory")
                .onClick((slot,state)-> Collections.emptyList())
                .itemLeft(is)
                .open(player);

        searchUIs.put(player.getUniqueId(),new SearchSession(anvil));
    }

    public void removeSearch(UUID uuid) {
        searchUIs.remove(uuid);

        Player p = Bukkit.getPlayer(uuid);
        if (p == null) return;
    }

    public boolean isSearch(UUID uuid, Inventory inventory) {
        var anvil = searchUIs.get(uuid);
        if (anvil == null) return false;

        return (anvil.getAnvilGUI().getInventory().equals(inventory));
    }

    public SearchSession getSession(UUID uuid) {
        return searchUIs.get(uuid);
    }

    public Map<UUID, SearchSession> getSearchUIs() {
        return searchUIs;
    }

    public void updateSearch(Player player, String input) {
        var session = searchUIs.get(player.getUniqueId());
        List<ShopItem> found = new ArrayList<>();
        String[] strs = input.split(" ");
        for (Shop shop : ShopGuiPlugin.getInstance().getShopManager().shops) {
            for (ShopItem shopItem : shop.getShopItems()) {
                if (shopItem.getType()!= ShopItemType.ITEM) continue;
                var mat = shopItem.getItem().getType().toString();
                mat = mat.replace("_"," ").toLowerCase();

                boolean b = true;
                for (String str : strs) {

                    if (!mat.contains(str.toLowerCase())) {
                        b = false;
                        break;
                    }
                }
                if (b) {
                    found.add(shopItem);
                }
            }
        }
        session.setFoundItems(found);
        session.setPage(0);

        updateSearchPage(player);
    }

    public void updateSearchPage(Player player) {
        var session = searchUIs.get(player.getUniqueId());
        session.getClickActions().clear();
        List<Integer> slots = Arrays.asList(
                10,11,12,13,14,15,16,
                19,20,21,22,23,24,25,
                28,29,30,31,32,33,34
        );

        for (int i = 0; i < slots.size(); i++) {
            int itemIndex = (session.getPage()*slots.size())+i;

            ItemStack is;
            int slot = slots.get(i);
            if (itemIndex >= session.getFoundItems().size()) {
                is = new ItemStack(Material.AIR);
            } else {
                var shopItem = session.getFoundItems().get(itemIndex);
                is = shopItem.getPlaceholder().clone();
                addShopLore(is,player,shopItem.getShop(),shopItem, shopItem.getBuyPrice(player), shopItem.getSellPrice(player));
                session.getClickActions().put(slot,(e)-> {
                    removeSearch(player.getUniqueId());

                    var shop = shopItem.getShop();
                    if (!shop.hasAccessToItem(player, shopItem, true))
                        return;
                    ShopGuiPlugin.getInstance().getSoundManager().playSound(player, SoundAction.SHOP_SELECT_ITEM);
                    for (String str : shopItem.getCommandsOnClick())
                        Bukkit.dispatchCommand(player, str.replace("%PLAYER%", player.getName()));
                    for (String str : shopItem.getCommandsOnClickConsole())
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), str.replace("%PLAYER%", player.getName()));
                    if (shopItem.getType() == ShopItemType.SHOP_LINK) {
                        ShopGuiPlugin.getInstance().getShopManager().openShopMenu(player, shopItem.getShopLinkShopId(), true);
                        return;
                    }
                    if (shopItem.isCloseGuiOnClick()) {
                        ShopGuiPlugin.getInstance().getShopManager().closeGui(player);
                        return;
                    }
                    ItemStack itemStack = shopItem.getPlaceholder().clone();
                    GuiClickAction guiClickAction = null;
                    var clickType = e.getClick();
                    if (shop.getGuiClickActions().hasClickType(clickType)) {
                        guiClickAction = shop.getGuiClickActions().getGuiClickAction(clickType);
                    } else if (Settings.clickActions.hasClickType(clickType)) {
                        guiClickAction = Settings.clickActions.getGuiClickAction(clickType);
                    }
                    var playerData = ShopGuiPlugin.getInstance().getPlayerManager().getPlayerData(player);

                    if (guiClickAction != null)
                        if (guiClickAction == GuiClickAction.BUY) {
                            if (shopItem.getType() == ShopItemType.ITEM) {
                                clickBuyItem(player, playerData, shopItem, itemStack);
                            } else if (shopItem.getType() == ShopItemType.COMMAND) {
                                clickBuyCommand(player, playerData, shopItem, itemStack);
                            }
                        } else if (guiClickAction == GuiClickAction.SELL && shopItem.getType() == ShopItemType.ITEM) {
                            clickSellItem(player, playerData, shopItem, itemStack);
                        } else if (guiClickAction == GuiClickAction.SELL_ALL && shopItem.getType() == ShopItemType.ITEM) {
                            if (!Settings.enableSellAll)
                                return;
                            if (shopItem.getSellPrice(player) < 0.0D) {
                                player.sendMessage(Lang.PREFIX + Lang.MSG_ITEM_CANNOTSELL.toString());
                                return;
                            }
                            clickSellAllItem(player, shopItem);
                        }
                });
            }

            plugin.getNMSHandler().setContainerItem(player,is,slot-6);
            //player.getInventory().setItem(slot,is);
        }

        {
            ItemStack is = new ItemStack(Material.MAP);
            ItemMeta im = is.getItemMeta();
            im.setCustomModelData(1010);
            im.setDisplayName("§f");
            is.setItemMeta(im);
            plugin.getNMSHandler().setContainerItem(player,is,0);
        }

        {
            ItemStack is = new ItemStack(Material.MAP);
            ItemMeta im = is.getItemMeta();
            im.setCustomModelData(1010);
            im.setDisplayName("§bPrevious Page");
            is.setItemMeta(im);
            plugin.getNMSHandler().setContainerItem(player,is,9-6);
            session.getClickActions().put(9,(e)-> {
                Bukkit.broadcastMessage("Click prev page");
            });
            plugin.getNMSHandler().setContainerItem(player,is,18-6);
            session.getClickActions().put(18,(e)-> {
                Bukkit.broadcastMessage("Click prev page");
            });
            plugin.getNMSHandler().setContainerItem(player,is,27-6);
            session.getClickActions().put(27, (e)-> {
                Bukkit.broadcastMessage("Click prev page");
            });
        }
        {
            ItemStack is = new ItemStack(Material.MAP);
            ItemMeta im = is.getItemMeta();
            im.setCustomModelData(1010);
            im.setDisplayName("§bNext Page");
            is.setItemMeta(im);
            plugin.getNMSHandler().setContainerItem(player,is,17-6);
            session.getClickActions().put(17,(e)-> {
                Bukkit.broadcastMessage("Click next page");
            });
            plugin.getNMSHandler().setContainerItem(player,is,26-6);
            session.getClickActions().put(26,(e)-> {
                Bukkit.broadcastMessage("Click next page");
            });
            plugin.getNMSHandler().setContainerItem(player,is,35-6);
            session.getClickActions().put(35,(e)-> {
                Bukkit.broadcastMessage("Click next page");
            });
        }

    }

    private void addShopLore(ItemStack paramItemStack, Player paramPlayer, Shop paramShop, ShopItem paramShopItem, double paramDouble1, double paramDouble2) {
        ShopItemType shopItemType = paramShopItem.getType();
        ItemMeta itemMeta = paramItemStack.getItemMeta();
        ArrayList<String> arrayList;
        switch (shopItemType) {
            case PERMISSION:
                arrayList = new ArrayList(Settings.shopItemLoreFormatPermission);
                break;
            case ENCHANTMENT:
                arrayList = new ArrayList(Settings.shopItemLoreFormatEnchantment);
                break;
            default:
                arrayList= new ArrayList(Settings.shopItemLoreFormatCommand);
                break;
        }
        String str1 = (paramDouble1 < 0.0D) ? Settings.buyPriceForUnsellablePlaceholder : ChatUtils.formatCurrencyString(paramShop, paramDouble1);
        String str2 = (paramDouble2 < 0.0D) ? Settings.sellPriceForUnsellablePlaceholder : ChatUtils.formatCurrencyString(paramShop, paramDouble2);
        ListIterator<String> listIterator = arrayList.listIterator();
        while (listIterator.hasNext()) {
            String str = listIterator.next();
            if (Settings.hideBuyPriceForUnbuyable && paramDouble1 < 0.0D && str.toLowerCase().contains("%buy%"))
                listIterator.remove();
            if (Settings.hideSellPriceForUnsellable && paramDouble2 < 0.0D && (str.toLowerCase().contains("%sell%") || str.toLowerCase().contains("%sellall%") || str.toLowerCase().contains("sell-all") || str.toLowerCase().contains("sell all") || str.toLowerCase().contains("sellall")))
                listIterator.remove();
        }
        byte b;
        for (b = 0; b < arrayList.size(); b++)
            arrayList.set(b, ChatUtils.fixColors(((String)arrayList.get(b)).replace("%buy%", str1).replace("%sell%", str2)));
        if (shopItemType == ShopItemType.PERMISSION) {
            b = 1;
            for (ShopItemPermission shopItemPermission : paramShopItem.getPermissions()) {
                if (shopItemPermission.has(paramPlayer)) {
                    b = 0;
                    break;
                }
            }
            if (b != 0) {
                if (!Lang.SHOP_PERMISSION_ALREADYOWNED.toString().isEmpty())
                    for (byte b1 = 0; b1 < arrayList.size(); b1++)
                        arrayList.set(b1, ((String)arrayList.get(b1)).replace("%owned%", Lang.SHOP_PERMISSION_ALREADYOWNED.toString()));
            } else {
                listIterator = arrayList.listIterator();
                while (listIterator.hasNext()) {
                    String str = listIterator.next();
                    if (str.toLowerCase().contains("%owned%")) {
                        if (Lang.SHOP_PERMISSION_NOTOWNED.toString().isEmpty()) {
                            listIterator.remove();
                            continue;
                        }
                        listIterator.set(str.replace("%owned%", Lang.SHOP_PERMISSION_NOTOWNED.toString()));
                    }
                }
            }
        }
        if (itemMeta.hasLore()) {
            List<String> list = itemMeta.getLore();
            list.addAll(arrayList);
            itemMeta.setLore(list);
        } else {
            itemMeta.setLore(arrayList);
        }
        paramItemStack.setItemMeta(itemMeta);
    }

    private void clickBuyItem(Player paramPlayer, PlayerData paramPlayerData, ShopItem paramShopItem, ItemStack paramItemStack) {
        Shop shop = paramShopItem.getShop();
        if (paramShopItem.getBuyPrice(paramPlayer) < 0.0D) {
            paramPlayer.sendMessage(Lang.PREFIX + Lang.MSG_ITEM_CANNOTBUY.toString());
            return;
        }
        if (Settings.enableBuyGUI && shop.isEnableBuyGUI()) {
            if (Settings.openBulkGuiImmediately) {
                if (PermissionManager.hasPermission(paramPlayer, "shopguiplus.buymore")) {
                    AmountSelectionBulkGui amountSelectionBulkGui = new AmountSelectionBulkGui(paramPlayer, paramPlayerData, OpenGui.MenuType.AMOUNT_SELECTION_BULK_BUY, shop, ShopManager.ShopAction.BUY, paramShopItem, paramItemStack, Settings.amountSelectionGUISettingsBulkBuy);
                    InventoryUtils.openInventory(paramPlayer, paramPlayerData, amountSelectionBulkGui.getInventory(), (OpenGui)amountSelectionBulkGui);
                } else {
                    paramPlayer.sendMessage(Lang.PREFIX + Lang.MSG_NOACCESS.toString());
                }
            } else {
                AmountSelectionGui amountSelectionGui = new AmountSelectionGui(paramPlayer, paramPlayerData, OpenGui.MenuType.AMOUNT_SELECTION, shop, ShopManager.ShopAction.BUY, paramShopItem, paramItemStack);
                InventoryUtils.openInventory(paramPlayer, paramPlayerData, amountSelectionGui.getInventory(), (OpenGui)amountSelectionGui);
            }
            return;
        }
        ShopGuiPlugin.getInstance().getShopManager().handleItemBuy(paramPlayer, paramShopItem, paramItemStack.getAmount(), true);
    }

    private void clickSellItem(Player paramPlayer, PlayerData paramPlayerData, ShopItem paramShopItem, ItemStack paramItemStack) {
        Shop shop = paramShopItem.getShop();
        if (paramShopItem.getSellPrice(paramPlayer) < 0.0D) {
            paramPlayer.sendMessage(Lang.PREFIX + Lang.MSG_ITEM_CANNOTSELL.toString());
            return;
        }
        if (Settings.enableSellGUI && shop.isEnableSellGUI()) {
            if (Settings.openBulkGuiImmediately) {
                if (PermissionManager.hasPermission(paramPlayer, "shopguiplus.sellmore")) {
                    AmountSelectionBulkGui amountSelectionBulkGui = new AmountSelectionBulkGui(paramPlayer, paramPlayerData, OpenGui.MenuType.AMOUNT_SELECTION_BULK_SELL, shop, ShopManager.ShopAction.SELL, paramShopItem, paramItemStack, Settings.amountSelectionGUISettingsBulkSell);
                    InventoryUtils.openInventory(paramPlayer, paramPlayerData, amountSelectionBulkGui.getInventory(), (OpenGui)amountSelectionBulkGui);
                } else {
                    paramPlayer.sendMessage(Lang.PREFIX + Lang.MSG_NOACCESS.toString());
                }
            } else {
                AmountSelectionGui amountSelectionGui = new AmountSelectionGui(paramPlayer, paramPlayerData, OpenGui.MenuType.AMOUNT_SELECTION, shop, ShopManager.ShopAction.SELL, paramShopItem, paramItemStack);
                InventoryUtils.openInventory(paramPlayer, paramPlayerData, amountSelectionGui.getInventory(), (OpenGui)amountSelectionGui);
            }
            return;
        }
        ShopGuiPlugin.getInstance().getShopManager().handleItemSell(paramPlayer, paramShopItem, paramItemStack.getAmount(), true, true);
    }


    private void clickSellAllItem(Player paramPlayer, ShopItem paramShopItem) {
        ShopGuiPlugin.getInstance().getShopManager().handleItemSellAll(paramPlayer, paramShopItem, true);
    }

    private void clickBuyCommand(Player paramPlayer, PlayerData paramPlayerData, ShopItem paramShopItem, ItemStack paramItemStack) {
        Shop shop = paramShopItem.getShop();
        if (Settings.enableBuyGUI && shop.isEnableBuyGUI()) {
            if (Settings.openBulkGuiImmediately) {
                if (PermissionManager.hasPermission(paramPlayer, "shopguiplus.buymore")) {
                    AmountSelectionBulkGui amountSelectionBulkGui = new AmountSelectionBulkGui(paramPlayer, paramPlayerData, OpenGui.MenuType.AMOUNT_SELECTION_BULK_BUY, shop, ShopManager.ShopAction.BUY, paramShopItem, paramItemStack, Settings.amountSelectionGUISettingsBulkBuy);
                    InventoryUtils.openInventory(paramPlayer, paramPlayerData, amountSelectionBulkGui.getInventory(), (OpenGui)amountSelectionBulkGui);
                } else {
                    paramPlayer.sendMessage(Lang.PREFIX + Lang.MSG_NOACCESS.toString());
                }
            } else {
                AmountSelectionGui amountSelectionGui = new AmountSelectionGui(paramPlayer, paramPlayerData, OpenGui.MenuType.AMOUNT_SELECTION, shop, ShopManager.ShopAction.BUY, paramShopItem, paramItemStack);
                InventoryUtils.openInventory(paramPlayer, paramPlayerData, amountSelectionGui.getInventory(), (OpenGui)amountSelectionGui);
            }
            return;
        }
        ShopGuiPlugin.getInstance().getShopManager().handleCommandBuy(paramPlayer, paramShopItem, paramItemStack.getAmount(), true);
    }
}
