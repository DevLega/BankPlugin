package me.bankplugin.bank.Listeners;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class BankListener implements Listener {

    @EventHandler
    public void onBankInventoryClick(InventoryClickEvent e) {

        //CHECK FOR PLAYER CLICK IN BANK INVENTORY
        if (e.getView().getTitle().equalsIgnoreCase("Банк")) {
            if (e.isRightClick()) return;

            e.setCancelled(true);

            Player player = (Player) e.getWhoClicked();

            if (e.getCurrentItem() == null || !e.getCurrentItem().hasItemMeta() || e.getCurrentItem().getItemMeta().getDisplayName() == null) {
                return;
            }

            switch (e.getCurrentItem().getItemMeta().getDisplayName()) {
                case "§dПереводы":
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                    player.sendMessage(ChatColor.GREEN + "Данная секция находиться в разработке");
                    break;
                case "§cМои штрафы":
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                    player.sendMessage(ChatColor.GREEN + "Данная секция находиться в разработке");
                    break;
                default:
                    break;
            }
        }

        //ACCOUNTS MENU
        if (e.getView().getTitle().equalsIgnoreCase("Счета")) {
            if (e.isRightClick()) return;

            e.setCancelled(true);

            Player player = (Player) e.getWhoClicked();

            if (e.getCurrentItem() == null || !e.getCurrentItem().hasItemMeta() || e.getCurrentItem().getItemMeta().getDisplayName() == null) {
                return;
            }
        }
    }

}
