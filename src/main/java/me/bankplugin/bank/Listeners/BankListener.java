package me.bankplugin.bank.Listeners;

import me.bankplugin.bank.Bank;
import me.bankplugin.bank.Database.AccountsDatabase;
import me.bankplugin.bank.Database.FinesDatabase;
import me.bankplugin.bank.Menu.Menu;
import me.bankplugin.bank.Models.Fine;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;

import static java.awt.SystemColor.menu;

public class BankListener implements Listener {

    private final Menu menu;
    private final Bank bankPlugin;
    private final FinesDatabase finesDatabase;
    private final AccountsDatabase accountsDatabase;

    public BankListener(FinesDatabase finesDatabase, AccountsDatabase accountsDatabase, Bank bankPlugin, Menu menu) {
        this.finesDatabase = finesDatabase;
        this.accountsDatabase = accountsDatabase;
        this.bankPlugin = bankPlugin;
        this.menu = menu;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (!e.getView().getTitle().equalsIgnoreCase("Банк") || !e.getView().getTitle().equalsIgnoreCase("Штрафы")) return;
        Player player = (Player) e.getPlayer();
        if (player.getItemOnCursor() != null && !player.getItemOnCursor().getType().isAir()) {
            player.setItemOnCursor(null);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        if (e.getView().getTitle().equalsIgnoreCase("Банк") || e.getView().getTitle().equalsIgnoreCase("Штрафы")) {
            e.setCancelled(true);

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
                Menu menu = new Menu(finesDatabase, accountsDatabase, bankPlugin); // или ваш класс с методом getFineMenu
                Inventory fineInventory = menu.getFineMenu(player, finesDatabase, accountsDatabase);
                player.openInventory(fineInventory);
                break;
            default:
                break;
        }

    }

    @EventHandler
    public void onFineInventoryClick(InventoryClickEvent event) {

        if (!(event.getWhoClicked() instanceof Player player)) return;

        Inventory inventory = event.getClickedInventory();
        if (inventory == null || !event.getView().getTitle().equals("Штрафы")) return;

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        String displayName = clickedItem.getItemMeta().getDisplayName();

        if (displayName.equals(ChatColor.RED + "Назад")) {
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            player.closeInventory();
            return;
        }

        if (displayName.startsWith(ChatColor.GOLD + "Штраф ")) {
            String[] split = displayName.split("#");
            if (split.length < 2) {
                player.sendMessage(ChatColor.RED + "Невозможно определить ID штрафа.");
                return;
            }

            int fineId;
            try {
                fineId = Integer.parseInt(split[1]);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Неверный формат ID штрафа.");
                return;
            }

            try {
                Fine fine = finesDatabase.getFineById(fineId);
                if (fine == null) {
                    player.sendMessage(ChatColor.RED + "Штраф с ID " + fineId + " не найден.");
                    return;
                }

                int playerBalance = accountsDatabase.getPlayerBalance(player.getName());
                if (playerBalance < fine.getAmount()) {
                    player.sendMessage(ChatColor.RED + "У вас недостаточно средств для оплаты штрафа.");
                    return;
                }

                accountsDatabase.updateBalance(player.getName(), playerBalance - fine.getAmount());
                int victimBalance = accountsDatabase.getPlayerBalance(fine.getVictim());
                accountsDatabase.updateBalance(fine.getVictim(), victimBalance + fine.getAmount());

                finesDatabase.removeFine(fineId);

                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                player.sendMessage(ChatColor.GREEN + "Вы успешно оплатили штраф §8#" + fineId + " §aна сумму §6" + fine.getAmount() + " АР.");

                Player victim = Bukkit.getPlayer(fine.getVictim());
                if (victim != null && victim.isOnline()) {
                    victim.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                    victim.sendMessage("§6" + player.getName() + " §aоплатил штраф на сумму §6" + fine.getAmount() + " АР.");
                }

                Menu menu = new Menu(finesDatabase, accountsDatabase, bankPlugin);
                player.openInventory(menu.getFineMenu(player, finesDatabase, accountsDatabase));
            } catch (SQLException e) {
                e.printStackTrace();
                player.sendMessage(ChatColor.RED + "Ошибка при оплате штрафа: " + e.getMessage());
            }
        }
    }
}

