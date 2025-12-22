package com.example.mathquiz;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;

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

    private TextView tvTitle, tvStatus, tvIp, tvPlayerName, tvWaiting;
    private MaterialButton btnCancel;
    private CardView cardInfo;

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

        bindViews();
        loadPlayerInfo();
        animateEntrance();
        startServer();
    }

    private void bindViews() {
        tvTitle = findViewById(R.id.tvTitle);
        tvStatus = findViewById(R.id.tvStatus);
        tvIp = findViewById(R.id.tvIp);
        tvPlayerName = findViewById(R.id.tvPlayerName);
        tvWaiting = findViewById(R.id.tvWaiting);
        btnCancel = findViewById(R.id.btnCancel);
        cardInfo = findViewById(R.id.cardInfo);

        String ip = Utils.getLocalIpAddress();
        tvIp.setText(ip);
        tvPlayerName.setText(hostName);

        btnCancel.setOnClickListener(v -> {
            animateButtonClick();
            new Handler().postDelayed(() -> {
                isWaiting = false;
                finish();
            }, 200);
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

    private void animateEntrance() {
        // Title fade in
        tvTitle.setAlpha(0f);
        tvTitle.setTranslationY(-50f);
        tvTitle.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        // Card scale in
        cardInfo.setScaleX(0.8f);
        cardInfo.setScaleY(0.8f);
        cardInfo.setAlpha(0f);
        cardInfo.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(600)
                .setStartDelay(200)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        // Waiting animation
        tvWaiting.setAlpha(0f);
        tvWaiting.animate()
                .alpha(1f)
                .setDuration(600)
                .setStartDelay(400)
                .start();

        startWaitingAnimation();

        // Button from bottom
        btnCancel.setTranslationY(200f);
        btnCancel.setAlpha(0f);
        btnCancel.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(600)
                .setStartDelay(600)
                .start();
    }

    private void startWaitingAnimation() {
        handler.postDelayed(new Runnable() {
            int dots = 0;
            @Override
            public void run() {
                if (!isWaiting) return;

                dots = (dots + 1) % 4;
                String waitingText = "Rakip bekleniyor" + ".".repeat(dots);
                tvWaiting.setText(waitingText);

                handler.postDelayed(this, 500);
            }
        }, 500);
    }

    private void animateButtonClick() {
        btnCancel.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(() -> {
                    btnCancel.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start();
                })
                .start();
    }

    private void startServer() {
        new Thread(() -> {
            try {
                // Önceki socket'i temizle
                SocketHolder.close();

                serverSocket = new ServerSocket(PORT);
                serverSocket.setReuseAddress(true);
                Log.d(TAG, "Server başlatıldı, port: " + PORT);

                updateStatus("🎮 Oda hazır!");

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
                    updateStatus("❌ Bağlantı hatası!");
                    return;
                }

                updateStatus("✅ Rakip bağlandı!");
                handler.post(() -> {
                    tvWaiting.setText("Oyun başlıyor...");
                    Toast.makeText(this, "Rakip bağlandı! 🎉", Toast.LENGTH_SHORT).show();
                });

                // Kısa bir gecikme ile oyuna geç
                handler.postDelayed(this::goToGame, 1500);

            } catch (Exception e) {
                Log.e(TAG, "Server hatası: " + e.getMessage());
                e.printStackTrace();
                updateStatus("❌ Hata: " + e.getMessage());
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
        handler.removeCallbacksAndMessages(null);

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "ServerSocket kapatma hatası: " + e.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
        isWaiting = false;
        finish();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}