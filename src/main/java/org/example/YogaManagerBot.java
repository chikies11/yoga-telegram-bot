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
import java.util.Timer;
import java.util.TimerTask;

public class YogaManagerBot extends TelegramLongPollingBot {

    private final String CHANNEL_ID;
    private final String BOT_TOKEN;
    private Timer reminderTimer;

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

    public YogaManagerBot() {
        // Получаем переменные окружения
        this.BOT_TOKEN = System.getenv("BOT_TOKEN");
        this.CHANNEL_ID = System.getenv("CHANNEL_ID");

        // Проверяем, что переменные установлены
        if (BOT_TOKEN == null || BOT_TOKEN.isEmpty()) {
            throw new IllegalStateException("❌ BOT_TOKEN не установлен! Проверьте переменные окружения.");
        }
        if (CHANNEL_ID == null || CHANNEL_ID.isEmpty()) {
            throw new IllegalStateException("❌ CHANNEL_ID не установлен! Проверьте переменные окружения.");
        }

        System.out.println("✅ Бот инициализирован");
        System.out.println("📢 Канал: " + CHANNEL_ID);

        startReminderScheduler();
    }

    @Override
    public String getBotUsername() {
        return "yoga_manager_bot";
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                String messageText = update.getMessage().getText();
                long chatId = update.getMessage().getChatId();

                System.out.println("📨 Получено сообщение: " + messageText + " от " + chatId);

                switch (messageText) {
                    case "/start":
                        sendWelcomeMessage(chatId);
                        break;
                    case "Reminder":
                        sendReminderInfo(chatId);
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
                        sendMessageWithKeyboard(chatId, "Выберите действие:", createMainKeyboard());
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Ошибка обработки сообщения: " + e.getMessage());
        }
    }

    private void sendWelcomeMessage(long chatId) {
        String welcomeText = "Привет, я твой помощник в планировании йога-занятий! 🧘‍♀️\n\nС чем тебе помочь?";
        sendMessageWithKeyboard(chatId, welcomeText, createMainKeyboard());
        System.out.println("✅ Отправлено приветствие для " + chatId);
    }

    private void sendReminderInfo(long chatId) {
        String info = "✅ Напоминания настроены!\n\n" +
                "Я буду автоматически отправлять напоминания в канал за 24 часа до занятий.\n" +
                "Следующее напоминание: " + getNextReminderTime();
        sendMessageWithKeyboard(chatId, info, createMainKeyboard());
    }

    private void sendFullSchedule(long chatId) {
        sendMessageWithKeyboard(chatId, "📅 Полное расписание занятий:\n\n" + getFullScheduleText(), createMainKeyboard());
    }

    private void sendTodaySchedule(long chatId) {
        int todayIndex = getDayOfWeekIndex(Calendar.getInstance());
        String schedule = getDaySchedule(todayIndex);
        sendMessageWithKeyboard(chatId, "📋 Сегодня (" + SCHEDULE[todayIndex][0] + "):\n\n" + schedule, createMainKeyboard());
    }

    private void sendTomorrowSchedule(long chatId) {
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);
        int tomorrowIndex = getDayOfWeekIndex(tomorrow);
        String schedule = getDaySchedule(tomorrowIndex);
        sendMessageWithKeyboard(chatId, "📋 Завтра (" + SCHEDULE[tomorrowIndex][0] + "):\n\n" + schedule, createMainKeyboard());
    }

    private void sendTestReminder(long chatId) {
        String tomorrowSchedule = getTomorrowSchedule();
        String testReminder;

        if (tomorrowSchedule.contains("Официально!!! Отдых")) {
            testReminder = "🔔 ТЕСТ: Завтра отдых!\n\n" +
                    tomorrowSchedule +
                    "\n\nНаслаждайтесь свободным днём! 🌈";
        } else {
            testReminder = "🔔 ТЕСТ: Напоминание о завтрашних занятиях!\n\n" +
                    tomorrowSchedule +
                    "\n\nНе забудьте записаться! 🧘‍♀️";
        }

        sendMessageToChannel(testReminder);
        sendMessageWithKeyboard(chatId, "✅ Тестовое напоминание отправлено в канал!", createMainKeyboard());
    }

    private ReplyKeyboardMarkup createMainKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add("Reminder");
        row1.add("Расписание");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("Сегодня");
        row2.add("Завтра");

        KeyboardRow row3 = new KeyboardRow();
        row3.add("Тест напоминания");

        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);

        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        return keyboardMarkup;
    }

    private String getFullScheduleText() {
        StringBuilder sb = new StringBuilder();
        for (String[] day : SCHEDULE) {
            sb.append(day[0]).append(":\n");
            if (!day[1].isEmpty()) {
                sb.append("• ").append(day[1]).append("\n");
            }
            if (!day[2].isEmpty()) {
                sb.append("• ").append(day[2]).append("\n");
            }
            if (day[1].isEmpty() && day[2].isEmpty()) {
                sb.append("• Нет занятий\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private String getDaySchedule(int dayIndex) {
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

    private void startReminderScheduler() {
        reminderTimer = new Timer();

        Calendar time = Calendar.getInstance();
        time.set(Calendar.HOUR_OF_DAY, 9);
        time.set(Calendar.MINUTE, 0);
        time.set(Calendar.SECOND, 0);

        if (time.before(Calendar.getInstance())) {
            time.add(Calendar.DAY_OF_MONTH, 1);
        }

        System.out.println("⏰ Напоминания запланированы на: " + time.getTime());

        reminderTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    checkAndSendReminders();
                } catch (Exception e) {
                    System.err.println("❌ Ошибка в планировщике: " + e.getMessage());
                }
            }
        }, time.getTime(), 24 * 60 * 60 * 1000);
    }

    private String getNextReminderTime() {
        Calendar now = Calendar.getInstance();
        Calendar next = Calendar.getInstance();
        next.set(Calendar.HOUR_OF_DAY, 9);
        next.set(Calendar.MINUTE, 0);
        next.set(Calendar.SECOND, 0);

        if (next.before(now)) {
            next.add(Calendar.DAY_OF_MONTH, 1);
        }

        return String.format("%02d:%02d", next.get(Calendar.HOUR_OF_DAY), next.get(Calendar.MINUTE));
    }

    private void checkAndSendReminders() {
        try {
            String tomorrowSchedule = getTomorrowSchedule();

            if (tomorrowSchedule.contains("Официально!!! Отдых")) {
                String restMessage = "🎉 Завтрашний день объявляется днём отдыха!\n\n" +
                        "Официально!!! Отдых - друг человека! 🎉\n" +
                        "Завтра занятий нет - наслаждайтесь свободным временем! 😊";

                sendMessageToChannel(restMessage);
                System.out.println("✅ Отправлено напоминание об отдыхе");
            } else if (!tomorrowSchedule.contains("Нет занятий")) {
                String reminder = "🔔 Напоминание о завтрашних занятиях!\n\n" +
                        tomorrowSchedule +
                        "\n\nНе забудьте записаться! 🧘‍♀️";

                sendMessageToChannel(reminder);
                System.out.println("✅ Отправлено напоминание о занятиях");
            } else {
                System.out.println("ℹ️ Завтра нет занятий, напоминание не отправляется");
            }
        } catch (Exception e) {
            System.err.println("❌ Ошибка отправки напоминания: " + e.getMessage());
        }
    }

    private String getTomorrowSchedule() {
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);
        int tomorrowIndex = getDayOfWeekIndex(tomorrow);
        String[] day = SCHEDULE[tomorrowIndex];

        StringBuilder sb = new StringBuilder();
        sb.append("Завтра (").append(day[0]).append("):\n\n");

        boolean hasClasses = false;

        if (!day[1].isEmpty() && !day[1].equals("Нет занятий")) {
            sb.append("• ").append(day[1]).append("\n");
            hasClasses = true;
        }
        if (!day[2].isEmpty() && !day[2].equals("Нет занятий")) {
            sb.append("• ").append(day[2]).append("\n");
            hasClasses = true;
        }

        if (!hasClasses) {
            sb.append("🎉 Официально!!! Отдых - друг человека! 🎉\n");
            sb.append("Завтра занятий нет, можно отдохнуть! 😴");
        }

        return sb.toString();
    }

    private void sendMessageToChannel(String text) {
        try {
            SendMessage message = new SendMessage();
            message.setChatId(CHANNEL_ID);
            message.setText(text);

            execute(message);
            System.out.println("✅ Сообщение отправлено в канал: " + text.substring(0, Math.min(50, text.length())) + "...");
        } catch (TelegramApiException e) {
            System.err.println("❌ Ошибка отправки в канал: " + e.getMessage());
        }
    }

    private void sendMessageWithKeyboard(long chatId, String text, ReplyKeyboardMarkup keyboard) {
        try {
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));
            message.setText(text);
            message.setReplyMarkup(keyboard);

            execute(message);
        } catch (TelegramApiException e) {
            System.err.println("❌ Ошибка отправки сообщения: " + e.getMessage());
        }
    }
}