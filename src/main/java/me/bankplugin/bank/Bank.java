package me.bankplugin.bank;

import me.bankplugin.bank.Commands.OpenBankCommand;
import me.bankplugin.bank.Listeners.BankListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class Bank extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic

        getCommand("bank").setExecutor(new OpenBankCommand());
        getServer().getPluginManager().registerEvents(new BankListener(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
