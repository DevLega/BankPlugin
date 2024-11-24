package me.bankplugin.bank.Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import me.bankplugin.bank.Database.FinesDatabase;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class FineCommands implements CommandExecutor {

    private final FinesDatabase finesDatabase;

    public FineCommands(FinesDatabase finesDatabase) {
        this.finesDatabase = finesDatabase;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (command.getName().equalsIgnoreCase("forgive") && strings.length == 2) {
            if (commandSender instanceof Player player) {
                String offenderName = strings[0];
                int fineId;
                try {
                    fineId = Integer.parseInt(strings[1]);
                } catch (NumberFormatException e) {
                    player.sendMessage("Неверный формат для ID штрафа.");
                    return false;
                }

                // Проверка, является ли игрок пострадавшим
                try {
                    if (finesDatabase.isVictim(player.getName(), fineId)) {
                        finesDatabase.removeFine(fineId);
                        player.sendMessage("Вы простили штраф для игрока " + offenderName);
                    } else {
                        player.sendMessage("Вы не можете простить этот штраф, так как не являетесь пострадавшим.");
                    }
                } catch (SQLException e) {
                    player.sendMessage("Ошибка при обработке штрафа: " + e.getMessage());
                }
            }
            return true;
        }

        // Проверка команды "/fines <игрок>"
        if (command.getName().equalsIgnoreCase("fines") && strings.length == 1) {
            if (commandSender instanceof Player player) {
                if (player.hasPermission("bank.fines")) {
                    String offenderName = strings[0];
                    try {
                        List<Integer> fineIds = finesDatabase.getPlayerFines(offenderName);
                        player.sendMessage("Штрафы игрока " + offenderName + ": " + fineIds.toString());
                    } catch (SQLException e) {
                        player.sendMessage("Ошибка при получении штрафов: " + e.getMessage());
                    }
                } else {
                    player.sendMessage("У вас нет прав на просмотр штрафов!");
                }
            }
            return true;
        }

        // Проверка команды "/fine add <Нарушитель> <Пострадавший> <Сумма> <Причина>"
        if (command.getName().equalsIgnoreCase("fine") && strings.length >= 4 && strings[0].equalsIgnoreCase("add")) {
            if (strings.length < 4) {
                // Если не хватает аргументов, отправляем сообщение с подсказкой
                commandSender.sendMessage(ChatColor.RED + "Попробуйте: /fine add <Нарушитель> <Пострадавший> <Сумма> <Причина>");
                return false;
            }

            String offender = strings[1];
            String victim = strings[2];
            int amount;
            try {
                amount = Integer.parseInt(strings[3]); // Попытка конвертации суммы штрафа
            } catch (NumberFormatException e) {
                // Если сумма штрафа не является числом, отправляем ошибку
                commandSender.sendMessage(ChatColor.RED + "Неверный формат для суммы штрафа. Сумма должна быть числом.");
                return false;
            }

            // Собираем причину из всех оставшихся аргументов
            String reason = String.join(" ", Arrays.copyOfRange(strings, 4, strings.length));

            try {
                finesDatabase.addFine(offender, victim, amount, reason); // Добавление штрафа
                commandSender.sendMessage(ChatColor.GREEN + "Штраф для игрока " + ChatColor.GOLD + offender + ChatColor.GREEN + " на сумму " + ChatColor.GOLD + amount + ChatColor.GREEN + " был добавлен.");
            } catch (SQLException e) {
                // Обработка ошибок при добавлении штрафа в базу данных
                commandSender.sendMessage(ChatColor.RED + "Ошибка при добавлении штрафа: " + e.getMessage());
            }
            return true;
        }


        // Проверка команды "/fine remove <номер штрафа>"
        if (command.getName().equalsIgnoreCase("fine") && strings.length == 2 && strings[0].equalsIgnoreCase("remove")) {
            if (commandSender instanceof Player player) {
                if (player.hasPermission("bank.fine")) {
                    int fineId;
                    try {
                        fineId = Integer.parseInt(strings[1]);
                    } catch (NumberFormatException e) {
                        player.sendMessage("Неверный формат для ID штрафа.");
                        return false;
                    }

                    try {
                        finesDatabase.removeFine(fineId);
                        player.sendMessage("Штраф с номером " + fineId + " был удалён.");
                    } catch (SQLException e) {
                        player.sendMessage("Ошибка при удалении штрафа: " + e.getMessage());
                    }
                } else {
                    player.sendMessage("У вас нет прав на удаление штрафов!");
                }
            }
            return true;
        }

        // Проверка команды "/fine lookup <номер штрафа>"
        if (command.getName().equalsIgnoreCase("fine") && strings.length == 2 && strings[0].equalsIgnoreCase("lookup")) {
            if (commandSender instanceof Player player) {
                if (player.hasPermission("bank.lookup")) {
                    int fineId;
                    try {
                        fineId = Integer.parseInt(strings[1]);
                    } catch (NumberFormatException e) {
                        player.sendMessage("Неверный формат для ID штрафа.");
                        return false;
                    }

                    try {
                        String fineDetails = finesDatabase.getFineDetails(fineId);
                        player.sendMessage("Штраф " + fineId + ": " + fineDetails);
                    } catch (SQLException e) {
                        player.sendMessage("Ошибка при получении информации о штрафе: " + e.getMessage());
                    }
                } else {
                    player.sendMessage("У вас нет прав на просмотр информации о штрафе!");
                }
            }
            return true;
        }

        // Проверка команды "/fine list"
        if (command.getName().equalsIgnoreCase("fine") && strings.length == 1 && strings[0].equalsIgnoreCase("list")) {
            if (commandSender instanceof Player player) {
                if (player.hasPermission("bank.fine")) {
                    try {
                        List<String> fines = finesDatabase.getAllFines();
                        player.sendMessage("Все штрафы: " + fines.toString());
                    } catch (SQLException e) {
                        player.sendMessage("Ошибка при получении списка штрафов: " + e.getMessage());
                    }
                } else {
                    player.sendMessage("У вас нет прав на просмотр всех штрафов!");
                }
            }
            return true;
        }

        return false;
    }
}
