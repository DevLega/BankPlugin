package me.bankplugin.bank.Listeners;

import me.bankplugin.bank.Bank;
import me.bankplugin.bank.Database.FinesDatabase;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;
import java.util.List;

public class PlayerJoinListener implements Listener {

    private final FinesDatabase finesDatabase;
    private final Bank bankPlugin;

    public PlayerJoinListener(FinesDatabase finesDatabase, Bank bankPlugin) {
        this.finesDatabase = finesDatabase;
        this.bankPlugin = bankPlugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        try {

            List<Integer> playerFineIds = finesDatabase.getPlayerFines(player.getName());

            if (!playerFineIds.isEmpty()) {
                player.sendTitle(ChatColor.RED + "Оплатите штраф!", "", 10, 40, 10);

                for (int fineId : playerFineIds) {

                    if (finesDatabase.isOverdueForOneMinute(player.getName(), fineId)) {
                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);

                        new BukkitRunnable() {
                            int count = 0;

                            @Override
                            public void run() {
                                if (count >= 1000000000) {
                                    cancel();
                                    return;
                                }

                                if (count % 2 == 0) {
                                    player.sendTitle(ChatColor.BLUE + "Оплатите штраф!", "", 10, 40, 10);
                                } else {
                                    player.sendTitle(ChatColor.RED + "Оплатите штраф!", "", 10, 40, 10);
                                }
                                count++;
                            }
                        }.runTaskTimer(bankPlugin, 0L, 20L);  // Периодичность мигания (каждые 20 тиков)

                        player.sendMessage(ChatColor.RED + "Вы не оплатили штраф в течение 3 дней! Оплатите его в банке.");
                    }
                }
            }
        } catch (SQLException e) {
            player.sendMessage(ChatColor.RED + "Произошла ошибка при проверке ваших штрафов: " + e.getMessage());
        }
    }
}
