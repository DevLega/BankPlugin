package me.bankplugin.bank.Database;

import org.bukkit.entity.Player;

import java.sql.*;

public class AccountsDatabase {

    private final Connection connection;

    public AccountsDatabase(String path) throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:" + path);
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS players (
                    uuid TEXT PRIMARY KEY,
                    username TEXT NOT NULL,
                    moneys INTEGER NOT NULL DEFAULT 0
                    )
        """);
        }
    }

    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()){
            connection.close();
        }
    }

    public int getPlayerBalance(String playerName) throws SQLException {
        String query = "SELECT moneys FROM players WHERE username = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, playerName);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("moneys");
            }
        }
        return 0;
    }

    public void updateBalance(String playerName, int newBalance) throws SQLException {
        String query = "UPDATE players SET moneys = ? WHERE username = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, newBalance);
            statement.setString(2, playerName);
            statement.executeUpdate();
        }
    }

    public void addPlayer(Player p) throws SQLException {
        try(PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO players (uuid, username, moneys) VALUES (?, ?, 0)")) {
            preparedStatement.setString(1, p.getUniqueId().toString());
            preparedStatement.setString(2, p.getDisplayName());
            preparedStatement.executeUpdate();
        }
    }

    public boolean playerExists(Player player) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM players WHERE uuid = ?")){
            preparedStatement.setString(1, player.getUniqueId().toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        }
    }

    public void updatePlayerMoneys(Player player, int moneys) throws SQLException {

        if (!playerExists(player)) {
            addPlayer(player);
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE players SET moneys = ? WHERE uuid = ?")) {
            preparedStatement.setInt(1, moneys);
            preparedStatement.setString(2, player.getUniqueId().toString());
            preparedStatement.executeUpdate();

        }
    }

    public int getPlayerMoneys(Player player) throws SQLException {
        if (!playerExists(player)) {
            addPlayer(player);
        }

        int balance = 0;

        if (connection == null) {
            throw new SQLException("Database connection is not established.");
        }

        String query = "SELECT moneys FROM players WHERE uuid = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, player.getUniqueId().toString());

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    balance = resultSet.getInt("moneys");
                } else {
                    System.out.println("Player not found in the database: " + player.getUniqueId());
                    return 0;
                }
            } catch (SQLException e) {
                System.out.println("Error while processing result set: " + e.getMessage());
                throw e;
            }
        } catch (SQLException e) {
            System.out.println("Error while executing SQL query: " + e.getMessage());
            throw e;
        }

        return balance;
    }
}
