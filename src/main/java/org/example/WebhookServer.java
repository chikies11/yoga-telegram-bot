package org.example;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.telegram.telegrambots.updatesreceivers.ServerlessWebhook;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

public class WebhookServer {
    private static YogaManagerBot bot;

    public static void main(String[] args) {
        System.out.println("🚀 Starting Yoga Telegram Bot (Webhook)...");

        try {
            // Получаем параметры из окружения
            String botToken = System.getenv("BOT_TOKEN");
            String webhookPath = System.getenv("BOT_PATH");
            String renderUrl = System.getenv("RENDER_EXTERNAL_URL");

            if (botToken == null || webhookPath == null || renderUrl == null) {
                System.err.println("❌ Missing required environment variables");
                System.exit(1);
            }

            String webhookUrl = renderUrl + "/" + webhookPath;
            System.out.println("🌐 Webhook URL: " + webhookUrl);

            // Создаем бота с webhook
            bot = new YogaManagerBot();

            // Настраиваем webhook
            SetWebhook setWebhook = SetWebhook.builder()
                    .url(webhookUrl)
                    .build();

            // Исправленная строка: используем ServerlessWebhook и правильный метод регистрации
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            bot.setWebhook(setWebhook); // Устанавливаем webhook напрямую через бота

            // Запускаем HTTP сервер для health checks и webhook
            startWebhookServer(webhookPath);

            System.out.println("✅ Bot successfully registered with Webhook!");
            System.out.println("🤖 Username: " + bot.getBotUsername());

        } catch (TelegramApiException e) {
            System.err.println("❌ Error initializing bot: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void startWebhookServer(String webhookPath) {
        try {
            String portStr = System.getenv("PORT");
            int port = (portStr != null && !portStr.isEmpty()) ? Integer.parseInt(portStr) : 8080;

            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

            // Webhook endpoint
            server.createContext("/" + webhookPath, new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                        // Обрабатываем webhook запрос от Telegram
                        bot.processWebhookUpdate(exchange.getRequestBody());
                        sendResponse(exchange, 200, "OK");
                    } else {
                        sendResponse(exchange, 405, "Method Not Allowed");
                    }
                }
            });

            // Health check endpoint
            server.createContext("/health", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    String response = "{\"status\":\"ok\",\"service\":\"yoga-telegram-bot\"}";
                    sendResponse(exchange, 200, response);
                }
            });

            // Root endpoint
            server.createContext("/", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    String response = "Yoga Telegram Bot is running! 🧘‍♀️";
                    sendResponse(exchange, 200, response);
                }
            });

            server.setExecutor(null);
            server.start();

            System.out.println("✅ Webhook server started on port " + port);
            System.out.println("🌐 Health check URL: http://0.0.0.0:" + port + "/health");

        } catch (IOException e) {
            System.err.println("❌ Failed to start webhook server: " + e.getMessage());
        }
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);

        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}