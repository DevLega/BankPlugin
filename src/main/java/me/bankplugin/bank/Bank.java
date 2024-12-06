package me.bankplugin.bank;

import me.bankplugin.bank.Commands.*;
import me.bankplugin.bank.Database.AccountsDatabase;
import me.bankplugin.bank.Database.FinesDatabase;
import me.bankplugin.bank.Handlers.TransferHandler;
import me.bankplugin.bank.Listeners.BankListener;
import me.bankplugin.bank.Listeners.ChatListener;
import me.bankplugin.bank.Listeners.PlayerJoinListener;
import me.bankplugin.bank.Menu.Menu;
import me.bankplugin.bank.TabCompleters.FineTabCompleter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import javax.swing.event.MenuListener;
import java.nio.file.Paths;
import java.sql.SQLException;

public final class Bank extends JavaPlugin {

    private AccountsDatabase accountsDatabase;
    private TransferHandler transferHandler;
    private FinesDatabase finesDatabase;

    @Override
    public void onEnable() {
        // Инициализация папки плагина
        if (!getDataFolder().exists()) {
            if (getDataFolder().mkdirs()) {
                getLogger().info("Создана папка для данных плагина.");
            }
        }

        // Подключение баз данных
        initDatabases();

        // Инициализация обработчиков
        transferHandler = new TransferHandler(this);

        // Регистрация команд
        registerCommands();

        // Регистрация слушателей событий
        registerListeners();

        getLogger().info("Плагин успешно включен!");
    }

    @Override
    public void onDisable() {
        closeDatabases();
        getLogger().info("Плагин успешно выключен.");
    }

    private void initDatabases() {
        try {
            accountsDatabase = new AccountsDatabase(Paths.get(getDataFolder().getAbsolutePath(), "banks.db").toString());
            getLogger().info("База данных счетов успешно подключена.");
        } catch (SQLException ex) {
            getLogger().severe("Не удалось подключить базу данных счетов: " + ex.getMessage());
            ex.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        try {
            finesDatabase = new FinesDatabase(Paths.get(getDataFolder().getAbsolutePath(), "fines.db").toString());
            getLogger().info("База данных штрафов успешно подключена.");
        } catch (SQLException ex) {
            getLogger().severe("Не удалось подключить базу данных штрафов: " + ex.getMessage());
            ex.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    private void registerCommands() {
        if (getCommand("openbank") != null) {
            getCommand("openbank").setExecutor(new OpenBankCommand(finesDatabase, accountsDatabase, this));
        }
        if (getCommand("addmoney") != null) {
            getCommand("addmoney").setExecutor(new AddMoneyCommand(this));
        }
        if (getCommand("takemoney") != null) {
            getCommand("takemoney").setExecutor(new TakeMoneyCommand(this));
        }
        if (getCommand("forgive") != null) {
            getCommand("forgive").setExecutor(new FineCommands(finesDatabase));
        }
        if (getCommand("fines") != null) {
            getCommand("fines").setExecutor(new FineCommands(finesDatabase));
        }
        if (getCommand("fine") != null) {
            getCommand("fine").setExecutor(new FineCommands(finesDatabase));
        }
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new ChatListener(transferHandler), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(finesDatabase, this), this);

        Menu menu = new Menu(finesDatabase, accountsDatabase, this);

        getServer().getPluginManager().registerEvents(new BankListener(finesDatabase, accountsDatabase, this, menu), this);
    }

    private void closeDatabases() {
        try {
            if (accountsDatabase != null) {
                accountsDatabase.closeConnection();
                getLogger().info("Соединение с базой данных счетов закрыто.");
            }
        } catch (SQLException ex) {
            getLogger().severe("Ошибка при закрытии соединения с базой данных счетов: " + ex.getMessage());
            ex.printStackTrace();
        }

        try {
            if (finesDatabase != null) {
                finesDatabase.closeConnection();
                getLogger().info("Соединение с базой данных штрафов закрыто.");
            }
        } catch (SQLException ex) {
            getLogger().severe("Ошибка при закрытии соединения с базой данных штрафов: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public AccountsDatabase getAccountsDatabase() {
        return accountsDatabase;
    }

    public TransferHandler getTransferHandler() {
        return transferHandler;
    }
}
