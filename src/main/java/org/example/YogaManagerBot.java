package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class YogaManagerBot extends TelegramLongPollingBot {

    // Расписание занятий
    private static final String[][] SCHEDULE = {
            {"Понедельник", "8:00-11:30 - Майсор класс", "17:00-20:30 - Майсор класс"},
            {"Вторник", "8:00-11:30 - Майсор класс", "18:30-20:00 - LED-класс"},
            {"Среда", "8:00-11:30 - Майсор класс", "18:30-20:00 - LED-класс"},
            {"Четверг", "8:00-11:30 - Майсор класс", "17:00-20:30 - LED-класс"},
            {"Пятница", "8:00-11:30 - Майсор класс", "17:00-20:30 - LED-класс"},
            {"Суббота", "Нет занятий", ""},
            {"Воскресенье", "10:00-11:30 - LED-класс", ""}
    };

    @Override
    public String getBotUsername() {
        return "yoga_manager_bot";
    }

    @Override
    public String getBotToken() {
        String token = System.getenv("BOT_TOKEN");
        if (token == null || token.isEmpty()) {
            token = "dummy-token-for-testing";
            System.err.println("⚠️ BOT_TOKEN not set, using dummy token");
        }
        return token;
    }

    @Override
    public void onUpdateReceived(Update update) {
        System.out.println("🎯 Update received!");

        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                String messageText = update.getMessage().getText();
                long chatId = update.getMessage().getChatId();
                String userName = update.getMessage().getFrom().getUserName();

                System.out.println("📨 Message: '" + messageText + "' from: @" + userName + " (" + chatId + ")");

                switch (messageText) {
                    case "/start":
                        sendWelcomeMessage(chatId);
                        break;
                    case "Расписание":
                        sendFullSchedule(chatId);
                        break;
                    case "Сегодня":
                        sendTodaySchedule(chatId);
                        break;
                    case "Завтра":
                        sendTomorrowSchedule(chatId);
                        break;
                    case "Тест напоминания":
                        sendTestReminder(chatId);
                        break;
                    default:
                        sendMessageWithKeyboard(chatId, "Выберите действие из меню ниже:");
                }

                System.out.println("✅ Response sent successfully");
            }
        } catch (Exception e) {
            System.err.println("❌ Error processing message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void sendWelcomeMessage(long chatId) throws TelegramApiException {
        String text = "Привет! Я твой помощник в планировании йога-занятий! 🧘‍♀️\n\n" +
                "С помощью меня ты можешь:\n" +
                "• 📅 Посмотреть расписание занятий\n" +
                "• 🔔 Получить информацию о напоминаниях\n" +
                "• 📋 Узнать о занятиях сегодня и завтра\n\n" +
                "Выбери действие из меню ниже:";
        sendMessageWithKeyboard(chatId, text);
    }

    private void sendFullSchedule(long chatId) throws TelegramApiException {
        StringBuilder sb = new StringBuilder();
        sb.append("📅 Полное расписание занятий:\n\n");

        for (String[] day : SCHEDULE) {
            sb.append(day[0]).append(":\n");
            if (!day[1].isEmpty()) sb.append("• ").append(day[1]).append("\n");
            if (!day[2].isEmpty()) sb.append("• ").append(day[2]).append("\n");
            if (day[1].isEmpty() && day[2].isEmpty()) sb.append("• Нет занятий\n");
            sb.append("\n");
        }

        sendMessage(chatId, sb.toString());
    }

    private void sendTodaySchedule(long chatId) throws TelegramApiException {
        int todayIndex = getDayOfWeekIndex(Calendar.getInstance());
        String schedule = formatDaySchedule(todayIndex);
        String message = "📋 Сегодня (" + SCHEDULE[todayIndex][0] + "):\n\n" + schedule;
        sendMessage(chatId, message);
    }

    private void sendTomorrowSchedule(long chatId) throws TelegramApiException {
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);
        int tomorrowIndex = getDayOfWeekIndex(tomorrow);
        String schedule = formatDaySchedule(tomorrowIndex);
        String message = "📋 Завтра (" + SCHEDULE[tomorrowIndex][0] + "):\n\n" + schedule;
        sendMessage(chatId, message);
    }

    private void sendTestReminder(long chatId) throws TelegramApiException {
        sendMessage(chatId, "✅ Тестовое напоминание! Напоминания настроены и будут отправляться автоматически.");
    }

    private String formatDaySchedule(int dayIndex) {
        String[] day = SCHEDULE[dayIndex];
        StringBuilder sb = new StringBuilder();

        if (!day[1].isEmpty() && !day[1].equals("Нет занятий")) {
            sb.append("• ").append(day[1]).append("\n");
        }
        if (!day[2].isEmpty() && !day[2].equals("Нет занятий")) {
            sb.append("• ").append(day[2]).append("\n");
        }
        if (sb.length() == 0) {
            sb.append("• Нет занятий\n");
        }

        return sb.toString();
    }

    private int getDayOfWeekIndex(Calendar calendar) {
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        return (dayOfWeek + 5) % 7;
    }

    private void sendMessage(long chatId, String text) throws TelegramApiException {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        execute(message);
    }

    private void sendMessageWithKeyboard(long chatId, String text) throws TelegramApiException {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        message.setReplyMarkup(createMainKeyboard());
        execute(message);
    }

    private ReplyKeyboardMarkup createMainKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add("Расписание");
        row1.add("Сегодня");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("Завтра");
        row2.add("Тест напоминания");

        keyboard.add(row1);
        keyboard.add(row2);

        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        return keyboardMarkup;
    }

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        for (Update update : updates) {
            onUpdateReceived(update);
        }
    }
}