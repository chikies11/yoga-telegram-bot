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

    // Настраиваемое расписание (по умолчанию)
    private String[][] schedule = {
            {"Понедельник", "8:00-11:30 - Майсор класс", "17:00-20:30 - Майсор класс"},
            {"Вторник", "8:00-11:30 - Майсор класс", "18:30-20:00 - Практика на Аргуновском"},
            {"Среда", "8:00-11:30 - Майсор класс", "17:00-20:30 - Майсор-класс"},
            {"Четверг", "8:00-11:30 - Майсор класс", "17:00-20:30 - Майсор-класс"},
            {"Пятница", "8:00-11:30 - Майсор класс", "17:00-20:30 - Майсор-класс"},
            {"Суббота", "ОТДЫХ", ""},
            {"Воскресенье", "10:00-11:30 - LED-класс", "11:30-12:00 - Конференция (ответы на вопросы)"}
    };

    // Администраторы (добавьте свои ID чатов)
    private final List<Long> adminUsers = List.of(639619404L); // Замените на ваш ID

    // Переменные для режима редактирования
    private int editDayIndex = -1;
    private int editFieldIndex = -1;

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

                // Проверяем режим редактирования
                if (editDayIndex != -1 && isAdmin(chatId)) {
                    handleScheduleEdit(chatId, messageText);
                    return;
                }

                // Административные команды
                if (messageText.startsWith("/admin")) {
                    if (isAdmin(chatId)) {
                        handleAdminCommand(chatId, messageText);
                    } else {
                        sendMessage(chatId, "❌ У вас нет прав администратора");
                    }
                    return;
                }

                // Основные команды для всех пользователей
                switch (messageText) {
                    case "/start":
                        sendWelcomeMessage(chatId);
                        break;
                    case "📅 Расписание":
                        sendFullSchedule(chatId);
                        break;
                    case "📋 Сегодня":
                        sendTodaySchedule(chatId);
                        break;
                    case "📆 Завтра":
                        sendTomorrowSchedule(chatId);
                        break;
                    case "⚙️ Управление":
                        if (isAdmin(chatId)) {
                            showAdminPanel(chatId);
                        } else {
                            sendMessage(chatId, "❌ У вас нет прав администратора");
                        }
                        break;
                    case "🔄 Сбросить расписание":
                        if (isAdmin(chatId)) {
                            resetToDefaultSchedule(chatId);
                        } else {
                            sendMessage(chatId, "❌ У вас нет прав администратора");
                        }
                        break;
                    case "❌ Отмена":
                        cancelEditMode(chatId);
                        break;
                    case "ℹ️ О боте":
                        sendAboutMessage(chatId);
                        break;
                    default:
                        if (isAdmin(chatId) && messageText.matches("\\d+")) {
                            handleDaySelection(chatId, messageText);
                        } else {
                            sendMessageWithKeyboard(chatId, "Выберите действие из меню ниже:");
                        }
                }

                System.out.println("✅ Response sent successfully");
            }
        } catch (Exception e) {
            System.err.println("❌ Error processing message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean isAdmin(long chatId) {
        return adminUsers.contains(chatId);
    }

    private void handleAdminCommand(long chatId, String command) throws TelegramApiException {
        if (command.equals("/admin")) {
            showAdminPanel(chatId);
        } else if (command.equals("/admin_edit")) {
            showScheduleEditor(chatId);
        } else if (command.equals("/admin_reset")) {
            resetToDefaultSchedule(chatId);
        } else {
            sendMessage(chatId, "Доступные административные команды:\n" +
                    "/admin - панель управления\n" +
                    "/admin_edit - редактировать расписание\n" +
                    "/admin_reset - сбросить к стандартному");
        }
    }

    private void showAdminPanel(long chatId) throws TelegramApiException {
        String text = "⚙️ Панель администратора\n\n" +
                "Доступные действия:\n" +
                "• 📝 Редактировать расписание\n" +
                "• 🔄 Сбросить к стандартному\n" +
                "• 📊 Просмотреть текущее\n\n" +
                "Используйте меню ниже или команды:\n" +
                "/admin_edit - редактировать\n" +
                "/admin_reset - сбросить";

        sendMessageWithAdminKeyboard(chatId, text);
    }

    private void showScheduleEditor(long chatId) throws TelegramApiException {
        StringBuilder sb = new StringBuilder();
        sb.append("📝 Редактор расписания\n\n");
        sb.append("Выберите день для редактирования:\n\n");

        for (int i = 0; i < schedule.length; i++) {
            sb.append(i + 1).append(". ").append(schedule[i][0]).append(":\n");
            if (!schedule[i][1].isEmpty()) sb.append("   🕘 ").append(schedule[i][1]).append("\n");
            if (!schedule[i][2].isEmpty()) sb.append("   🕘 ").append(schedule[i][2]).append("\n");
            sb.append("\n");
        }

        sb.append("Отправьте номер дня (1-7) или выберите действие из меню:");

        sendMessageWithAdminKeyboard(chatId, sb.toString());
    }

    private void handleDaySelection(long chatId, String dayNumber) throws TelegramApiException {
        try {
            int dayIndex = Integer.parseInt(dayNumber) - 1;
            if (dayIndex >= 0 && dayIndex < schedule.length) {
                showDayEditOptions(chatId, dayIndex);
            } else {
                sendMessage(chatId, "❌ Неверный номер дня. Введите число от 1 до 7");
            }
        } catch (NumberFormatException e) {
            sendMessage(chatId, "❌ Пожалуйста, введите число от 1 до 7");
        }
    }

    private void showDayEditOptions(long chatId, int dayIndex) throws TelegramApiException {
        editDayIndex = dayIndex;

        StringBuilder sb = new StringBuilder();
        sb.append("✏️ Редактирование: ").append(schedule[dayIndex][0]).append("\n\n");
        sb.append("Текущее расписание:\n");

        if (!schedule[dayIndex][1].isEmpty()) {
            sb.append("1. 🕘 ").append(schedule[dayIndex][1]).append("\n");
        } else {
            sb.append("1. [пусто]\n");
        }

        if (!schedule[dayIndex][2].isEmpty()) {
            sb.append("2. 🕘 ").append(schedule[dayIndex][2]).append("\n");
        } else {
            sb.append("2. [пусто]\n");
        }

        sb.append("\nВыберите что редактировать:\n");
        sb.append("• Отправьте '1' для первого занятия\n");
        sb.append("• Отправьте '2' для второго занятия\n");
        sb.append("• Отправьте '0' для отметки как 'ОТДЫХ'\n");
        sb.append("• Используйте меню для отмены");

        sendMessageWithCancelKeyboard(chatId, sb.toString());
    }

    private void handleScheduleEdit(long chatId, String messageText) throws TelegramApiException {
        if (messageText.equals("1") || messageText.equals("2")) {
            editFieldIndex = Integer.parseInt(messageText) - 1;
            sendMessageWithCancelKeyboard(chatId, "📝 Введите новое описание для занятия " + messageText +
                    ":\n(например: '8:00-11:30 - Майсор класс')\n\nТекущее: " +
                    (schedule[editDayIndex][editFieldIndex].isEmpty() ? "[пусто]" : schedule[editDayIndex][editFieldIndex]));
        }
        else if (messageText.equals("0")) {
            // Отметка как отдых
            schedule[editDayIndex][0] = getDayName(editDayIndex); // Восстанавливаем название дня
            schedule[editDayIndex][1] = "ОТДЫХ";
            schedule[editDayIndex][2] = "";

            sendMessage(chatId, "✅ " + schedule[editDayIndex][0] + " отмечен как день отдыха!");
            resetEditMode();
            showScheduleEditor(chatId);
        }
        else if (editFieldIndex != -1) {
            // Сохраняем новое описание
            schedule[editDayIndex][editFieldIndex] = messageText;

            sendMessage(chatId, "✅ Занятие " + (editFieldIndex + 1) + " для " +
                    schedule[editDayIndex][0] + " обновлено!");
            resetEditMode();
            showScheduleEditor(chatId);
        }
        else {
            sendMessage(chatId, "❌ Неверный выбор. Используйте 1, 2, 0 или отмените редактирование");
        }
    }

    private void cancelEditMode(long chatId) throws TelegramApiException {
        resetEditMode();
        sendMessage(chatId, "❌ Редактирование отменено");
        showAdminPanel(chatId);
    }

    private void resetEditMode() {
        editDayIndex = -1;
        editFieldIndex = -1;
    }

    private void resetToDefaultSchedule(long chatId) throws TelegramApiException {
        schedule = new String[][]{
                {"Понедельник", "8:00-11:30 - Майсор класс", "17:00-20:30 - Майсор класс"},
                {"Вторник", "8:00-11:30 - Майсор класс", "18:30-20:00 - Практика на Аргуновском"},
                {"Среда", "8:00-11:30 - Майсор класс", "17:00-20:30 - Майсор-класс"},
                {"Четверг", "8:00-11:30 - Майсор класс", "17:00-20:30 - Майсор-класс"},
                {"Пятница", "8:00-11:30 - Майсор класс", "17:00-20:30 - Майсор-класс"},
                {"Суббота", "ОТДЫХ", ""},
                {"Воскресенье", "10:00-11:30 - LED-класс", "11:30-12:00 - Конференция (ответы на вопросы)"}
        };

        sendMessage(chatId, "✅ Расписание сброшено к стандартному!");
        showScheduleEditor(chatId);
    }

    private String getDayName(int dayIndex) {
        String[] dayNames = {"Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье"};
        return dayNames[dayIndex];
    }

    private void sendWelcomeMessage(long chatId) throws TelegramApiException {
        String text = "Привет! Я твой помощник в планировании йога-занятий! 🧘‍♀️\n\n" +
                "С помощью меня ты можешь:\n" +
                "• 📅 Посмотреть расписание занятий\n" +
                "• 📋 Узнать о занятиях сегодня и завтра\n" +
                "• ⚙️ Управлять расписанием (для администраторов)\n\n" +
                "Выбери действие из меню ниже:";
        sendMessageWithKeyboard(chatId, text);
    }

    private void sendAboutMessage(long chatId) throws TelegramApiException {
        String text = "ℹ️ О боте\n\n" +
                "Йога-бот v2.0\n" +
                "Функции:\n" +
                "• Просмотр расписания занятий\n" +
                "• Автоматические напоминания\n" +
                "• Настройка расписания (администраторы)\n\n" +
                "Разработано для удобного планирования йога-практики.";
        sendMessage(chatId, text);
    }

    private void sendFullSchedule(long chatId) throws TelegramApiException {
        StringBuilder sb = new StringBuilder();
        sb.append("📅 Текущее расписание занятий:\n\n");

        for (String[] day : schedule) {
            sb.append("**").append(day[0]).append(":**\n");
            if (!day[1].isEmpty() && !day[1].equals("ОТДЫХ")) {
                sb.append("• 🕘 ").append(day[1]).append("\n");
            }
            if (!day[2].isEmpty() && !day[2].equals("ОТДЫХ")) {
                sb.append("• 🕘 ").append(day[2]).append("\n");
            }
            if (day[1].equals("ОТДЫХ")) {
                sb.append("• 🎉 ОТДЫХ\n");
            }
            sb.append("\n");
        }

        sendMessage(chatId, sb.toString());
    }

    private void sendTodaySchedule(long chatId) throws TelegramApiException {
        int todayIndex = getDayOfWeekIndex(Calendar.getInstance());
        String scheduleText = formatDaySchedule(todayIndex);
        String message = "📋 Сегодня (" + schedule[todayIndex][0] + "):\n\n" + scheduleText;
        sendMessage(chatId, message);
    }

    private void sendTomorrowSchedule(long chatId) throws TelegramApiException {
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);
        int tomorrowIndex = getDayOfWeekIndex(tomorrow);
        String scheduleText = formatDaySchedule(tomorrowIndex);
        String message = "📆 Завтра (" + schedule[tomorrowIndex][0] + "):\n\n" + scheduleText;
        sendMessage(chatId, message);
    }

    private String formatDaySchedule(int dayIndex) {
        String[] day = schedule[dayIndex];
        StringBuilder sb = new StringBuilder();

        if (!day[1].isEmpty() && !day[1].equals("ОТДЫХ")) {
            sb.append("• 🕘 ").append(day[1]).append("\n");
        }
        if (!day[2].isEmpty() && !day[2].equals("ОТДЫХ")) {
            sb.append("• 🕘 ").append(day[2]).append("\n");
        }
        if (day[1].equals("ОТДЫХ")) {
            sb.append("• 🎉 ОТДЫХ - наслаждайтесь свободным днем!\n");
        }

        return sb.toString();
    }

    private int getDayOfWeekIndex(Calendar calendar) {
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        return (dayOfWeek + 5) % 7; // Воскресенье = 0, Понедельник = 1, etc.
    }

    private void sendMessage(long chatId, String text) throws TelegramApiException {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        message.setParseMode("Markdown");
        execute(message);
    }

    private void sendMessageWithKeyboard(long chatId, String text) throws TelegramApiException {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        message.setReplyMarkup(createMainKeyboard());
        message.setParseMode("Markdown");
        execute(message);
    }

    private void sendMessageWithAdminKeyboard(long chatId, String text) throws TelegramApiException {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        message.setReplyMarkup(createAdminKeyboard());
        message.setParseMode("Markdown");
        execute(message);
    }

    private void sendMessageWithCancelKeyboard(long chatId, String text) throws TelegramApiException {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        message.setReplyMarkup(createCancelKeyboard());
        message.setParseMode("Markdown");
        execute(message);
    }

    private ReplyKeyboardMarkup createMainKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add("📅 Расписание");
        row1.add("📋 Сегодня");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("📆 Завтра");
        row2.add("⚙️ Управление");

        KeyboardRow row3 = new KeyboardRow();
        row3.add("ℹ️ О боте");

        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);

        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        return keyboardMarkup;
    }

    private ReplyKeyboardMarkup createAdminKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add("📝 Редактировать");
        row1.add("🔄 Сбросить");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("📅 Расписание");
        row2.add("📋 Сегодня");

        KeyboardRow row3 = new KeyboardRow();
        row3.add("📆 Завтра");
        row3.add("❌ Отмена");

        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);

        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        return keyboardMarkup;
    }

    private ReplyKeyboardMarkup createCancelKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add("❌ Отмена");

        keyboard.add(row1);

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