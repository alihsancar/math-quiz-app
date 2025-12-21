package com.example.mathquiz;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * HostActivity - Sunucu olarak oyun oluşturma ekranı
 * IP adresini gösterir ve client bağlantısını bekler
 */
public class HostActivity extends AppCompatActivity {

    private static final String TAG = "HostActivity";
    private static final int PORT = 5050;

    private TextView tvStatus, tvIp;
    private Button btnCancel;

    private ServerSocket serverSocket;
    private volatile boolean isWaiting = true;
    private Handler handler = new Handler(Looper.getMainLooper());

    // Oyuncu bilgileri
    private String hostName = "Host";
    private int hostAvatar = R.drawable.avatar1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);

        initViews();
        loadPlayerInfo();
        startServer();
    }

    private void initViews() {
        tvStatus = findViewById(R.id.tvStatus);
        tvIp = findViewById(R.id.tvIp);
        btnCancel = findViewById(R.id.btnCancel);

        String ip = Utils.getLocalIpAddress();
        tvIp.setText("IP: " + ip);
        tvStatus.setText("Rakip bekleniyor...");

        btnCancel.setOnClickListener(v -> {
            isWaiting = false;
            finish();
        });
    }

    private void loadPlayerInfo() {
        // SharedPreferences'tan host bilgilerini al
        SharedPreferences sp = getSharedPreferences("player_prefs", MODE_PRIVATE);
        hostName = sp.getString("host_name", "Host");
        hostAvatar = sp.getInt("host_avatar", R.drawable.avatar1);

        // Veya Intent'ten al
        hostName = getIntent().getStringExtra("playerName");
        if (hostName == null || hostName.isEmpty()) {
            hostName = "Host";
        }
        hostAvatar = getIntent().getIntExtra("playerAvatar", R.drawable.avatar1);
    }

    private void startServer() {
        new Thread(() -> {
            try {
                // Önceki socket'i temizle
                SocketHolder.close();

                serverSocket = new ServerSocket(PORT);
                serverSocket.setReuseAddress(true);
                Log.d(TAG, "Server başlatıldı, port: " + PORT);

                updateStatus("Rakip bekleniyor... (IP: " + Utils.getLocalIpAddress() + ")");

                // Client bağlantısını bekle
                Socket clientSocket = serverSocket.accept();

                if (!isWaiting) {
                    clientSocket.close();
                    return;
                }

                Log.d(TAG, "Client bağlandı: " + clientSocket.getInetAddress());

                // Socket stream'lerini ayarla
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream())
                );

                SocketHolder.setConnection(clientSocket, out, in);

                if (!SocketHolder.isReady()) {
                    updateStatus("Bağlantı hatası!");
                    return;
                }

                updateStatus("Rakip bağlandı! Oyun hazırlanıyor...");

                // Kısa bir gecikme ile oyuna geç
                handler.postDelayed(this::goToGame, 500);

            } catch (Exception e) {
                Log.e(TAG, "Server hatası: " + e.getMessage());
                e.printStackTrace();
                updateStatus("Bağlantı hatası: " + e.getMessage());
            }
        }).start();
    }

    private void updateStatus(String status) {
        handler.post(() -> tvStatus.setText(status));
    }

    private void goToGame() {
        if (!SocketHolder.isReady()) {
            Toast.makeText(this, "Bağlantı kurulamadı!", Toast.LENGTH_SHORT).show();
            tvStatus.setText("Socket hazır değil!");
            return;
        }

        // Dinlemeyi başlat
        SocketManager.startListening();

        // Oyun ayarlarını al
        SharedPreferences sp = getSharedPreferences("game_settings", MODE_PRIVATE);
        String difficulty = sp.getString("difficulty", "easy");
        int questionCount = sp.getInt("question_count", 5);
        int timePerTurn = sp.getInt("time_per_turn", 30);

        // GameActivity'ye geç
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("isOnline", true);
        intent.putExtra("isHost", true);

        // Host kendi bilgilerini Player 1 olarak set eder
        intent.putExtra("p1Name", hostName);
        intent.putExtra("p1Avatar", hostAvatar);

        // Client bilgileri GameActivity'de alınacak
        intent.putExtra("p2Name", "Rakip");
        intent.putExtra("p2Avatar", R.drawable.avatar1);

        // Oyun ayarları
        intent.putExtra("difficulty", difficulty);
        intent.putExtra("questionCount", questionCount);
        intent.putExtra("timePerTurn", timePerTurn);

        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isWaiting = false;

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "ServerSocket kapatma hatası: " + e.getMessage());
        }
    }
}