package org.example;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

public class WebhookServer {
    public static void main(String[] args) {
        System.out.println("🚀 Starting Yoga Telegram Bot (Long Polling)...");

        try {
            // Запускаем HTTP сервер для health checks
            startHealthCheckServer();

            // Создаем и регистрируем бота
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            YogaManagerBot bot = new YogaManagerBot();
            botsApi.registerBot(bot);

            System.out.println("✅ Bot successfully registered!");
            System.out.println("🤖 Username: " + bot.getBotUsername());
            System.out.println("⏰ Bot is now listening for messages...");

            // Бесконечный цикл чтобы приложение не завершалось
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }

        } catch (TelegramApiException e) {
            System.err.println("❌ Error initializing bot: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void startHealthCheckServer() {
        try {
            // Получаем порт из переменных окружения (Render автоматически устанавливает PORT)
            String portStr = System.getenv("PORT");
            int port = (portStr != null && !portStr.isEmpty()) ? Integer.parseInt(portStr) : 8080;

            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

            // Health check endpoint
            server.createContext("/health", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    String response = "{\"status\":\"ok\",\"service\":\"yoga-telegram-bot\"}";
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, response.getBytes().length);

                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                }
            });

            // Root endpoint
            server.createContext("/", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    String response = "Yoga Telegram Bot is running! 🧘‍♀️";
                    exchange.getResponseHeaders().set("Content-Type", "text/plain");
                    exchange.sendResponseHeaders(200, response.getBytes().length);

                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                }
            });

            server.setExecutor(null);
            server.start();

            System.out.println("✅ Health check server started on port " + port);
            System.out.println("🌐 Health check URL: http://0.0.0.0:" + port + "/health");

        } catch (IOException e) {
            System.err.println("❌ Failed to start health check server: " + e.getMessage());
        }
    }
}