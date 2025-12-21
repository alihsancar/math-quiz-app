package com.example.mathquiz;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

/**
 * MainActivity - Ana menü ekranı
 * Offline, Online, Ayarlar ve Geçmiş seçenekleri sunar
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Dark mode ayarını uygula
        applyTheme();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initButtons();
    }

    private void applyTheme() {
        SharedPreferences sp = getSharedPreferences("game_settings", MODE_PRIVATE);
        boolean darkMode = sp.getBoolean("dark_mode", false);

        AppCompatDelegate.setDefaultNightMode(
                darkMode ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    private void initButtons() {
        Button btnOffline = findViewById(R.id.btnOffline);
        Button btnOnline = findViewById(R.id.btnOnline);
        Button btnSettings = findViewById(R.id.btnSettings);
        Button btnHistory = findViewById(R.id.btnHistory);

        // Offline oyun - Oyuncu ayarlama ekranına git
        btnOffline.setOnClickListener(v ->
                startActivity(new Intent(this, SetupPlayersActivity.class))
        );

        // Online oyun - Online menü ekranına git
        btnOnline.setOnClickListener(v ->
                startActivity(new Intent(this, OnlineMenuActivity.class))
        );

        // Ayarlar
        btnSettings.setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class))
        );

        // Geçmiş
        btnHistory.setOnClickListener(v ->
                startActivity(new Intent(this, HistoryActivity.class))
        );
    }
}