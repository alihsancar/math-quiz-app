package com.example.mathquiz;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * SocketHolder - Thread-safe socket yönetimi
 * Tüm socket bağlantılarını ve stream'leri tutar
 */
public class SocketHolder {

    private static final Object lock = new Object();

    public static Socket socket;
    public static PrintWriter out;
    public static BufferedReader in;

    /**
     * Socket bağlantısının hazır olup olmadığını kontrol eder
     * @return true eğer socket, input ve output stream'ler hazırsa
     */
    public static boolean isReady() {
        synchronized (lock) {
            return socket != null &&
                    socket.isConnected() &&
                    !socket.isClosed() &&
                    out != null &&
                    in != null;
        }
    }

    /**
     * Thread-safe mesaj gönderme
     * @param msg Gönderilecek mesaj
     */
    public static synchronized void send(String msg) {
        synchronized (lock) {
            try {
                if (out != null && socket != null && !socket.isClosed()) {
                    out.println(msg);
                    out.flush();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Tüm bağlantıları güvenli şekilde kapatır
     */
    public static void close() {
        synchronized (lock) {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception ignored) {}

            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception ignored) {}

            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (Exception ignored) {}

            socket = null;
            out = null;
            in = null;
        }
    }

    /**
     * Socket bağlantısını ve stream'leri ayarlar
     * @param s Socket bağlantısı
     * @param output PrintWriter output stream
     * @param input BufferedReader input stream
     */
    public static void setConnection(Socket s, PrintWriter output, BufferedReader input) {
        synchronized (lock) {
            socket = s;
            out = output;
            in = input;
        }
    }
}