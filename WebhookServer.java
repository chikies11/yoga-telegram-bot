package org.example;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.telegram.telegrambots.updatesreceivers.DefaultWebhook;

import static spark.Spark.*;

public class WebhookServer {
    public static void main(String[] args) {
        try {
            // Получаем порт из переменных окружения (Render автоматически устанавливает PORT)
            String port = System.getenv("PORT");
            if (port == null || port.isEmpty()) {
                port = "8080";
            }

            // Получаем внешний URL (Render предоставляет RENDER_EXTERNAL_URL)
            String externalUrl = System.getenv("RENDER_EXTERNAL_URL");
            if (externalUrl == null || externalUrl.isEmpty()) {
                externalUrl = "https://your-app-name.onrender.com";
            }

            // Получаем путь для вебхука
            String botPath = System.getenv("BOT_PATH");
            if (botPath == null || botPath.isEmpty()) {
                botPath = "yoga-bot";
            }

            // Настраиваем вебхук
            DefaultWebhook webhook = new DefaultWebhook();
            webhook.setInternalUrl("http://0.0.0.0:" + port + "/");
            webhook.setExternalUrl(externalUrl + "/");

            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class, webhook);

            // Регистрируем бота
            YogaManagerBot bot = new YogaManagerBot();
            botsApi.registerBot(bot, botPath);

            System.out.println("✅ Йога-бот успешно запущен на порту " + port);
            System.out.println("🌐 Webhook URL: " + externalUrl + "/" + botPath);
            System.out.println("⏰ Напоминания будут отправляться каждый день в 9:00");

        } catch (TelegramApiException e) {
            System.err.println("❌ Ошибка запуска бота: " + e.getMessage());
            e.printStackTrace();
        }
    }
}