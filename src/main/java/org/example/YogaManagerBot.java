package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

public class YogaManagerBot extends TelegramLongPollingBot {

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
    public void onUpdateReceived(Update update) {
        System.out.println("🎯 Update received in LONG POLLING mode!");

        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            String chatId = update.getMessage().getChatId().toString();

            System.out.println("📨 Message: " + text + " from: " + chatId);

            SendMessage response = new SendMessage();
            response.setChatId(chatId);
            response.setText("✅ Long polling works! You said: " + text);

            try {
                execute(response);
                System.out.println("✅ Response sent!");
            } catch (TelegramApiException e) {
                System.err.println("❌ Error sending response: " + e.getMessage());
            }
        }
    }

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        for (Update update : updates) {
            onUpdateReceived(update);
        }
    }
}