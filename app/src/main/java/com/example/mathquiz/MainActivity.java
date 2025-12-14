package com.example.mathquiz;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // 🌙 DARK MODE AYARINI UYGULA (SADECE BURADA)
        SharedPreferences sp = getSharedPreferences("game_settings", MODE_PRIVATE);
        boolean dark = sp.getBoolean("dark_mode", false);

        AppCompatDelegate.setDefaultNightMode(
                dark ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_NO
        );

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnPlay = findViewById(R.id.btnPlay);
        Button btnSettings = findViewById(R.id.btnSettings);
        Button btnHistory = findViewById(R.id.btnHistory);

        btnPlay.setOnClickListener(v ->
                startActivity(new Intent(this, SetupPlayersActivity.class)));

        btnSettings.setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));

        btnHistory.setOnClickListener(v ->
                startActivity(new Intent(this, HistoryActivity.class)));
    }
}
