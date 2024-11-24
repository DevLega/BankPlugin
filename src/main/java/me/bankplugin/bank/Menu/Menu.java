package me.bankplugin.bank.Menu;

import me.bankplugin.bank.Bank;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Menu {

    private final Bank bankPlugin;

    // Конструктор класса Menu для получения экземпляра плагина
    public Menu(Bank bankPlugin) {
        this.bankPlugin = bankPlugin;
    }

    public static Inventory getAccountsMenu() {

        Inventory accountsMenu = Bukkit.createInventory(null, 27, "Счета");

        //BUTTONS
        ItemStack backHead = new ItemStack(Material.PLAYER_HEAD);
        ItemStack accountHead = new ItemStack(Material.PLAYER_HEAD);
        ItemStack grayPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);

        //META
        ItemMeta backHeadMeta = backHead.getItemMeta();
        backHeadMeta.setDisplayName(ChatColor.RED + "Назад");
        backHead.setItemMeta(backHeadMeta);

        ItemMeta accountHeadMeta = accountHead.getItemMeta();
        accountHeadMeta.setDisplayName(ChatColor.GOLD + "Счёт");
        accountHead.setItemMeta(accountHeadMeta);

        ItemMeta paneMeta = grayPane.getItemMeta();
        if (paneMeta != null) {
            paneMeta.setDisplayName(" "); // Пустое имя, чтобы не мешать
            grayPane.setItemMeta(paneMeta);
        }

        for (int i = 0; i < accountsMenu.getSize(); i++) {
            if (accountsMenu.getItem(i) == null) {
                accountsMenu.setItem(i, grayPane);
            }
        }

        //MENU
        accountsMenu.setItem(0, backHead);
        accountsMenu.setItem(4, accountHead);

        return accountsMenu;
    }

    public Inventory getBankMenu(Player player) {
        Inventory bankInventory = Bukkit.createInventory(player, 27, "Банк");

        // BD

        int playerMoneys = 0;
        try {
            playerMoneys = bankPlugin.getAccountsDatabase().getPlayerMoneys(player);
        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage(ChatColor.RED + "Ошибка при получении данных о балансе игрока");
            return null;  // Возвращаем null, если произошла ошибка
        }


        //BUTTONS
        ItemStack bankHead = new ItemStack(Material.PLAYER_HEAD);
        ItemStack translateHead = new ItemStack(Material.PLAYER_HEAD);
        ItemStack myAccountHead = new ItemStack(Material.PLAYER_HEAD);
        ItemStack myFinesHead = new ItemStack(Material.PLAYER_HEAD);
        ItemStack CountryHead = new ItemStack(Material.PLAYER_HEAD);

        ItemStack grayPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);

        //META
        ItemMeta bankHeadMeta = bankHead.getItemMeta();
        bankHeadMeta.setDisplayName(ChatColor.GOLD + "Банк");
        bankHead.setItemMeta(bankHeadMeta);

        ItemMeta translateHeadMeta = translateHead.getItemMeta();
        translateHeadMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Переводы");
        translateHead.setItemMeta(translateHeadMeta);

        ItemMeta myAccountMeta = myAccountHead.getItemMeta();
        myAccountMeta.setDisplayName(ChatColor.GOLD + "Счёт");
        List<String> loresList = new ArrayList<>();
        loresList.add(ChatColor.GREEN + "" + playerMoneys + " АР");
        loresList.add(ChatColor.WHITE + "Владелец: " + player.getName());
        myAccountMeta.setLore(loresList);
        myAccountHead.setItemMeta(myAccountMeta);

        ItemMeta myFinesMeta = myFinesHead.getItemMeta();
        myFinesMeta.setDisplayName(ChatColor.RED + "Мои штрафы");
        myFinesHead.setItemMeta(myFinesMeta);

        ItemMeta CountryMeta = CountryHead.getItemMeta();
        CountryMeta.setDisplayName(ChatColor.GOLD + "Казна государства");
        List<String> myCountryList = new ArrayList<>();
        myCountryList.add(ChatColor.GREEN + "1,056" + " АР");
        CountryMeta.setLore(myCountryList);
        CountryHead.setItemMeta(CountryMeta);

        //GLASS

        ItemMeta paneMeta = grayPane.getItemMeta();
        if (paneMeta != null) {
            paneMeta.setDisplayName(" ");
            grayPane.setItemMeta(paneMeta);
        }

        for (int i = 0; i < bankInventory.getSize(); i++) {
            if (bankInventory.getItem(i) == null) {
                bankInventory.setItem(i, grayPane);
            }
        }

        //MENU

        bankInventory.setItem(0, CountryHead);
        bankInventory.setItem(4, bankHead);
        bankInventory.setItem(20, translateHead);
        bankInventory.setItem(22, myAccountHead);
        bankInventory.setItem(24, myFinesHead);

        return bankInventory;
    }

    public Inventory getFineMenu(Player player) {
        Inventory fineInventory = Bukkit.createInventory(player, 45, "Штрафы");

        // BD

        //BUTTONS

        ItemStack titleHead = new ItemStack(Material.PLAYER_HEAD);
        ItemStack fineHead = new ItemStack(Material.PLAYER_HEAD);


        ItemStack grayPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);

        //META
        ItemMeta titleHeadMeta = titleHead.getItemMeta();
        titleHeadMeta.setDisplayName(ChatColor.GOLD + "Банк");
        titleHead.setItemMeta(titleHeadMeta);


        ItemMeta fineHeadMeta = fineHead.getItemMeta();
        fineHeadMeta.setDisplayName(ChatColor.GOLD + "Штраф " + ChatColor.GRAY + "#");
        List<String> loresList = new ArrayList<>();
        loresList.add(ChatColor.GREEN + "Сумма штрафа: " + ChatColor.GOLD + " АР");
        loresList.add(ChatColor.GREEN + "Пострадавший: " + ChatColor.GOLD + "НИК");
        fineHeadMeta.setLore(loresList);
        fineHead.setItemMeta(fineHeadMeta);

        //GLASS

        ItemMeta paneMeta = grayPane.getItemMeta();
        if (paneMeta != null) {
            paneMeta.setDisplayName(" ");
            grayPane.setItemMeta(paneMeta);
        }

        for (int i = 0; i < fineInventory.getSize(); i++) {
            if (fineInventory.getItem(i) == null) {
                fineInventory.setItem(i, grayPane);
            }
        }

        //MENU

        fineInventory.setItem(4, titleHead);
        fineInventory.setItem(20, fineHead);

        return fineInventory;
    }

}
