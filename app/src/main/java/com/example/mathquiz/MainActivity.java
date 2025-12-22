package com.example.mathquiz;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;

/**
 * MainActivity - Ana menü ekranı
 * Offline, Online, Ayarlar ve Geçmiş seçenekleri sunar
 */
public class MainActivity extends AppCompatActivity {

    private ImageView imgLogo;
    private TextView tvTitle, tvSubtitle;
    private CardView cardOffline, cardOnline, cardSettings, cardHistory;
    private MaterialButton btnOffline, btnOnline, btnSettings, btnHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Dark mode ayarını uygula
        applyTheme();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindViews();
        animateEntrance();
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

    private void bindViews() {
        imgLogo = findViewById(R.id.imgLogo);
        tvTitle = findViewById(R.id.tvTitle);
        tvSubtitle = findViewById(R.id.tvSubtitle);

        cardOffline = findViewById(R.id.cardOffline);
        cardOnline = findViewById(R.id.cardOnline);
        cardSettings = findViewById(R.id.cardSettings);
        cardHistory = findViewById(R.id.cardHistory);

        btnOffline = findViewById(R.id.btnOffline);
        btnOnline = findViewById(R.id.btnOnline);
        btnSettings = findViewById(R.id.btnSettings);
        btnHistory = findViewById(R.id.btnHistory);
    }

    private void animateEntrance() {
        // Logo animasyonu - yukarıdan gelip bounce ile yerleşir
        imgLogo.setTranslationY(-500f);
        imgLogo.setAlpha(0f);
        imgLogo.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(800)
                .setInterpolator(new OvershootInterpolator(1.5f))
                .start();

        // Title fade in
        tvTitle.setAlpha(0f);
        tvTitle.animate()
                .alpha(1f)
                .setDuration(600)
                .setStartDelay(300)
                .start();

        // Subtitle fade in
        tvSubtitle.setAlpha(0f);
        tvSubtitle.animate()
                .alpha(1f)
                .setDuration(600)
                .setStartDelay(450)
                .start();

        // Butonlar sırayla soldan gelir
        animateCardEntrance(cardOffline, 600);
        animateCardEntrance(cardOnline, 700);
        animateCardEntrance(cardSettings, 800);
        animateCardEntrance(cardHistory, 900);

        // Floating animasyonu - logo sürekli yukarı aşağı hareket eder
        startFloatingAnimation();
    }

    private void animateCardEntrance(CardView card, long delay) {
        card.setTranslationX(-800f);
        card.setAlpha(0f);
        card.animate()
                .translationX(0f)
                .alpha(1f)
                .setDuration(600)
                .setStartDelay(delay)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    private void startFloatingAnimation() {
        new Handler().postDelayed(() -> {
            imgLogo.animate()
                    .translationYBy(-20f)
                    .setDuration(1500)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .withEndAction(() -> {
                        imgLogo.animate()
                                .translationYBy(20f)
                                .setDuration(1500)
                                .setInterpolator(new AccelerateDecelerateInterpolator())
                                .withEndAction(this::startFloatingAnimation)
                                .start();
                    })
                    .start();
        }, 1000);
    }

    private void initButtons() {
        // Offline oyun - Oyuncu ayarlama ekranına git
        btnOffline.setOnClickListener(v -> {
            animateButtonClick(cardOffline);
            new Handler().postDelayed(() ->
                    startActivity(new Intent(this, SetupPlayersActivity.class)), 200);
        });

        // Online oyun - Online menü ekranına git
        btnOnline.setOnClickListener(v -> {
            animateButtonClick(cardOnline);
            new Handler().postDelayed(() ->
                    startActivity(new Intent(this, OnlineMenuActivity.class)), 200);
        });

        // Ayarlar
        btnSettings.setOnClickListener(v -> {
            animateButtonClick(cardSettings);
            new Handler().postDelayed(() ->
                    startActivity(new Intent(this, SettingsActivity.class)), 200);
        });

        // Geçmiş
        btnHistory.setOnClickListener(v -> {
            animateButtonClick(cardHistory);
            new Handler().postDelayed(() ->
                    startActivity(new Intent(this, HistoryActivity.class)), 200);
        });
    }

    private void animateButtonClick(CardView card) {
        card.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(() -> {
                    card.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start();
                })
                .start();
    }
}