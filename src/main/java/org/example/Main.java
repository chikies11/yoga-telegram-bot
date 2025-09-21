package org.example;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {
    public static void main(String[] args) {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new YogaManagerBot());
            System.out.println("✅ Йога-бот успешно запущен! 🧘‍♀️");
            System.out.println("⏰ Напоминания будут отправляться каждый день в 9:00");
        } catch (TelegramApiException e) {
            System.err.println("❌ Ошибка запуска бота: " + e.getMessage());
            e.printStackTrace();
        }
    }
}