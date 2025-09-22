package org.example;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.telegram.telegrambots.updatesreceivers.DefaultWebhook;

public class WebhookServer {
    public static void main(String[] args) {
        try {
            String port = System.getenv("PORT");
            if (port == null || port.isEmpty()) port = "8080";

            String externalUrl = System.getenv("RENDER_EXTERNAL_URL");
            if (externalUrl == null || externalUrl.isEmpty()) {
                externalUrl = "https://yoga-telegram-bot.onrender.com";
            }

            String botPath = System.getenv("BOT_PATH");
            if (botPath == null || botPath.isEmpty()) {
                botPath = "yoga-bot-webhook";
            }

            SetWebhook setWebhook = SetWebhook.builder()
                    .url(externalUrl + "/" + botPath)
                    .build();

            DefaultWebhook webhook = new DefaultWebhook();
            webhook.setInternalUrl("http://0.0.0.0:" + port);

            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class, webhook);
            YogaManagerBot bot = new YogaManagerBot();
            botsApi.registerBot(bot, setWebhook);

            System.out.println("✅ Webhook bot started successfully!");
            System.out.println("🌐 URL: " + externalUrl + "/" + botPath);

        } catch (TelegramApiException e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}