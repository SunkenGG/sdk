package gg.sunken.sdk.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class SlackWebhook {
    private final String webhook;

    public SlackWebhook(String webhook) {
        this.webhook = webhook;
    }

    public void send(String message) {
        try {
            URL url = new URL(webhook);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            try {
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.setDoOutput(true);

                String jsonPayload = String.format("{\"text\":\"%s\"}",
                        message.replace("\"", "\\\"").replace("\n", "\\n"));
                try (OutputStream os = connection.getOutputStream()) {
                    os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
                    os.flush();
                }

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    System.err.println("Failed to send log to Slack: " + connection.getResponseMessage());
                }
            } finally {
                connection.disconnect();
            }
        } catch (IOException e) {
            System.err.println("Failed to send log to Slack: " + e.getMessage());
        }
    }
}
