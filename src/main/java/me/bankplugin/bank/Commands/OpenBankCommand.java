package me.bankplugin.bank.Commands;

import me.bankplugin.bank.Bank;
import me.bankplugin.bank.Menu.Menu;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class OpenBankCommand implements CommandExecutor {
    private final Bank bankPlugin;

    public OpenBankCommand(Bank bankPlugin) {
        if (bankPlugin == null) {
            throw new IllegalArgumentException("bankPlugin не может быть null");
        }
        this.bankPlugin = bankPlugin;
    }
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("Эту команду могут выполнять только игроки!");
            return true;
        }

        Player player = (Player) commandSender;

        // Создаем экземпляр Menu и передаем bankPlugin
        Menu menu = new Menu(bankPlugin);
        player.openInventory(menu.getBankMenu(player));

        return true;
    }
}
