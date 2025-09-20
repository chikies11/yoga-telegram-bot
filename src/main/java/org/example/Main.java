package org.example;

import io.github.cdimascio.dotenv.Dotenv;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {
    public static void main(String[] args) {
        try {
            System.out.println("🚀 Запуск йога-бота...");

            // Загружаем переменные из .env файла
            Dotenv dotenv = Dotenv.load();
            String botToken = dotenv.get("BOT_TOKEN");
            String channelId = dotenv.get("CHANNEL_ID");

            // Проверяем, что переменные загружены
            if (botToken == null || botToken.isEmpty()) {
                System.err.println("❌ BOT_TOKEN не найден в .env файле");
                System.exit(1);
            }
            if (channelId == null || channelId.isEmpty()) {
                System.err.println("❌ CHANNEL_ID не найден в .env файле");
                System.exit(1);
            }

            System.out.println("✅ Переменные окружения загружены");
            System.out.println("🤖 Бот: " + botToken.substring(0, 10) + "...");
            System.out.println("📢 Канал: " + channelId);

            // Устанавливаем переменные окружения для бота
            System.setProperty("BOT_TOKEN", botToken);
            System.setProperty("CHANNEL_ID", channelId);

            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new YogaManagerBot());

            System.out.println("✅ Йога-бот успешно запущен! 🧘‍♀️");
            System.out.println("⏰ Напоминания будут отправляться каждый день в 9:00");

        } catch (TelegramApiException e) {
            System.err.println("❌ Ошибка запуска бота: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ Неожиданная ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }
}