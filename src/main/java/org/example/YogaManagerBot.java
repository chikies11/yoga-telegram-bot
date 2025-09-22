package org.example;

import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public class YogaManagerBot extends TelegramWebhookBot {

    public YogaManagerBot() {
        System.out.println("🎯 YogaManagerBot constructor called!");
    }

    @Override
    public String getBotUsername() {
        return "yoga_manager_bot";
    }

    @Override
    public String getBotToken() {
        // Используем реальный токен из переменных окружения
        String token = System.getenv("BOT_TOKEN");
        if (token == null) {
            token = "dummy-token";
            System.err.println("⚠️ BOT_TOKEN not found in environment");
        }
        return token;
    }

    @Override
    public String getBotPath() {
        return "yoga-bot-webhook";
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        System.out.println("🎯 onWebhookUpdateReceived CALLED! Update ID: " + update.getUpdateId());

        try {
            if (update == null) {
                System.out.println("⚠️ Update is null");
                return null;
            }

            System.out.println("📦 Update content: " + update.toString());

            if (update.hasMessage()) {
                System.out.println("💬 Has message: true");
                if (update.getMessage().hasText()) {
                    String text = update.getMessage().getText();
                    String chatId = update.getMessage().getChatId().toString();

                    System.out.println("📨 Received text: " + text);
                    System.out.println("👤 Chat ID: " + chatId);

                    // Простейший ответ
                    SendMessage response = new SendMessage();
                    response.setChatId(chatId);
                    response.setText("✅ Bot is working! Received: " + text);

                    System.out.println("✅ Response created successfully");
                    return response;
                }
            }

            System.out.println("❓ Update type not handled");
            return null;

        } catch (Exception e) {
            System.err.println("💥 ERROR in onWebhookUpdateReceived: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}