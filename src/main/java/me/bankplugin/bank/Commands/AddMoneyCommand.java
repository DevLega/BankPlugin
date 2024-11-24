package me.bankplugin.bank.Commands;

import me.bankplugin.bank.Bank;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;

public class AddMoneyCommand implements CommandExecutor {

    private final Bank bankPlugin;

    public AddMoneyCommand(Bank bankPlugin) {
        this.bankPlugin = bankPlugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        if (strings.length < 2) {
            commandSender.sendMessage(ChatColor.RED + "Используйте /bank:addmoney [Игрок] [Количество]");
            return true;
        }

        // Получаем игрока
        String playerName = strings[0];
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            commandSender.sendMessage(ChatColor.RED + "Игрок не найден");
            return true;
        }

        // Получаем количество
        int amount;
        try {
            amount = Integer.parseInt(strings[1]);
        } catch (NumberFormatException e) {
            commandSender.sendMessage(ChatColor.RED + "Неверное число");
            return true;
        }

        if (amount <= 0) {
            commandSender.sendMessage(ChatColor.RED + "Количество должно быть больше нуля");
            return true;
        }

        Player player = (Player) commandSender;

        // Проверяем наличие блоков deepslate_diamond_ore
        int playerOreCount = countItem(player, Material.DEEPSLATE_DIAMOND_ORE);
        if (playerOreCount < amount) {
            commandSender.sendMessage(ChatColor.RED + "У вас недостаточно " + ChatColor.GOLD + "АР" + ChatColor.RED + ". Требуется " + ChatColor.GOLD + amount + " АР" + ChatColor.RED + ", у вас " + ChatColor.GOLD + playerOreCount + " АР");
            return true;
        }

        // Убираем блоки из инвентаря


        // Добавляем деньги к балансу игрока
        if (player.getLocation().distance(target.getLocation()) <= 10) {
            // Целевой игрок находится в радиусе 10 блоков
            try {
                removeItem(player, Material.DEEPSLATE_DIAMOND_ORE, amount);
                int currentMoney = bankPlugin.getAccountsDatabase().getPlayerMoneys(target);
                bankPlugin.getAccountsDatabase().updatePlayerMoneys(target, currentMoney + amount);

                target.playSound(target.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);

                player.sendMessage(ChatColor.GREEN + "Игроку " + ChatColor.GOLD + target.getName() + ChatColor.GREEN + " было начислено " + ChatColor.GOLD + amount + " АР");
                target.sendMessage(ChatColor.GREEN + "Вам было начислено " + ChatColor.GOLD + amount + " АР");
            } catch (SQLException e) {
                e.printStackTrace();
                player.sendMessage(ChatColor.RED + "Произошла ошибка при обновлении баланса игрока. Попробуйте позже.");
            }
        } else {
            // Игрок слишком далеко
            player.sendMessage(ChatColor.RED + "Игрок " + ChatColor.GOLD + target.getName() + ChatColor.RED + " находится слишком далеко. Подойди ближе (до 10 блоков).");
        }

        return true;
    }

    private int countItem(Player player, Material material) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                count += item.getAmount();
            }
        }
        return count;
    }

    /**
     * Удаляет указанное количество предметов из инвентаря игрока.
     */
    private void removeItem(Player player, Material material, int amount) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                if (item.getAmount() > amount) {
                    item.setAmount(item.getAmount() - amount);
                    break;
                } else {
                    amount -= item.getAmount();
                    player.getInventory().remove(item);
                }
            }
        }
    }
}
