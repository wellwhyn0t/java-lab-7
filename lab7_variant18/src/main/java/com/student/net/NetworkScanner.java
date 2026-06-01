package com.student.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Утилита для сканирования сети на наличие серверов-цензоров (вариант 18).
 * Проверяет указанные IP-адреса на предмет открытых портов.
 */
public class NetworkScanner {
    
    private static final int DEFAULT_PORT = 8018;
    private static final int TIMEOUT_MS = 300;
    
    /**
     * Сканирует диапазон адресов в локальной сети.
     */
    public static void main(String[] args) {
        String targetNetwork = "192.168.1.";
        int port = DEFAULT_PORT;
        
        if (args.length >= 1) {
            targetNetwork = args[0];
            if (!targetNetwork.endsWith(".")) {
                targetNetwork += ".";
            }
        }
        if (args.length >= 2) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Некорректный порт. Используется порт: " + DEFAULT_PORT);
            }
        }
        
        System.out.println("========================================");
        System.out.println("Сканер сети для поиска серверов варианта 18");
        System.out.println("Диапазон: " + targetNetwork + "1 - " + targetNetwork + "254");
        System.out.println("Порт: " + port);
        System.out.println("Таймаут: " + TIMEOUT_MS + " мс");
        System.out.println("========================================\n");
        
        int foundCount = 0;
        String myIp = getLocalIpAddress();
        
        for (int i = 1; i <= 254; i++) {
            String ip = targetNetwork + i;
            
            // Пропускаем свой собственный адрес
            if (ip.equals(myIp)) {
                continue;
            }
            
            if (checkPort(ip, port)) {
                foundCount++;
                System.out.println("[" + foundCount + "] Найден сервер на " + ip + ":" + port);
                
                // Тестируем соединение
                testServerConnection(ip, port);
            }
            
            // Выводим прогресс каждые 50 адресов
            if (i % 50 == 0) {
                System.out.println("Проверено " + i + " из 254 адресов...");
            }
        }
        
        System.out.println("\n========================================");
        System.out.println("Сканирование завершено.");
        System.out.println("Найдено серверов: " + foundCount);
        System.out.println("========================================");
    }
    
    /**
     * Проверяет, открыт ли указанный порт на данном IP.
     */
    private static boolean checkPort(String ip, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(ip, port), TIMEOUT_MS);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * Тестирует соединение с найденным сервером.
     */
    private static void testServerConnection(String ip, int port) {
        try (Socket socket = new Socket(ip, port);
             java.io.PrintWriter out = new java.io.PrintWriter(
                 socket.getOutputStream(), true);
             java.io.BufferedReader in = new java.io.BufferedReader(
                 new java.io.InputStreamReader(socket.getInputStream()))) {
            
            String testMessage = "Hello World";
            out.println(testMessage);
            
            String response = in.readLine();
            if (response != null) {
                System.out.println("    Тест: \"" + testMessage + "\" -> \"" + response + "\"");
            }
        } catch (IOException e) {
            System.out.println("    Ошибка тестирования: " + e.getMessage());
        }
    }
    
    /**
     * Возвращает локальный IP-адрес машины.
     */
    private static String getLocalIpAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "unknown";
        }
    }
}
