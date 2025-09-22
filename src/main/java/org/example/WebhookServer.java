package org.example;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.telegram.telegrambots.updatesreceivers.DefaultWebhook;

public class WebhookServer {
    public static void main(String[] args) {
        System.out.println("=== STARTING BOT ===");
        System.out.println("Java version: " + System.getProperty("java.version"));

        try {
            // Проверим переменные окружения
            System.out.println("BOT_TOKEN exists: " + (System.getenv("BOT_TOKEN") != null));
            System.out.println("CHANNEL_ID exists: " + (System.getenv("CHANNEL_ID") != null));
            System.out.println("BOT_PATH: " + System.getenv("BOT_PATH"));
            System.out.println("PORT: " + System.getenv("PORT"));

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

                System.out.println("🚀 Starting Yoga Telegram Bot...");
                System.out.println("📍 Port: " + port);
                System.out.println("🌐 External URL: " + externalUrl);
                System.out.println("🛣️ Bot path: " + botPath);

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

            System.out.println("🎯 Bot registration completed!");

// Тест: создаем искусственный update для проверки
            try {
                System.out.println("🧪 Testing bot with mock update...");

                // Создаем тестовый update
                Update testUpdate = new Update();
                Message testMessage = new Message();
                testMessage.setText("/test");
                testMessage.setChat(new Chat(123456789L, "private"));
                testMessage.setFrom(new User(123456789L, "TestUser", false));
                testUpdate.setMessage(testMessage);

                // Пытаемся обработать
                BotApiMethod<?> result = bot.onWebhookUpdateReceived(testUpdate);
                System.out.println("✅ Test update processed successfully");

            } catch (Exception e) {
                System.err.println("💥 TEST FAILED: " + e.getMessage());
                e.printStackTrace();
            }// Регистрируем бота с объектом SetWebhook

                YogaManagerBot bot = new YogaManagerBot();
                botsApi.registerBot(bot, setWebhook);

                System.out.println("✅ Йога-бот успешно запущен на порту " + port);
                System.out.println("🌐 Webhook URL: " + externalUrl + "/" + botPath);
                System.out.println("⏰ Напоминания будут отправляться каждый день в 9:00");
                System.out.println("🚀 Бот готов к работе!");

            } catch (Exception e) {
                System.err.println("❌ Фатальная ошибка запуска бота: " + e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }

        }
    }