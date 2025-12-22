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
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * ClientActivity - Host'a bağlanma ekranı
 * IP adresi girilerek host'a bağlanılır
 */
public class ClientActivity extends AppCompatActivity {

    private static final String TAG = "ClientActivity";
    private static final int PORT = 5050;
    private static final int CONNECTION_TIMEOUT = 10000; // 10 saniye

    private TextInputEditText etIp;
    private TextInputLayout inputLayout;
    private TextView tvTitle, tvStatus, tvPlayerName;
    private MaterialButton btnConnect;
    private CardView cardInput;

    private Handler handler = new Handler(Looper.getMainLooper());
    private volatile boolean isConnecting = false;

    // Oyuncu bilgileri
    private String clientName = "Client";
    private int clientAvatar = R.drawable.avatar1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        bindViews();
        loadPlayerInfo();
        animateEntrance();
    }

    private void bindViews() {
        tvTitle = findViewById(R.id.tvTitle);
        tvStatus = findViewById(R.id.tvStatus);
        tvPlayerName = findViewById(R.id.tvPlayerName);
        inputLayout = findViewById(R.id.inputLayout);
        etIp = findViewById(R.id.etIp);
        btnConnect = findViewById(R.id.btnConnect);
        cardInput = findViewById(R.id.cardInput);

        tvPlayerName.setText(clientName);

        btnConnect.setOnClickListener(v -> {
            animateButtonClick();
            new Handler().postDelayed(this::connect, 200);
        });

        // Son kullanılan IP'yi yükle
        SharedPreferences sp = getSharedPreferences("network_prefs", MODE_PRIVATE);
        String lastIp = sp.getString("last_ip", "");
        if (!lastIp.isEmpty()) {
            etIp.setText(lastIp);
        }
    }

    private void loadPlayerInfo() {
        SharedPreferences sp = getSharedPreferences("player_prefs", MODE_PRIVATE);
        clientName = sp.getString("client_name", "Client");
        clientAvatar = sp.getInt("client_avatar", R.drawable.avatar1);

        // Veya Intent'ten al
        String name = getIntent().getStringExtra("playerName");
        if (name != null && !name.isEmpty()) {
            clientName = name;
        }
        int avatar = getIntent().getIntExtra("playerAvatar", -1);
        if (avatar != -1) {
            clientAvatar = avatar;
        }
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
        cardInput.setScaleX(0.8f);
        cardInput.setScaleY(0.8f);
        cardInput.setAlpha(0f);
        cardInput.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(600)
                .setStartDelay(200)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        // Button from bottom
        btnConnect.setTranslationY(200f);
        btnConnect.setAlpha(0f);
        btnConnect.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(600)
                .setStartDelay(400)
                .start();
    }

    private void animateButtonClick() {
        btnConnect.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(() -> {
                    btnConnect.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start();
                })
                .start();
    }

    private void connect() {
        String ip = etIp.getText().toString().trim();

        if (ip.isEmpty()) {
            inputLayout.setError("IP adresi gerekli!");
            Toast.makeText(this, "IP adresi girin", Toast.LENGTH_SHORT).show();
            return;
        }

        inputLayout.setError(null);

        if (isConnecting) {
            Toast.makeText(this, "Bağlantı devam ediyor...", Toast.LENGTH_SHORT).show();
            return;
        }

        // IP'yi kaydet
        SharedPreferences sp = getSharedPreferences("network_prefs", MODE_PRIVATE);
        sp.edit().putString("last_ip", ip).apply();

        isConnecting = true;
        btnConnect.setEnabled(false);
        tvStatus.setText("🔄 Bağlanıyor...");

        new Thread(() -> {
            try {
                // Önceki bağlantıyı temizle
                SocketHolder.close();

                Log.d(TAG, "Bağlanılıyor: " + ip + ":" + PORT);

                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(ip, PORT), CONNECTION_TIMEOUT);

                if (!socket.isConnected()) {
                    throw new Exception("Bağlantı kurulamadı");
                }

                Log.d(TAG, "Socket bağlandı");

                // Stream'leri ayarla
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream())
                );

                SocketHolder.setConnection(socket, out, in);

                if (!SocketHolder.isReady()) {
                    throw new Exception("Stream'ler hazırlanamadı");
                }

                handler.post(() -> {
                    tvStatus.setText("✅ Bağlandı!");
                    Toast.makeText(this, "Bağlantı başarılı! 🎉", Toast.LENGTH_SHORT).show();

                    // Kısa gecikme ile oyuna geç
                    handler.postDelayed(this::goToGame, 1000);
                });

            } catch (Exception e) {
                Log.e(TAG, "Bağlantı hatası: " + e.getMessage());
                e.printStackTrace();

                handler.post(() -> {
                    tvStatus.setText("❌ Bağlantı başarısız!");
                    Toast.makeText(this, "Bağlantı hatası: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    btnConnect.setEnabled(true);
                    isConnecting = false;
                });

                SocketHolder.close();
            }
        }).start();
    }

    private void goToGame() {
        if (!SocketHolder.isReady()) {
            tvStatus.setText("Socket hazır değil!");
            btnConnect.setEnabled(true);
            isConnecting = false;
            return;
        }

        // Dinlemeyi başlat
        SocketManager.startListening();

        // Oyun ayarlarını al
        SharedPreferences sp = getSharedPreferences("game_settings", MODE_PRIVATE);
        String difficulty = sp.getString("difficulty", "easy");
        int questionCount = sp.getInt("question_count", 5);
        int timePerTurn = sp.getInt("time_per_turn", 30);

        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("isOnline", true);
        intent.putExtra("isHost", false);

        // Client kendi bilgilerini Player 2 olarak set eder
        intent.putExtra("p2Name", clientName);
        intent.putExtra("p2Avatar", clientAvatar);

        // Host bilgileri GameActivity'de alınacak
        intent.putExtra("p1Name", "Host");
        intent.putExtra("p1Avatar", R.drawable.avatar1);

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
        isConnecting = false;
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}