package me.bankplugin.bank.Commands;

import me.bankplugin.bank.Discord.DiscordWebhookSender;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import me.bankplugin.bank.Database.FinesDatabase;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class FineCommands implements CommandExecutor {

    private final FinesDatabase finesDatabase;

    public FineCommands(FinesDatabase finesDatabase) {
        this.finesDatabase = finesDatabase;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Эта команда доступна только для игроков.");
            return true;
        }

        String commandName = command.getName().toLowerCase();

        switch (commandName) {
            case "forgive":
                return handleForgiveCommand(player, args);

            case "fines":
                return handleFinesCommand(player, args);

            case "fine":
                return handleFineCommand(player, args);

            default:
                player.sendMessage(ChatColor.RED + "Неизвестная команда.");
                return false;
        }
    }

    private boolean handleForgiveCommand(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage(ChatColor.RED + "Использование: /forgive <Игрок> <ID штрафа>");
            return false;
        }

        String offenderName = args[0];
        int fineId;

        try {
            fineId = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "ID штрафа должен быть числом.");
            return false;
        }

        try {
            if (finesDatabase.isVictim(player.getName(), fineId)) {
                finesDatabase.removeFine(fineId);
                player.sendMessage(ChatColor.GREEN + "Вы простили штраф для игрока " + ChatColor.GOLD + offenderName);
            } else {
                player.sendMessage(ChatColor.RED + "Вы не являетесь пострадавшим по этому штрафу.");
            }
        } catch (SQLException e) {
            player.sendMessage(ChatColor.RED + "Ошибка при обработке штрафа: " + e.getMessage());
        }
        return true;
    }

    private boolean handleFinesCommand(Player player, String[] args) {
        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Использование: /fines <Игрок>");
            return false;
        }

        if (!player.hasPermission("bank.fines")) {
            player.sendMessage(ChatColor.RED + "У вас нет прав на просмотр штрафов.");
            return true;
        }

        String offenderName = args[0];
        try {
            List<Integer> fineIds = finesDatabase.getPlayerFines(offenderName);
            player.sendMessage(ChatColor.GREEN + "Штрафы игрока " + ChatColor.GOLD + offenderName + ": " + fineIds);
        } catch (SQLException e) {
            player.sendMessage(ChatColor.RED + "Ошибка при получении штрафов: " + e.getMessage());
        }
        return true;
    }

    private boolean handleFineCommand(Player player, String[] args) {
        if (args.length < 1) {
            sendFineCommandUsage(player);
            return false;
        }

        switch (args[0].toLowerCase()) {
            case "add":
                return handleFineAdd(player, args);

            case "remove":
                return handleFineRemove(player, args);

            case "lookup":
                return handleFineLookup(player, args);

            case "list":
                return handleFineList(player);

            default:
                sendFineCommandUsage(player);
                return false;
        }
    }

    private boolean handleFineAdd(Player player, String[] args) {
        if (args.length < 5) {
            player.sendMessage(ChatColor.RED + "Использование: /fine add <Нарушитель> <Пострадавший> <Сумма> <Причина>");
            return false;
        }

        if (!player.hasPermission("bank.fine")) {
            player.sendMessage(ChatColor.RED + "У вас нет прав на добавление штрафов.");
            return true;
        }

        String offender = args[1];
        String victim = args[2];
        int amount;

        try {
            amount = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Сумма штрафа должна быть числом.");
            return false;
        }

        String reason = String.join(" ", Arrays.copyOfRange(args, 4, args.length));
        String issuer = player.getName(); // Имя выписывающего штраф

        try {
            finesDatabase.addFine(offender, victim, amount, reason);

            player.sendMessage(ChatColor.GREEN + "Штраф для игрока " + ChatColor.GOLD + offender + ChatColor.GREEN +
                    " на сумму " + ChatColor.GOLD + amount + ChatColor.GREEN + " был добавлен.");

            DiscordWebhookSender.sendWebhook(offender, finesDatabase.getLastFineId(), amount, issuer, victim, reason);

            Player offenderPlayer = Bukkit.getPlayer(offender);
            if (offenderPlayer != null && offenderPlayer.isOnline()) {
                offenderPlayer.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                offenderPlayer.sendMessage(ChatColor.RED + "Вам был выписан штраф на сумму §6" + amount + " АР §cза нарушение. Причина: §6" + reason);
            }
        } catch (SQLException e) {
            player.sendMessage(ChatColor.RED + "Ошибка при добавлении штрафа: " + e.getMessage());
        }
        return true;
    }


    private boolean handleFineRemove(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage(ChatColor.RED + "Использование: /fine remove <ID штрафа>");
            return false;
        }

        if (!player.hasPermission("bank.fine")) {
            player.sendMessage(ChatColor.RED + "У вас нет прав на удаление штрафов.");
            return true;
        }

        int fineId;

        try {
            fineId = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "ID штрафа должен быть числом.");
            return false;
        }

        try {
            finesDatabase.removeFine(fineId);
            player.sendMessage(ChatColor.GREEN + "Штраф с номером " + ChatColor.GOLD + fineId + ChatColor.GREEN + " был удалён.");
        } catch (SQLException e) {
            player.sendMessage(ChatColor.RED + "Ошибка при удалении штрафа: " + e.getMessage());
        }
        return true;
    }

    private boolean handleFineLookup(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage(ChatColor.RED + "Использование: /fine lookup <ID штрафа>");
            return false;
        }

        if (!player.hasPermission("bank.lookup")) {
            player.sendMessage(ChatColor.RED + "У вас нет прав на просмотр информации о штрафах.");
            return true;
        }

        int fineId;

        try {
            fineId = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "ID штрафа должен быть числом.");
            return false;
        }

        try {
            String fineDetails = finesDatabase.getFineDetails(fineId);
            player.sendMessage(ChatColor.GOLD + "Штраф #" + fineId + ": " + ChatColor.GREEN + fineDetails);
        } catch (SQLException e) {
            player.sendMessage(ChatColor.RED + "Ошибка при получении информации о штрафе: " + e.getMessage());
        }
        return true;
    }

    private boolean handleFineList(Player player) {
        if (!player.hasPermission("bank.fine")) {
            player.sendMessage(ChatColor.RED + "У вас нет прав на просмотр списка штрафов.");
            return true;
        }

        try {
            List<String> fines = finesDatabase.getAllFines();
            player.sendMessage(ChatColor.GOLD + "Список всех штрафов:\n" + ChatColor.GREEN + String.join("\n", fines));
        } catch (SQLException e) {
            player.sendMessage(ChatColor.RED + "Ошибка при получении списка штрафов: " + e.getMessage());
        }
        return true;
    }

    private void sendFineCommandUsage(Player player) {
        player.sendMessage(ChatColor.RED + "Использование команды /fine:");
        player.sendMessage(ChatColor.GRAY + "/fine add <Нарушитель> <Пострадавший> <Сумма> <Причина>");
        player.sendMessage(ChatColor.GRAY + "/fine remove <ID штрафа>");
        player.sendMessage(ChatColor.GRAY + "/fine lookup <ID штрафа>");
        player.sendMessage(ChatColor.GRAY + "/fine list");
    }
}
