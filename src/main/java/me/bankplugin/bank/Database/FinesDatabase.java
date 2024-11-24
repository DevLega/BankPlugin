package me.bankplugin.bank.Database;

import me.bankplugin.bank.Models.Fine;
import org.bukkit.entity.Player;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FinesDatabase {

    private final Connection connection;

    public FinesDatabase(String path) throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:" + path);
        // Создание таблицы для штрафов
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS fines (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    amount INTEGER NOT NULL,
                    officer TEXT NOT NULL,
                    offender TEXT NOT NULL,
                    reason TEXT NOT NULL,
                    victim TEXT NOT NULL,
                    date_issued TEXT NOT NULL)
            """);
        }
    }

    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    public void addFine(String offender, String victim, int amount, String reason) throws SQLException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateIssued = sdf.format(new Date());

        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO fines (amount, officer, offender, reason, victim, date_issued) VALUES (?, ?, ?, ?, ?, ?)")) {
            preparedStatement.setInt(1, amount);
            preparedStatement.setString(2, victim);  // пострадавший выписывает штраф
            preparedStatement.setString(3, offender);
            preparedStatement.setString(4, reason);
            preparedStatement.setString(5, victim);  // пострадавший получает деньги
            preparedStatement.setString(6, dateIssued);
            preparedStatement.executeUpdate();
        }
    }

    public List<Integer> getAllFineIds() throws SQLException {
        List<Integer> fineIds = new ArrayList<>();
        String query = "SELECT id FROM fines";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                fineIds.add(resultSet.getInt("id"));
            }
        }
        return fineIds;
    }

    public void removeFine(int fineId) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM fines WHERE id = ?")) {
            preparedStatement.setInt(1, fineId);
            preparedStatement.executeUpdate();
        }
    }

    public Fine getFineById(int fineId) throws SQLException {
        String query = "SELECT * FROM fines WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, fineId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Fine(
                        rs.getInt("id"),
                        rs.getString("playerName"),
                        rs.getDouble("amount"),
                        rs.getString("victim")
                );
            }
        }
        return null;
    }

    public boolean isVictim(String playerName, int fineId) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT victim FROM fines WHERE id = ?")) {
            preparedStatement.setInt(1, fineId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("victim").equals(playerName);
            }
        }
        return false;
    }

    public List<Integer> getPlayerFines(String playerName) throws SQLException {
        List<Integer> fineIds = new ArrayList<>();

        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT id FROM fines WHERE offender = ? OR victim = ?")) {
            preparedStatement.setString(1, playerName);
            preparedStatement.setString(2, playerName);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                fineIds.add(resultSet.getInt("id"));
            }
        }

        return fineIds;
    }



    public String getFineDetails(int fineId) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT * FROM fines WHERE id = ?")) {
            preparedStatement.setInt(1, fineId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return "Нарушитель: " + resultSet.getString("offender") + ", Размер штрафа: " + resultSet.getInt("amount")
                        + ", Причина: " + resultSet.getString("reason") + ", Пострадавший: " + resultSet.getString("victim")
                        + ", Дата: " + resultSet.getString("date_issued");
            }
        }
        return "Штраф не найден";
    }

    public List<String> getAllFines() throws SQLException {
        List<String> fines = new ArrayList<>();
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT * FROM fines");
            while (resultSet.next()) {
                fines.add("Штраф #" + resultSet.getInt("id") + " для " + resultSet.getString("offender")
                        + " на сумму " + resultSet.getInt("amount") + ", Причина: " + resultSet.getString("reason"));
            }
        }
        return fines;
    }
}
