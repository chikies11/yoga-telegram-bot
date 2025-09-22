package org.example;

import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class YogaManagerBot extends TelegramWebhookBot {

    @Override
    public String getBotUsername() {
        return "yoga_manager_bot";
    }

    @Override
    public String getBotToken() {
        String token = System.getenv("BOT_TOKEN");
        if (token == null) token = "dummy-token";
        return token;
    }

    @Override
    public String getBotPath() {
        return "yoga-bot-webhook";
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        System.out.println("🎯 Webhook update received!");

        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                String text = update.getMessage().getText();
                long chatId = update.getMessage().getChatId();

                System.out.println("📨 Message: " + text + " from: " + chatId);

                SendMessage response = new SendMessage();
                response.setChatId(String.valueOf(chatId));
                response.setText("✅ Webhook works! You said: " + text);

                return response;
            }
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }
}