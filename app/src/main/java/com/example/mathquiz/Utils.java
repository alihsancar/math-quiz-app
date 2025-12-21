package com.example.mathquiz;

import android.util.Log;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

/**
 * Utils - Yardımcı fonksiyonlar
 */
public class Utils {

    private static final String TAG = "Utils";

    /**
     * Cihazın yerel IP adresini döndürür
     * @return IP adresi string olarak, bulunamazsa "Bilinmiyor"
     */
    public static String getLocalIpAddress() {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());

            for (NetworkInterface networkInterface : interfaces) {
                // Loopback ve kapalı arayüzleri atla
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }

                List<InetAddress> addresses = Collections.list(networkInterface.getInetAddresses());

                for (InetAddress address : addresses) {
                    // Sadece IPv4 adreslerini al
                    if (address instanceof Inet4Address) {
                        String ip = address.getHostAddress();

                        // Loopback adresini atla
                        if (ip != null && !ip.equals("127.0.0.1")) {
                            Log.d(TAG, "IP bulundu: " + ip + " (" + networkInterface.getName() + ")");
                            return ip;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "IP adresi alınamadı: " + e.getMessage());
        }

        return "Bilinmiyor";
    }

    /**
     * WiFi üzerinden IP adresini döndürür
     * @return WiFi IP adresi veya null
     */
    public static String getWifiIpAddress() {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());

            for (NetworkInterface networkInterface : interfaces) {
                String name = networkInterface.getName().toLowerCase();

                // WiFi arayüzünü bul (genellikle wlan0)
                if (name.contains("wlan") || name.contains("wifi")) {
                    if (!networkInterface.isUp()) continue;

                    List<InetAddress> addresses = Collections.list(networkInterface.getInetAddresses());

                    for (InetAddress address : addresses) {
                        if (address instanceof Inet4Address) {
                            return address.getHostAddress();
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "WiFi IP adresi alınamadı: " + e.getMessage());
        }

        return null;
    }
}