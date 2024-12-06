package me.bankplugin.bank.Handlers;

import me.bankplugin.bank.Bank;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

public class TransferHandler {

    private final Bank bankPlugin;
    private final HashMap<UUID, TransferState> activeTransfers = new HashMap<>();

    public TransferHandler(Bank bankPlugin) {
        this.bankPlugin = bankPlugin;
    }

    public void startTransfer(Player player) {
        player.closeInventory();
        player.sendTitle(ChatColor.GREEN + "Введите никнейм", ChatColor.YELLOW + "Ввод в течение 30 секунд", 10, 70, 20);

        UUID playerId = player.getUniqueId();
        activeTransfers.put(playerId, new TransferState(player));

        new BukkitRunnable() {
            @Override
            public void run() {
                if (activeTransfers.containsKey(playerId)) {
                    activeTransfers.remove(playerId);
                    player.sendMessage(ChatColor.RED + "Время на ввод никнейма истекло!");
                }
            }
        }.runTaskLater(bankPlugin, 600L); // 600L = 30 секунд
    }

    public void handleChatInput(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (!activeTransfers.containsKey(playerId)) return;

        TransferState state = activeTransfers.get(playerId);
        event.setCancelled(true);

        String message = event.getMessage();

        if (state.isAwaitingAmount()) {
            try {
                int amount = Integer.parseInt(message);
                int playerBalance = bankPlugin.getAccountsDatabase().getPlayerMoneys(player);

                if (amount <= 0) {
                    player.sendMessage(ChatColor.RED + "Сумма должна быть больше 0.");
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    return;
                }

                if (amount > playerBalance) {
                    player.sendMessage(ChatColor.RED + "У вас недостаточно средств.");
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    activeTransfers.remove(playerId);
                    return;
                }

                Player target = state.getTargetPlayer();
                bankPlugin.getAccountsDatabase().updatePlayerMoneys(player, playerBalance - amount);
                int targetBalance = bankPlugin.getAccountsDatabase().getPlayerMoneys(target);
                bankPlugin.getAccountsDatabase().updatePlayerMoneys(target, targetBalance + amount);

                player.sendMessage(ChatColor.GREEN + "Перевод успешно совершен!");
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                if (target != null) {
                    target.sendMessage(ChatColor.GREEN + "Вы получили " + ChatColor.GOLD + amount + " АР" + ChatColor.GREEN + " от " + ChatColor.GOLD + player.getName());
                }
                activeTransfers.remove(playerId);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Введите корректное число.");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            } catch (SQLException e) {
                player.sendMessage(ChatColor.RED + "Ошибка базы данных. Попробуйте позже.");
                e.printStackTrace();
            }
        } else {
            Player target = Bukkit.getPlayer(message);

            if (target == null || !target.isOnline()) {
                player.sendMessage(ChatColor.RED + "Игрок с ником " + ChatColor.GOLD + message + ChatColor.RED + " не найден.");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return;
            }

            state.setTargetPlayer(target);
            state.setAwaitingAmount(true);
            player.sendTitle(ChatColor.GREEN + "Введите сумму", ChatColor.YELLOW + "Ввод в течение 30 секунд", 10, 70, 20);
        }
    }

    private static class TransferState {
        private final Player initiator;
        private Player targetPlayer;
        private boolean awaitingAmount = false;

        public TransferState(Player initiator) {
            this.initiator = initiator;
        }

        public Player getTargetPlayer() {
            return targetPlayer;
        }

        public void setTargetPlayer(Player targetPlayer) {
            this.targetPlayer = targetPlayer;
        }

        public boolean isAwaitingAmount() {
            return awaitingAmount;
        }

        public void setAwaitingAmount(boolean awaitingAmount) {
            this.awaitingAmount = awaitingAmount;
        }
    }

}
