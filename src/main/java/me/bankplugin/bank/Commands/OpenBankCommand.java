package me.bankplugin.bank.Commands;

import me.bankplugin.bank.Bank;
import me.bankplugin.bank.Database.AccountsDatabase;
import me.bankplugin.bank.Database.FinesDatabase;
import me.bankplugin.bank.Menu.Menu;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class OpenBankCommand implements CommandExecutor {

    private final Bank bankPlugin;
    private final FinesDatabase finesDatabase;
    private final AccountsDatabase accountsDatabase;

    public OpenBankCommand(FinesDatabase finesDatabase, AccountsDatabase accountsDatabase, Bank bankPlugin) {
        this.finesDatabase = finesDatabase;
        this.accountsDatabase = accountsDatabase;
        this.bankPlugin = bankPlugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("Эту команду могут выполнять только игроки!");
            return true;
        }

        Player player = (Player) commandSender;

        Menu menu = new Menu(finesDatabase, accountsDatabase, bankPlugin);
        player.openInventory(menu.getBankMenu(player));

        return true;
    }
}
