package com.student.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Сервер-цензор для лабораторной работы №7, вариант 18.
 * Заменяет все гласные буквы на символ '*'.
 * Гласные: a, e, i, o, u, y (и их заглавные версии).
 */
public class CensorServer {
    
    private static final int DEFAULT_PORT = 8018;
    private static final Logger LOGGER = Logger.getLogger(CensorServer.class.getName());
    
    private final int port;
    private ServerSocket serverSocket;
    private volatile boolean running;
    
    public CensorServer(int port) {
        this.port = port;
        this.running = false;
    }
    
    /**
     * Запускает сервер и начинает принимать подключения клиентов.
     */
    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            log("Сервер запущен на порту " + port);
            
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    log("Новое подключение от: " + clientSocket.getInetAddress().getHostAddress());
                    handleClient(clientSocket);
                } catch (IOException e) {
                    if (running) {
                        logError("Ошибка при приеме клиента: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            logError("Критическая ошибка сервера: " + e.getMessage());
        } finally {
            stop();
        }
    }
    
    /**
     * Обрабатывает соединение с одним клиентом.
     * Читает строки и отправляет обратно обработанные сообщения.
     */
    private void handleClient(Socket clientSocket) {
        try (BufferedReader reader = new BufferedReader(
                 new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter writer = new PrintWriter(
                 clientSocket.getOutputStream(), true, StandardCharsets.UTF_8)) {
            
            String inputLine;
            while ((inputLine = reader.readLine()) != null) {
                log("Получено: \"" + inputLine + "\"");
                
                String censoredText = censorVowels(inputLine);
                writer.println(censoredText);
                
                log("Отправлено: \"" + censoredText + "\"");
            }
        } catch (IOException e) {
            logError("Ошибка при работе с клиентом: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                // Игнорируем ошибку закрытия сокета
            }
        }
    }
    
    /**
     * Заменяет все гласные буквы на символ '*'.
     * Поддерживает латинские гласные в обоих регистрах.
     * 
     * @param text исходный текст
     * @return текст с замененными гласными
     */
    private String censorVowels(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        StringBuilder result = new StringBuilder(text.length());
        for (char ch : text.toCharArray()) {
            if (isVowel(ch)) {
                result.append('*');
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }
    
    /**
     * Проверяет, является ли символ гласной буквой.
     * 
     * @param ch проверяемый символ
     * @return true если символ - гласная (a, e, i, o, u, y в любом регистре)
     */
    private boolean isVowel(char ch) {
        char lower = Character.toLowerCase(ch);
        return lower == 'a' || lower == 'e' || lower == 'i' || 
               lower == 'o' || lower == 'u' || lower == 'y';
    }
    
    /**
     * Останавливает работу сервера.
     */
    public void stop() {
        running = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
                log("Сервер остановлен");
            } catch (IOException e) {
                logError("Ошибка при остановке сервера: " + e.getMessage());
            }
        }
    }
    
    private void log(String message) {
        LOGGER.log(Level.INFO, message);
        System.out.println("[SERVER] " + message);
    }
    
    private void logError(String message) {
        LOGGER.log(Level.SEVERE, message);
        System.err.println("[SERVER ERROR] " + message);
    }
    
    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Некорректный номер порта. Используется порт по умолчанию: " + DEFAULT_PORT);
            }
        }
        
        CensorServer server = new CensorServer(port);
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nПолучен сигнал завершения. Остановка сервера...");
            server.stop();
        }));
        
        server.start();
    }
}
