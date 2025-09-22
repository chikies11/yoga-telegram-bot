package org.example;

import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public class YogaManagerBot extends TelegramWebhookBot {

    @Override
    public String getBotUsername() {
        return "yoga_manager_bot";
    }

    @Override
    public String getBotToken() {
        // Просто возвращаем строку
        return "dummy-bot-token-for-testing";
    }

    @Override
    public String getBotPath() {
        return "yoga-bot-webhook";
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        System.out.println("✅ Webhook method called successfully!");

        // Простейший ответ
        if (update.hasMessage() && update.getMessage().hasText()) {
            SendMessage response = new SendMessage();
            response.setChatId(update.getMessage().getChatId().toString());
            response.setText("✅ Bot is working! Test message received.");
            return response;
        }

        return null;
    }
}