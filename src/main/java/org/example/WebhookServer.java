package org.example;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class WebhookServer {
    public static void main(String[] args) {
        System.out.println("🚀 Starting Yoga Bot in LONG POLLING mode...");

        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            YogaManagerBot bot = new YogaManagerBot();
            botsApi.registerBot(bot);

            System.out.println("✅ Bot started in LONG POLLING mode!");
            System.out.println("🤖 Bot username: " + bot.getBotUsername());
            System.out.println("🔑 Token length: " + bot.getBotToken().length());

        } catch (TelegramApiException e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}