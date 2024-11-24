package me.bankplugin.bank.Models;

public class Fine {
    private final int id;
    private final String playerName;
    private final double amount;
    private final String victim;

    public Fine(int id, String playerName, double amount, String victim) {
        this.id = id;
        this.playerName = playerName;
        this.amount = amount;
        this.victim = victim;
    }

    public int getId() {
        return id;
    }

    public String getPlayerName() {
        return playerName;
    }

    public double getAmount() {
        return amount;
    }

    public String getVictim() {
        return victim;
    }
}
