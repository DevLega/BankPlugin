package me.bankplugin.bank.Commands;

import me.bankplugin.bank.Bank;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class TakeMoneyCommand implements CommandExecutor {

    private final Bank bankPlugin;
    private final HashMap<UUID, Long> cooldowns = new HashMap<>();
    private final HashMap<String, Boolean> messageStatus = new HashMap<>();

    public TakeMoneyCommand(Bank bankPlugin) {
        this.bankPlugin = bankPlugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player banker)) {
            commandSender.sendMessage(ChatColor.RED + "Эту команду может использовать только игрок.");
            return true;
        }

        // Проверка аргументов
        if (strings.length != 2) {
            banker.sendMessage(ChatColor.RED + "Использование: /takemoney <игрок> <сумма>");
            return true;
        }

        Player target = Bukkit.getPlayer(strings[0]);
        if (target == null || !target.isOnline()) {
            banker.sendMessage(ChatColor.RED + "Игрок с ником " + strings[0] + " не найден.");
            return true;
        }

        // Проверка расстояния
        if (banker.getLocation().distance(target.getLocation()) > 10) {
            banker.sendMessage(ChatColor.RED + "Игрок находится слишком далеко.");
            return true;
        }

        // Проверка суммы
        int amount;
        try {
            amount = Integer.parseInt(strings[1]);
            if (amount <= 0) {
                banker.sendMessage(ChatColor.RED + "Сумма должна быть больше 0.");
                return true;
            }
        } catch (NumberFormatException e) {
            banker.sendMessage(ChatColor.RED + "Введите корректное число.");
            return true;
        }

        // Проверка баланса игрока
        try {
            int targetBalance = bankPlugin.getAccountsDatabase().getPlayerMoneys(target);
            if (targetBalance < amount) {
                banker.sendMessage(ChatColor.RED + "У игрока недостаточно средств.");
                return true;
            }
        } catch (SQLException e) {
            banker.sendMessage(ChatColor.RED + "Ошибка базы данных. Попробуйте позже.");
            e.printStackTrace();
            return true;
        }

        try {
            int targetBalance = bankPlugin.getAccountsDatabase().getPlayerMoneys(target);

            // Обновляем базы данных
            bankPlugin.getAccountsDatabase().updatePlayerMoneys(target, targetBalance - amount);

            // Выдача предмета банкиру
            ItemStack diamonds = new ItemStack(Material.DEEPSLATE_DIAMOND_ORE, amount);
            banker.getInventory().addItem(diamonds);

            // Сообщения и звуки
            banker.sendMessage(ChatColor.GREEN + "Списание успешно. Вы получили " + ChatColor.GOLD + amount + " АР.");
            banker.playSound(banker.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);

            target.sendMessage( ChatColor.GOLD + banker.getName() + ChatColor.GREEN + " списал у вас " + ChatColor.GOLD + amount + " АР" + ChatColor.GREEN + ", на вашем балансе осталось " + ChatColor.GOLD + (targetBalance - amount) + " АР.");
            target.playSound(target.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);

        } catch (
                SQLException e) {
            banker.sendMessage(ChatColor.RED + "Ошибка базы данных. Попробуйте позже.");
            e.printStackTrace();
        }

        return true;
    }
}
