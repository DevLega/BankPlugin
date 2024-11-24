package me.bankplugin.bank.TabCompleters;

import me.bankplugin.bank.Database.FinesDatabase;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FineTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        List<String> completions = new ArrayList<>();

        if (strings.length == 1) {
            // Первое слово после /fine: add, remove, lookup, list
            if (commandSender.hasPermission("bank.fine")) {
                completions.add("add");
                completions.add("remove");
                completions.add("lookup");
                completions.add("list");
            }
        } else if (strings.length == 2) {
            // Второе слово, в зависимости от команды
            if (strings[0].equalsIgnoreCase("remove") || strings[0].equalsIgnoreCase("lookup")) {
                // Например, для /fine remove <номер штрафа> или /fine lookup <номер штрафа>
                if (commandSender instanceof Player player) {
                    // Здесь можно добавить логику, чтобы показать номера штрафов для этого игрока
                    // Например, если у игрока есть штрафы, то добавьте их в completions
                    completions.add("1");
                    completions.add("2");
                    completions.add("3");
                    // Можно добавить логику для извлечения реальных номеров штрафов
                }
            }
        }

        return completions;
    }
}
