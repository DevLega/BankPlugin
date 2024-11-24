package me.bankplugin.bank.Listeners;

import me.bankplugin.bank.Bank;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

public class BankListener implements Listener {

    private final Bank bankPlugin;

    public BankListener(Bank bankPlugin) {
        this.bankPlugin = bankPlugin;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (!e.getView().getTitle().equalsIgnoreCase("Банк") || !e.getView().getTitle().equalsIgnoreCase("Штрафы")) return;
        Player player = (Player) e.getPlayer();
        if (player.getItemOnCursor() != null && !player.getItemOnCursor().getType().isAir()) {
            player.setItemOnCursor(null); // Убираем предмет с курсора при закрытии
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        if (e.getView().getTitle().equalsIgnoreCase("Банк") || e.getView().getTitle().equalsIgnoreCase("Штрафы")) {
            e.setCancelled(true); // Блокируем любое перетаскивание только в банке

            // Убираем предмет с курсора
            Player player = (Player) e.getWhoClicked();
            if (player.getItemOnCursor() != null && !player.getItemOnCursor().getType().isAir()) {
                player.setItemOnCursor(null);
            }
        }
    }

    @EventHandler
    public void onBankInventoryClick(InventoryClickEvent e) {

        if (!e.getView().getTitle().equalsIgnoreCase("Банк")) return;

        e.setCancelled(true);

        Player player = (Player) e.getWhoClicked();

        if (player.getItemOnCursor() != null && !player.getItemOnCursor().getType().isAir()) {
            player.setItemOnCursor(null);
        }

        // Проверяем клики на конкретные элементы
        if (e.getCurrentItem() == null || !e.getCurrentItem().hasItemMeta() || e.getCurrentItem().getItemMeta().getDisplayName() == null) {
            return;
        }

        switch (e.getCurrentItem().getItemMeta().getDisplayName()) {
            case "§dПереводы":
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                bankPlugin.getTransferHandler().startTransfer(player);
                break;
            case "§cМои штрафы":
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                break;
            default:
                break;
        }

    }

    @EventHandler
    public void onFineInventoryClick(InventoryClickEvent e) {

        if (!e.getView().getTitle().equalsIgnoreCase("Штрафы")) return;

        e.setCancelled(true);

        Player player = (Player) e.getWhoClicked();

        if (player.getItemOnCursor() != null && !player.getItemOnCursor().getType().isAir()) {
            player.setItemOnCursor(null);
        }

        // Проверяем клики на конкретные элементы
        if (e.getCurrentItem() == null || !e.getCurrentItem().hasItemMeta() || e.getCurrentItem().getItemMeta().getDisplayName() == null) {
            return;
        }



    }
}

