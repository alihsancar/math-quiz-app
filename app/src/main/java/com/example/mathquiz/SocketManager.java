package com.example.mathquiz;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONObject;

/**
 * SocketManager - Merkezi socket iletişim yöneticisi
 * JSON tabanlı mesajlaşmayı yönetir
 */
public class SocketManager {

    private static final String TAG = "SocketManager";

    public interface Listener {
        void onMessage(JSONObject message);
    }

    public interface ConnectionListener {
        void onDisconnected();
    }

    private static Listener listener;
    private static ConnectionListener connectionListener;
    private static Thread listenThread;
    private static volatile boolean running = false;
    private static final Object listenerLock = new Object();
    private static Handler mainHandler = new Handler(Looper.getMainLooper());

    public static void setListener(Listener l) {
        synchronized (listenerLock) {
            listener = l;
        }
    }

    public static void setConnectionListener(ConnectionListener l) {
        synchronized (listenerLock) {
            connectionListener = l;
        }
    }

    public static synchronized void startListening() {
        if (!SocketHolder.isReady()) {
            Log.e(TAG, "Socket hazır değil, dinleme başlatılamadı");
            return;
        }

        if (running && listenThread != null && listenThread.isAlive()) {
            Log.w(TAG, "Dinleme zaten aktif");
            return;
        }

        running = true;

        listenThread = new Thread(() -> {
            Log.d(TAG, "Dinleme thread'i başladı");
            try {
                String line;
                while (running && SocketHolder.isReady()) {
                    line = SocketHolder.in.readLine();

                    if (line == null) {
                        Log.w(TAG, "Bağlantı koptu (null line)");
                        break;
                    }

                    line = line.trim();
                    if (line.isEmpty()) {
                        continue;
                    }

                    Log.d(TAG, "<<< MESAJ ALINDI: " + line);

                    try {
                        JSONObject obj = new JSONObject(line);
                        final String type = obj.optString("type", "UNKNOWN");

                        Log.d(TAG, "<<< Mesaj tipi: " + type);

                        synchronized (listenerLock) {
                            if (listener != null) {
                                final JSONObject finalObj = obj;
                                mainHandler.post(() -> {
                                    synchronized (listenerLock) {
                                        if (listener != null) {
                                            Log.d(TAG, "<<< Listener'a iletiliyor: " + type);
                                            listener.onMessage(finalObj);
                                        } else {
                                            Log.w(TAG, "<<< Listener null, mesaj iletilmedi: " + type);
                                        }
                                    }
                                });
                            } else {
                                Log.w(TAG, "<<< Listener null (ilk kontrol): " + type);
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "JSON parse hatası: " + e.getMessage());
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Dinleme hatası: " + e.getMessage());
            } finally {
                running = false;
                Log.d(TAG, "Dinleme thread'i sonlandı");

                mainHandler.post(() -> {
                    synchronized (listenerLock) {
                        if (connectionListener != null) {
                            connectionListener.onDisconnected();
                        }
                    }
                });
            }
        });

        listenThread.setName("SocketManager-Listener");
        listenThread.start();
    }

    /**
     * JSON mesajı ASENKRON gönderir (normal kullanım)
     */
    public static void send(JSONObject obj) {
        if (!SocketHolder.isReady()) {
            Log.e(TAG, "Socket hazır değil, mesaj gönderilemedi");
            return;
        }

        new Thread(() -> {
            try {
                String jsonStr = obj.toString();
                Log.d(TAG, ">>> MESAJ GÖNDERİLİYOR (async): " + jsonStr);
                SocketHolder.send(jsonStr);
                Log.d(TAG, ">>> Mesaj gönderildi (async)");
            } catch (Exception e) {
                Log.e(TAG, "Mesaj gönderme hatası: " + e.getMessage());
            }
        }).start();
    }

    /**
     * JSON mesajı SENKRON gönderir (önemli mesajlar için - END_GAME gibi)
     * Bu metod mesaj gönderilene kadar bekler
     */
    public static boolean sendSync(JSONObject obj) {
        if (!SocketHolder.isReady()) {
            Log.e(TAG, "Socket hazır değil, mesaj gönderilemedi");
            return false;
        }

        try {
            String jsonStr = obj.toString();
            Log.d(TAG, ">>> MESAJ GÖNDERİLİYOR (sync): " + jsonStr);
            SocketHolder.send(jsonStr);
            Log.d(TAG, ">>> Mesaj gönderildi (sync)");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Mesaj gönderme hatası: " + e.getMessage());
            return false;
        }
    }

    public static void stop() {
        Log.d(TAG, "SocketManager durduruluyor");
        running = false;

        if (listenThread != null) {
            listenThread.interrupt();
            listenThread = null;
        }

        synchronized (listenerLock) {
            listener = null;
            connectionListener = null;
        }
    }

    public static boolean isRunning() {
        return running && listenThread != null && listenThread.isAlive();
    }
}