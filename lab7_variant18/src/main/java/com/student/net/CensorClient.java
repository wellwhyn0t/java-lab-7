package com.student.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Клиентское приложение для подключения к серверу-цензору (вариант 18).
 * Позволяет отправлять текстовые сообщения и получать обработанные ответы.
 */
public class CensorClient {
    
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 8018;
    
    private final String host;
    private final int port;
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private Scanner scanner;
    
    public CensorClient(String host, int port) {
        this.host = host;
        this.port = port;
    }
    
    /**
     * Подключается к серверу и запускает сеанс общения.
     */
    public void connect() {
        try {
            socket = new Socket(host, port);
            reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            writer = new PrintWriter(
                socket.getOutputStream(), true, StandardCharsets.UTF_8);
            scanner = new Scanner(System.in);
            
            System.out.println("========================================");
            System.out.println("Подключено к серверу-цензору на " + host + ":" + port);
            System.out.println("Сервер заменяет гласные буквы на '*'");
            System.out.println("Введите текст для отправки (или 'exit' для выхода)");
            System.out.println("========================================");
            
            startCommunication();
            
        } catch (UnknownHostException e) {
            System.err.println("Ошибка: неизвестный хост " + host);
        } catch (IOException e) {
            System.err.println("Ошибка подключения: " + e.getMessage());
        } finally {
            disconnect();
        }
    }
    
    /**
     * Основной цикл общения с сервером.
     */
    private void startCommunication() {
        try {
            while (true) {
                System.out.print("\nВы: ");
                String userInput = scanner.nextLine();
                
                if (userInput.equalsIgnoreCase("exit") || 
                    userInput.equalsIgnoreCase("quit") ||
                    userInput.equalsIgnoreCase("выход")) {
                    System.out.println("Завершение работы...");
                    break;
                }
                
                if (userInput.trim().isEmpty()) {
                    continue;
                }
                
                writer.println(userInput);
                
                String response = reader.readLine();
                if (response != null) {
                    System.out.println("Ответ сервера: " + response);
                } else {
                    System.err.println("Сервер разорвал соединение");
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка связи с сервером: " + e.getMessage());
        }
    }
    
    /**
     * Отключается от сервера и закрывает все ресурсы.
     */
    private void disconnect() {
        try {
            if (scanner != null) scanner.close();
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (socket != null && !socket.isClosed()) socket.close();
            System.out.println("Соединение закрыто");
        } catch (IOException e) {
            System.err.println("Ошибка при закрытии соединения: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        String host = DEFAULT_HOST;
        int port = DEFAULT_PORT;
        
        if (args.length >= 1) {
            host = args[0];
        }
        if (args.length >= 2) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Некорректный порт. Используется порт по умолчанию: " + DEFAULT_PORT);
            }
        }
        
        CensorClient client = new CensorClient(host, port);
        client.connect();
    }
}
