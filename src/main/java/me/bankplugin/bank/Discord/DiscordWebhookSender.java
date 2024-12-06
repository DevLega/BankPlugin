package me.bankplugin.bank.Discord;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DiscordWebhookSender {
    private static final String WEBHOOK_URL = "https://discord.com/api/webhooks/1311412276628295810/h-COkgtTALCjVW6Otic9xSp1JnA6VV36moYgV_pAQxq2HrWAumYXHBItJElY9U9K0pUgit remote add origin https://github.com/DevLega/BankPlugin.git";

    public static void sendWebhook(String offender, int fineId, int amount, String issuer, String victim, String reason) {
        try {
            URL url = new URL(WEBHOOK_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");

            String skinUrl = "https://vzge.me/bust/256/" + offender + ".png?y=-40";
            String jsonPayload = "{"
                    + "\"embeds\": [{"
                    + "\"title\": \":receipt: Игроку " + offender + " был выписан штраф #" + fineId + " в размере " + amount + " АР!\","
                    + "\"description\": \":police_officer: **Выписал** — " + issuer + "\\n"
                    + ":bust_in_silhouette: **Пострадавший** — " + victim + "\\n"
                    + ":notepad_spiral: **Причина** — " + reason + "\","
                    + "\"thumbnail\": {\"url\": \"" + skinUrl + "\"},"
                    + "\"color\": 5620992"
                    + "}]"
                    + "}";

            try (OutputStream os = connection.getOutputStream()) {
                os.write(jsonPayload.getBytes());
                os.flush();
            }

            int responseCode = connection.getResponseCode();
            if (responseCode != 204) {
                System.out.println("Ошибка при отправке вебхука: " + connection.getResponseMessage());
            } else {
                System.out.println("Вебхук успешно отправлен!");
            }

        } catch (Exception e) {
            System.err.println("Ошибка при отправке вебхука: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        sendWebhook("offenderPlayer", 12345, 500, "issuerPlayer", "victimPlayer", "Reason for fine");
    }
}
