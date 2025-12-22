package com.example.mathquiz;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * OnlineMenuActivity - Online oyun modu seçim ekranı
 * Kullanıcı Host veya Client olarak oyuna katılabilir
 */
public class OnlineMenuActivity extends AppCompatActivity {

    private String playerName = "";
    private int playerAvatar = R.drawable.avatar1;

    private TextView tvTitle, tvSubtitle;
    private CardView cardHost, cardJoin;
    private MaterialButton btnHost, btnJoin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_menu);

        bindViews();
        animateEntrance();
        setupButtons();
    }

    private void bindViews() {
        tvTitle = findViewById(R.id.tvTitle);
        tvSubtitle = findViewById(R.id.tvSubtitle);
        cardHost = findViewById(R.id.cardHost);
        cardJoin = findViewById(R.id.cardJoin);
        btnHost = findViewById(R.id.btnHost);
        btnJoin = findViewById(R.id.btnJoin);
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

        // Subtitle fade in
        tvSubtitle.setAlpha(0f);
        tvSubtitle.animate()
                .alpha(1f)
                .setDuration(600)
                .setStartDelay(200)
                .start();

        // Host card from left
        cardHost.setTranslationX(-800f);
        cardHost.setAlpha(0f);
        cardHost.animate()
                .translationX(0f)
                .alpha(1f)
                .setDuration(600)
                .setStartDelay(400)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        // Join card from right
        cardJoin.setTranslationX(800f);
        cardJoin.setAlpha(0f);
        cardJoin.animate()
                .translationX(0f)
                .alpha(1f)
                .setDuration(600)
                .setStartDelay(600)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    private void setupButtons() {
        btnHost.setOnClickListener(v -> {
            animateButtonClick(cardHost);
            new Handler().postDelayed(() -> showNameDialog(true), 200);
        });

        btnJoin.setOnClickListener(v -> {
            animateButtonClick(cardJoin);
            new Handler().postDelayed(() -> showNameDialog(false), 200);
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

    private void showNameDialog(boolean isHost) {
        // Modern Material Design dialog
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_player_name, null);
        TextInputLayout inputLayout = dialogView.findViewById(R.id.inputLayout);
        TextInputEditText etName = dialogView.findViewById(R.id.etName);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(isHost ? "🎮 Oda Oluştur" : "🔗 Odaya Katıl")
                .setMessage("İsminizi girin:")
                .setView(dialogView)
                .setPositiveButton("Devam", null) // null to override
                .setNegativeButton("İptal", (d, which) -> d.dismiss())
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                playerName = etName.getText().toString().trim();

                if (playerName.isEmpty()) {
                    inputLayout.setError("İsim gerekli!");
                    return;
                }

                inputLayout.setError(null);
                dialog.dismiss();

                if (isHost) {
                    startHostActivity();
                } else {
                    startClientActivity();
                }
            });
        });

        dialog.show();
    }

    private void startHostActivity() {
        Intent intent = new Intent(this, HostActivity.class);
        intent.putExtra("playerName", playerName);
        intent.putExtra("playerAvatar", playerAvatar);
        startActivity(intent);
    }

    private void startClientActivity() {
        Intent intent = new Intent(this, ClientActivity.class);
        intent.putExtra("playerName", playerName);
        intent.putExtra("playerAvatar", playerAvatar);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}