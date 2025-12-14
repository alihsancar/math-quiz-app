package com.example.mathquiz;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class SettingsActivity extends AppCompatActivity {

    private RadioButton rbEasy, rbMedium, rbHard;
    private RadioButton rbQ5, rbQ7, rbQ10;
    private RadioButton rbT15, rbT20, rbT30, rbT45, rbT60;
    private Button btnSaveSettings;
    private Switch switchSound, switchDarkMode;

    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sp = getSharedPreferences("game_settings", MODE_PRIVATE);

        bindViews();
        loadSettings();

        btnSaveSettings.setOnClickListener(v -> saveSettings());
    }

    private void bindViews() {
        // Zorluk
        rbEasy = findViewById(R.id.rbEasy);
        rbMedium = findViewById(R.id.rbMedium);
        rbHard = findViewById(R.id.rbHard);

        // Soru sayısı
        rbQ5 = findViewById(R.id.rbQ5);
        rbQ7 = findViewById(R.id.rbQ7);
        rbQ10 = findViewById(R.id.rbQ10);

        // Süre
        rbT15 = findViewById(R.id.rbT15);
        rbT20 = findViewById(R.id.rbT20);
        rbT30 = findViewById(R.id.rbT30);
        rbT45 = findViewById(R.id.rbT45);
        rbT60 = findViewById(R.id.rbT60);

        // Switchler
        switchSound = findViewById(R.id.switchSound);
        switchDarkMode = findViewById(R.id.switchDarkMode);

        // Kaydet
        btnSaveSettings = findViewById(R.id.btnSaveSettings);
    }

    private void loadSettings() {
        String difficulty = sp.getString("difficulty", "easy");
        int questionCount = sp.getInt("question_count", 5);
        int time = sp.getInt("time_per_turn", 30);
        boolean soundOn = sp.getBoolean("sound_on", true);
        boolean darkMode = sp.getBoolean("dark_mode", false);

        // Zorluk
        rbEasy.setChecked(difficulty.equals("easy"));
        rbMedium.setChecked(difficulty.equals("medium"));
        rbHard.setChecked(difficulty.equals("hard"));

        // Soru sayısı
        rbQ5.setChecked(questionCount == 5);
        rbQ7.setChecked(questionCount == 7);
        rbQ10.setChecked(questionCount == 10);

        // Süre
        rbT15.setChecked(time == 15);
        rbT20.setChecked(time == 20);
        rbT30.setChecked(time == 30);
        rbT45.setChecked(time == 45);
        rbT60.setChecked(time == 60);

        // Switchler
        switchSound.setChecked(soundOn);
        switchDarkMode.setChecked(darkMode);
    }

    private void saveSettings() {
        SharedPreferences.Editor ed = sp.edit();

        // Zorluk
        if (rbEasy.isChecked()) ed.putString("difficulty", "easy");
        else if (rbMedium.isChecked()) ed.putString("difficulty", "medium");
        else ed.putString("difficulty", "hard");

        // Soru sayısı
        ed.putInt(
                "question_count",
                rbQ5.isChecked() ? 5 :
                        rbQ7.isChecked() ? 7 : 10
        );

        // Süre
        ed.putInt(
                "time_per_turn",
                rbT15.isChecked() ? 15 :
                        rbT20.isChecked() ? 20 :
                                rbT30.isChecked() ? 30 :
                                        rbT45.isChecked() ? 45 : 60
        );

        // Ses & Dark Mode
        ed.putBoolean("sound_on", switchSound.isChecked());
        ed.putBoolean("dark_mode", switchDarkMode.isChecked());
        ed.apply();

        // 🌙 DARK MODE UYGULA
        AppCompatDelegate.setDefaultNightMode(
                switchDarkMode.isChecked()
                        ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_NO
        );

        Toast.makeText(this, "Ayarlar kaydedildi", Toast.LENGTH_SHORT).show();

        // Activity düzgün yenilensin
        recreate();
    }
}
