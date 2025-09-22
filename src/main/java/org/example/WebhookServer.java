package org.example;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class WebhookServer {
    public static void main(String[] args) {
        System.out.println("🚀 Starting Yoga Telegram Bot (Long Polling)...");

        try {
            // Создаем экземпляр TelegramBotsApi
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);

            // Создаем и регистрируем бота
            YogaManagerBot bot = new YogaManagerBot();
            botsApi.registerBot(bot);

            System.out.println("✅ Bot successfully registered!");
            System.out.println("🤖 Username: " + bot.getBotUsername());
            System.out.println("🔐 Token length: " + bot.getBotToken().length());
            System.out.println("⏰ Bot is now listening for messages...");

            // Бесконечный цикл чтобы приложение не завершалось
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }

        } catch (TelegramApiException e) {
            System.err.println("❌ Error initializing bot: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}