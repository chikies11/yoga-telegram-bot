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

    // Основное расписание
    private String[][] schedule = {
            {"Понедельник", "8:00-11:30 - Майсор класс", "17:00-20:30 - Майсор класс"},
            {"Вторник", "8:00-11:30 - Майсор класс", "18:30-20:00 - Практика на Аргуновском"},
            {"Среда", "8:00-11:30 - Майсор класс", "17:00-20:30 - Майсор-класс"},
            {"Четверг", "8:00-11:30 - Майсор класс", "17:00-20:30 - Майсор-класс"},
            {"Пятница", "8:00-11:30 - Майсор класс", "17:00-20:30 - Майсор-класс"},
            {"Суббота", "ОТДЫХ", ""},
            {"Воскресенье", "10:00-11:30 - LED-класс", "11:30-12:00 - Конференция (ответы на вопросы)"}
    };

    // Временные изменения
    private String[] todaySpecial = null;
    private String[] tomorrowSpecial = null;

    // Администраторы
    private final List<Long> adminUsers = List.of(639619404L);

    // Режимы редактирования
    private String editMode = null;
    private int currentEditDay = -1;

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

                System.out.println("📨 Message: '" + messageText + "' from: " + chatId);

                // Обработка режимов редактирования
                if (editMode != null && isAdmin(chatId)) {
                    handleEditInput(chatId, messageText);
                    return;
                }

                // Обработка основных команд
                handleMainCommands(chatId, messageText);
            }
        } catch (Exception e) {
            System.err.println("❌ Error processing message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleMainCommands(long chatId, String messageText) throws TelegramApiException {
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
                    showManagementPanel(chatId);
                } else {
                    sendMessage(chatId, "❌ У вас нет прав администратора");
                    sendWelcomeMessage(chatId);
                }
                break;
            case "✏️ Редактировать расписание":
                if (isAdmin(chatId)) {
                    showScheduleEditor(chatId);
                }
                break;
            case "🕘 Изменить сегодня":
                if (isAdmin(chatId)) {
                    startEditToday(chatId);
                }
                break;
            case "🕘 Изменить завтра":
                if (isAdmin(chatId)) {
                    startEditTomorrow(chatId);
                }
                break;
            case "🔄 Сбросить изменения":
                if (isAdmin(chatId)) {
                    resetSpecialSchedules(chatId);
                }
                break;
            case "⬅️ Назад":
                sendWelcomeMessage(chatId);
                break;
            case "❌ Отмена":
                cancelEditing(chatId);
                break;
            case "ℹ️ О боте":
                sendAboutMessage(chatId);
                break;
            default:
                // Если введен номер дня для редактирования
                if (isAdmin(chatId) && messageText.matches("[1-7]")) {
                    int dayNum = Integer.parseInt(messageText);
                    startEditDay(chatId, dayNum - 1);
                } else {
                    sendMessageWithKeyboard(chatId, "Выберите действие из меню:");
                }
        }
    }

    private boolean isAdmin(long chatId) {
        return adminUsers.contains(chatId);
    }

    private void showManagementPanel(long chatId) throws TelegramApiException {
        String text = "⚙️ **Панель управления расписанием**\n\n" +
                "📊 **Текущий статус:**\n" +
                "• Сегодня: " + (todaySpecial != null ? "🔄 Изменено" : "📋 Стандартное") + "\n" +
                "• Завтра: " + (tomorrowSpecial != null ? "🔄 Изменено" : "📋 Стандартное") + "\n\n" +
                "📝 **Доступные действия:**\n" +
                "• ✏️ Редактировать основное расписание\n" +
                "• 🕘 Изменить расписание на сегодня\n" +
                "• 🕘 Изменить расписание на завтра\n" +
                "• 🔄 Сбросить временные изменения";

        sendMessageWithManagementKeyboard(chatId, text);
    }

    private void showScheduleEditor(long chatId) throws TelegramApiException {
        StringBuilder sb = new StringBuilder();
        sb.append("📝 **Редактор основного расписания**\n\n");
        sb.append("Текущее расписание:\n\n");

        for (int i = 0; i < schedule.length; i++) {
            sb.append("**").append(i + 1).append(". ").append(schedule[i][0]).append("**\n");
            if (schedule[i][1].equals("ОТДЫХ")) {
                sb.append("   🎉 ОТДЫХ\n");
            } else {
                sb.append("   🕘 ").append(schedule[i][1]).append("\n");
                if (!schedule[i][2].isEmpty()) {
                    sb.append("   🕘 ").append(schedule[i][2]).append("\n");
                }
            }
            sb.append("\n");
        }

        sb.append("📋 **Для редактирования отправьте номер дня (1-7):**");

        editMode = "SELECT_DAY";
        sendMessageWithCancelKeyboard(chatId, sb.toString());
    }

    private void startEditDay(long chatId, int dayIndex) throws TelegramApiException {
        currentEditDay = dayIndex;
        editMode = "EDIT_DAY";

        String text = "✏️ **Редактирование: " + schedule[dayIndex][0] + "**\n\n" +
                "📋 **Текущее расписание:**\n";

        if (schedule[dayIndex][1].equals("ОТДЫХ")) {
            text += "🎉 ОТДЫХ\n";
        } else {
            text += "🕘 " + schedule[dayIndex][1] + "\n";
            if (!schedule[dayIndex][2].isEmpty()) {
                text += "🕘 " + schedule[dayIndex][2] + "\n";
            }
        }

        text += "\n📝 **Введите новое расписание:**\n" +
                "• Каждое занятие с новой строки\n" +
                "• Для отдыха отправьте: **ОТДЫХ**\n\n" +
                "💡 **Пример:**\n" +
                "9:00-12:30 - Майсор класс\n" +
                "18:00-20:00 - Практика";

        sendMessageWithCancelKeyboard(chatId, text);
    }

    private void startEditToday(long chatId) throws TelegramApiException {
        int todayIndex = getDayOfWeekIndex(Calendar.getInstance());
        String[] currentSchedule = todaySpecial != null ? todaySpecial : schedule[todayIndex];

        String text = "🕘 **Изменение расписания на СЕГОДНЯ**\n\n" +
                "📋 **Текущее расписание:**\n";

        if (currentSchedule[1].equals("ОТДЫХ")) {
            text += "🎉 ОТДЫХ\n";
        } else {
            text += "🕘 " + currentSchedule[1] + "\n";
            if (!currentSchedule[2].isEmpty()) {
                text += "🕘 " + currentSchedule[2] + "\n";
            }
        }

        text += "\n📝 **Введите новое расписание:**\n" +
                "• Каждое занятие с новой строки\n" +
                "• Для отдыха отправьте: **ОТДЫХ**";

        editMode = "EDIT_TODAY";
        sendMessageWithCancelKeyboard(chatId, text);
    }

    private void startEditTomorrow(long chatId) throws TelegramApiException {
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);
        int tomorrowIndex = getDayOfWeekIndex(tomorrow);
        String[] currentSchedule = tomorrowSpecial != null ? tomorrowSpecial : schedule[tomorrowIndex];

        String text = "🕘 **Изменение расписания на ЗАВТРА**\n\n" +
                "📋 **Текущее расписание:**\n";

        if (currentSchedule[1].equals("ОТДЫХ")) {
            text += "🎉 ОТДЫХ\n";
        } else {
            text += "🕘 " + currentSchedule[1] + "\n";
            if (!currentSchedule[2].isEmpty()) {
                text += "🕘 " + currentSchedule[2] + "\n";
            }
        }

        text += "\n📝 **Введите новое расписание:**\n" +
                "• Каждое занятие с новой строки\n" +
                "• Для отдыха отправьте: **ОТДЫХ**";

        editMode = "EDIT_TOMORROW";
        sendMessageWithCancelKeyboard(chatId, text);
    }

    private void handleEditInput(long chatId, String inputText) throws TelegramApiException {
        switch (editMode) {
            case "EDIT_DAY":
                saveDaySchedule(chatId, inputText);
                break;
            case "EDIT_TODAY":
                saveTodaySchedule(chatId, inputText);
                break;
            case "EDIT_TOMORROW":
                saveTomorrowSchedule(chatId, inputText);
                break;
            default:
                sendMessage(chatId, "❌ Неизвестный режим редактирования");
                resetEditing();
                showManagementPanel(chatId);
        }
    }

    private void saveDaySchedule(long chatId, String inputText) throws TelegramApiException {
        if (inputText.equalsIgnoreCase("ОТДЫХ")) {
            schedule[currentEditDay][1] = "ОТДЫХ";
            schedule[currentEditDay][2] = "";
            sendMessage(chatId, "✅ **" + schedule[currentEditDay][0] + "** теперь день отдыха! 🎉");
        } else {
            String[] lines = inputText.split("\n");
            if (lines.length > 0) {
                schedule[currentEditDay][1] = lines[0].trim();
                schedule[currentEditDay][2] = lines.length > 1 ? lines[1].trim() : "";
                sendMessage(chatId, "✅ Расписание для **" + schedule[currentEditDay][0] + "** обновлено! 📝");
            }
        }
        resetEditing();
        showManagementPanel(chatId);
    }

    private void saveTodaySchedule(long chatId, String inputText) throws TelegramApiException {
        if (inputText.equalsIgnoreCase("ОТДЫХ")) {
            todaySpecial = new String[]{"Сегодня", "ОТДЫХ", ""};
            sendMessage(chatId, "✅ Расписание на **сегодня** изменено на ОТДЫХ! 🎉");
        } else {
            String[] lines = inputText.split("\n");
            if (lines.length > 0) {
                todaySpecial = new String[]{"Сегодня", lines[0].trim(), lines.length > 1 ? lines[1].trim() : ""};
                sendMessage(chatId, "✅ Расписание на **сегодня** обновлено! 📝");
            }
        }
        resetEditing();
        showManagementPanel(chatId);
    }

    private void saveTomorrowSchedule(long chatId, String inputText) throws TelegramApiException {
        if (inputText.equalsIgnoreCase("ОТДЫХ")) {
            tomorrowSpecial = new String[]{"Завтра", "ОТДЫХ", ""};
            sendMessage(chatId, "✅ Расписание на **завтра** изменено на ОТДЫХ! 🎉");
        } else {
            String[] lines = inputText.split("\n");
            if (lines.length > 0) {
                tomorrowSpecial = new String[]{"Завтра", lines[0].trim(), lines.length > 1 ? lines[1].trim() : ""};
                sendMessage(chatId, "✅ Расписание на **завтра** обновлено! 📝");
            }
        }
        resetEditing();
        showManagementPanel(chatId);
    }

    private void resetSpecialSchedules(long chatId) throws TelegramApiException {
        todaySpecial = null;
        tomorrowSpecial = null;
        sendMessage(chatId, "✅ Все временные изменения сброшены! 🔄\nТеперь используется основное расписание.");
        showManagementPanel(chatId);
    }

    private void cancelEditing(long chatId) throws TelegramApiException {
        resetEditing();
        sendMessage(chatId, "❌ Редактирование отменено");
        showManagementPanel(chatId);
    }

    private void resetEditing() {
        editMode = null;
        currentEditDay = -1;
    }

    private void sendWelcomeMessage(long chatId) throws TelegramApiException {
        String text = "🧘‍♀️ **Добро пожаловать в Йога-бот!**\n\n" +
                "Я помогу вам с расписанием занятий:\n\n" +
                "📅 **Просмотр расписания:**\n" +
                "• Полное расписание на неделю\n" +
                "• Занятия на сегодня\n" +
                "• Занятия на завтра\n\n" +
                "⚙️ **Для администраторов:**\n" +
                "• Редактирование расписания\n" +
                "• Временные изменения\n\n" +
                "Выберите действие ниже 👇";

        sendMessageWithKeyboard(chatId, text);
    }

    private void sendAboutMessage(long chatId) throws TelegramApiException {
        String text = "ℹ️ **О боте**\n\n" +
                "Йога-бот v2.0\n\n" +
                "📋 **Функции:**\n" +
                "• Просмотр расписания занятий\n" +
                "• Автоматические напоминания\n" +
                "• Настройка расписания (администраторы)\n\n" +
                "⚡ **Для администраторов** доступно управление расписанием.";

        sendMessage(chatId, text);
    }

    private void sendFullSchedule(long chatId) throws TelegramApiException {
        StringBuilder sb = new StringBuilder();
        sb.append("📅 **Полное расписание занятий**\n\n");

        for (String[] day : schedule) {
            sb.append("**").append(day[0]).append(":**\n");
            if (day[1].equals("ОТДЫХ")) {
                sb.append("🎉 **ОТДЫХ**\n");
            } else {
                sb.append("🕘 ").append(day[1]).append("\n");
                if (!day[2].isEmpty()) {
                    sb.append("🕘 ").append(day[2]).append("\n");
                }
            }
            sb.append("\n");
        }

        if (todaySpecial != null || tomorrowSpecial != null) {
            sb.append("⚡ **Временные изменения:**\n");
            if (todaySpecial != null) sb.append("• Сегодня: изменено\n");
            if (tomorrowSpecial != null) sb.append("• Завтра: изменено\n");
        }

        sendMessage(chatId, sb.toString());
    }

    private void sendTodaySchedule(long chatId) throws TelegramApiException {
        int todayIndex = getDayOfWeekIndex(Calendar.getInstance());
        String[] todaySchedule = todaySpecial != null ? todaySpecial : schedule[todayIndex];

        StringBuilder sb = new StringBuilder();
        sb.append("📋 **СЕГОДНЯ** (").append(todaySchedule[0]).append(")\n\n");

        if (todaySchedule[1].equals("ОТДЫХ")) {
            sb.append("🎉 **ОТДЫХ**\n\n");
            sb.append("Наслаждайтесь свободным днем! 🌈");
        } else {
            sb.append("🕘 ").append(todaySchedule[1]).append("\n");
            if (!todaySchedule[2].isEmpty()) {
                sb.append("🕘 ").append(todaySchedule[2]).append("\n");
            }
        }

        if (todaySpecial != null) {
            sb.append("\n\n⚡ *Временное изменение*");
        }

        sendMessage(chatId, sb.toString());
    }

    private void sendTomorrowSchedule(long chatId) throws TelegramApiException {
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);
        int tomorrowIndex = getDayOfWeekIndex(tomorrow);
        String[] tomorrowSchedule = tomorrowSpecial != null ? tomorrowSpecial : schedule[tomorrowIndex];

        StringBuilder sb = new StringBuilder();
        sb.append("📆 **ЗАВТРА** (").append(tomorrowSchedule[0]).append(")\n\n");

        if (tomorrowSchedule[1].equals("ОТДЫХ")) {
            sb.append("🎉 **ОТДЫХ**\n\n");
            sb.append("Планируйте отдых! 😴");
        } else {
            sb.append("🕘 ").append(tomorrowSchedule[1]).append("\n");
            if (!tomorrowSchedule[2].isEmpty()) {
                sb.append("🕘 ").append(tomorrowSchedule[2]).append("\n");
            }
        }

        if (tomorrowSpecial != null) {
            sb.append("\n\n⚡ *Временное изменение*");
        }

        sendMessage(chatId, sb.toString());
    }

    private int getDayOfWeekIndex(Calendar calendar) {
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        return (dayOfWeek + 5) % 7;
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

    private void sendMessageWithManagementKeyboard(long chatId, String text) throws TelegramApiException {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        message.setReplyMarkup(createManagementKeyboard());
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

    private ReplyKeyboardMarkup createManagementKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add("✏️ Редактировать расписание");
        row1.add("🕘 Изменить сегодня");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("🕘 Изменить завтра");
        row2.add("🔄 Сбросить изменения");

        KeyboardRow row3 = new KeyboardRow();
        row3.add("⬅️ Назад");
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
git add src/main/java/org/example
git commit -m "КОРРЕКТИРОВОЧКА КНОПОК РЕДАКТИРОВАНЯ"
git push origin main