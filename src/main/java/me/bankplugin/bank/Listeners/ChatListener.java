package me.bankplugin.bank.Listeners;

import me.bankplugin.bank.Handlers.TransferHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    private final TransferHandler transferHandler;

    public ChatListener(TransferHandler transferHandler) {
        this.transferHandler = transferHandler;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        transferHandler.handleChatInput(event);
    }

}
