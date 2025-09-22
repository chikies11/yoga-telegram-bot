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
        String token = System.getenv("BOT_TOKEN");
        if (token == null) {
            token = "dummy-token-for-testing";
            System.err.println("⚠️ BOT_TOKEN not found in environment");
        }
        System.out.println("🔑 Using token length: " + token.length());
        return token;
    }

    @Override
    public String getBotPath() {
        return "yoga-bot-webhook";
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        System.out.println("🎯 onWebhookUpdateReceived CALLED!");

        try {
            if (update == null) {
                System.out.println("⚠️ Update is null");
                return null;
            }

            System.out.println("📦 Update ID: " + update.getUpdateId());

            if (update.hasMessage() && update.getMessage().hasText()) {
                String text = update.getMessage().getText();
                String chatId = update.getMessage().getChatId().toString();

                System.out.println("📨 Received: " + text);
                System.out.println("👤 Chat ID: " + chatId);

                SendMessage response = new SendMessage();
                response.setChatId(chatId);
                response.setText("✅ Bot works! You said: " + text);

                System.out.println("✅ Response created");
                return response;
            }

            return null;

        } catch (Exception e) {
            System.err.println("💥 ERROR: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}