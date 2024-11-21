package me.bankplugin.bank;

import me.bankplugin.bank.Commands.AddMoneyCommand;
import me.bankplugin.bank.Commands.OpenBankCommand;
import me.bankplugin.bank.Database.AccountsDatabase;
import me.bankplugin.bank.Listeners.BankListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public final class Bank extends JavaPlugin {

    private AccountsDatabase accountsDatabase;
    @Override
    public void onEnable() {
        // Plugin startup logic

        // SQLite
        try {

            if(!getDataFolder().exists()) {
                getDataFolder().mkdirs();
            }
            accountsDatabase = new AccountsDatabase(getDataFolder().getAbsolutePath() + "/banks.db");

        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Failed to connect to the database! " + ex.getMessage());
            Bukkit.getPluginManager().disablePlugin(this);
        }


        getCommand("openbank").setExecutor(new OpenBankCommand(this));
        getCommand("addmoney").setExecutor(new AddMoneyCommand(this));

        getServer().getPluginManager().registerEvents(new BankListener(), this);
    }

    public AccountsDatabase getAccountsDatabase() {
        return this.accountsDatabase;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        try{
            accountsDatabase.closeConnection();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
