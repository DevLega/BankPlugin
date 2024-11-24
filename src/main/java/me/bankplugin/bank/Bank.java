package me.bankplugin.bank;

import me.bankplugin.bank.Commands.*;
import me.bankplugin.bank.Database.AccountsDatabase;
import me.bankplugin.bank.Database.FinesDatabase;
import me.bankplugin.bank.Handlers.TransferHandler;
import me.bankplugin.bank.Listeners.BankListener;
import me.bankplugin.bank.Listeners.ChatListener;
import me.bankplugin.bank.TabCompleters.FineTabCompleter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public final class Bank extends JavaPlugin {

    private AccountsDatabase accountsDatabase;

    private TransferHandler transferHandler;
    private FinesDatabase finesDatabase;


    @Override
    public void onEnable() {
        // Transfer
        transferHandler = new TransferHandler(this);
        getServer().getPluginManager().registerEvents(new ChatListener(transferHandler), this);


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

        try {
            finesDatabase = new FinesDatabase(getDataFolder().getAbsolutePath() + "/fines.db");
            getLogger().info("База данных для штрафов успешно подключена.");
        } catch (Exception e) {
            getLogger().severe("Не удалось подключить базу данных для штрафов: " + e.getMessage());
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        TakeMoneyCommand takeMoneyCommand = new TakeMoneyCommand(this);
        getCommand("openbank").setExecutor(new OpenBankCommand(this));
        getCommand("addmoney").setExecutor(new AddMoneyCommand(this));
        getCommand("takemoney").setExecutor(takeMoneyCommand);

        getCommand("forgive").setExecutor(new FineCommands(finesDatabase));
        getCommand("fines").setExecutor(new FineCommands(finesDatabase));
        getCommand("fine").setExecutor(new FineCommands(finesDatabase));

        getCommand("fine").setTabCompleter(new FineTabCompleter());

        getServer().getPluginManager().registerEvents(new BankListener(this), this);
    }

    public AccountsDatabase getAccountsDatabase() {
        return this.accountsDatabase;
    }

    public TransferHandler getTransferHandler() {
        return transferHandler;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        try{
            accountsDatabase.closeConnection();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        try {
            finesDatabase.closeConnection();
            getLogger().info("Соединение с базой данных закрыто.");
        } catch (Exception e) {
            getLogger().severe("Не удалось закрыть соединение с базой данных: " + e.getMessage());
        }
    }
}
