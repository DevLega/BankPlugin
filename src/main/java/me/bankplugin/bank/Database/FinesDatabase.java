package me.bankplugin.bank.Database;

import me.bankplugin.bank.Models.Fine;

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
            preparedStatement.setString(2, victim);
            preparedStatement.setString(3, offender);
            preparedStatement.setString(4, reason);
            preparedStatement.setString(5, victim);
            preparedStatement.setString(6, dateIssued);
            preparedStatement.executeUpdate();
        }
    }

    public int getLastFineId() throws SQLException {
        String sql = "SELECT id FROM fines ORDER BY id DESC LIMIT 1";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getInt("id");
            } else {
                throw new SQLException("Не удалось найти последний ID штрафа.");
            }
        }
    }

    public boolean isOverdueForOneMinute(String offender, int fineId) throws SQLException {
        String query = "SELECT date_issued FROM fines WHERE id = ? AND offender = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, fineId);
            statement.setString(2, offender);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String dateIssued = resultSet.getString("date_issued");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    Date issuedDate = sdf.parse(dateIssued);
                    long diffInMillies = new Date().getTime() - issuedDate.getTime();
                    long diffInSeconds = diffInMillies / (24 * 60 * 60 * 1000);
                    return diffInSeconds >= 3;
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
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
                        rs.getString("offender"),
                        rs.getInt("amount"),
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

    public List<Integer> getOffenderFines(String offenderName) throws SQLException {
        List<Integer> fines = new ArrayList<>();
        String query = "SELECT id FROM fines WHERE offender = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, offenderName);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                fines.add(resultSet.getInt("id"));
            }
        }
        return fines;
    }

    public String getFineDetails(int fineId) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT * FROM fines WHERE id = ?")) {
            preparedStatement.setInt(1, fineId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return "\n§aНарушитель: §6" + resultSet.getString("offender") + "\n§aРазмер штрафа: §6" + resultSet.getInt("amount")
                        + "\n§aПричина: §6" + resultSet.getString("reason") + "\n§aПострадавший: §6" + resultSet.getString("victim")
                        + "\n§aДата: §6" + resultSet.getString("date_issued");
            }
        }
        return "§cШтраф не найден";
    }

    public List<String> getAllFines() throws SQLException {
        List<String> fines = new ArrayList<>();
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT * FROM fines");
            while (resultSet.next()) {
                fines.add("§aШтраф §8#" + resultSet.getInt("id") + " §aдля §6" + resultSet.getString("offender") + "\n");
            }
        }
        return fines;
    }
}
