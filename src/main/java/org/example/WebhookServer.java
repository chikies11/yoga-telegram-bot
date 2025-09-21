package org.example;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.telegram.telegrambots.updatesreceivers.DefaultWebhook;

public class WebhookServer {
    public static void main(String[] args) {
        try {
            // Получаем порт из переменных окружения
            String port = System.getenv("PORT");
            if (port == null || port.isEmpty()) {
                port = "8080";
            }

            // Получаем внешний URL
            String externalUrl = System.getenv("RENDER_EXTERNAL_URL");
            if (externalUrl == null || externalUrl.isEmpty()) {
                externalUrl = "https://yoga-telegram-bot.onrender.com";
            }

            // Получаем путь для вебхука
            String botPath = System.getenv("BOT_PATH");
            if (botPath == null || botPath.isEmpty()) {
                botPath = "yoga-bot-webhook";
            }

            // Создаем объект SetWebhook с внешним URL
            SetWebhook setWebhook = SetWebhook.builder()
                    .url(externalUrl + "/" + botPath)
                    .build();

            // Настраиваем вебхук
            DefaultWebhook webhook = new DefaultWebhook();
            webhook.setInternalUrl("http://0.0.0.0:" + port);

            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class, webhook);

            // Регистрируем бота с объектом SetWebhook
            YogaManagerBot bot = new YogaManagerBot();
            botsApi.registerBot(bot, setWebhook);

            System.out.println("✅ Йога-бот успешно запущен на порту " + port);
            System.out.println("🌐 Webhook URL: " + externalUrl + "/" + botPath);
            System.out.println("⏰ Напоминания будут отправляться каждый день в 9:00");
            System.out.println("🚀 Бот готов к работе!");

        } catch (TelegramApiException e) {
            System.err.println("❌ Ошибка запуска бота: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}