package me.bankplugin.bank.TabCompleters;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;

public class FineTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return List.of();
    }
}
