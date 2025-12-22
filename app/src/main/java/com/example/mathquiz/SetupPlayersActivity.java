package com.example.mathquiz;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class SetupPlayersActivity extends AppCompatActivity {

    private TextInputEditText etP1Name, etP2Name;
    private MaterialButton btnStartGame;
    private CardView cardPlayer1, cardPlayer2;
    private TextView tvTitle;

    // Seçilen avatarların drawable id'si
    private int p1Avatar = R.drawable.avatar1;
    private int p2Avatar = R.drawable.avatar1;

    private ImageView[] p1Avatars;
    private ImageView[] p2Avatars;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_players);

        bindViews();
        setupAvatars();
        animateEntrance();

        btnStartGame.setOnClickListener(v -> {
            animateButtonClick();
            new Handler().postDelayed(this::startGame, 200);
        });
    }

    private void bindViews() {
        tvTitle = findViewById(R.id.tvTitle);

        // Player 1
        cardPlayer1 = findViewById(R.id.cardPlayer1);
        etP1Name = findViewById(R.id.etP1Name);

        // Player 2
        cardPlayer2 = findViewById(R.id.cardPlayer2);
        etP2Name = findViewById(R.id.etP2Name);

        // Button
        btnStartGame = findViewById(R.id.btnStartGame);

        // Oyuncu 1 avatar ImageView'leri
        ImageView p1a1 = findViewById(R.id.p1a1);
        ImageView p1a2 = findViewById(R.id.p1a2);
        ImageView p1a3 = findViewById(R.id.p1a3);
        ImageView p1a4 = findViewById(R.id.p1a4);

        // Oyuncu 2 avatar ImageView'leri
        ImageView p2a1 = findViewById(R.id.p2a1);
        ImageView p2a2 = findViewById(R.id.p2a2);
        ImageView p2a3 = findViewById(R.id.p2a3);
        ImageView p2a4 = findViewById(R.id.p2a4);

        p1Avatars = new ImageView[]{p1a1, p1a2, p1a3, p1a4};
        p2Avatars = new ImageView[]{p2a1, p2a2, p2a3, p2a4};
    }

    private void setupAvatars() {
        // Bu avatarların drawable id'leri
        int[] avatarIds = {
                R.drawable.avatar1,
                R.drawable.avatar2,
                R.drawable.avatar3,
                R.drawable.avatar4
        };

        // Her ImageView'a kendi avatar id'sini "tag" olarak bağla
        for (int i = 0; i < 4; i++) {
            p1Avatars[i].setTag(avatarIds[i]);
            p2Avatars[i].setTag(avatarIds[i]);
        }

        // Tıklama mantığını kur
        setupAvatarSelection(p1Avatars, true);
        setupAvatarSelection(p2Avatars, false);

        // Varsayılan seçim: ilk avatar
        p1Avatar = avatarIds[0];
        p2Avatar = avatarIds[0];
    }

    private void setupAvatarSelection(ImageView[] avatars, boolean isPlayer1) {
        for (ImageView iv : avatars) {
            iv.setOnClickListener(v -> {
                // Önce hepsini seçili değil yap
                for (ImageView other : avatars) {
                    other.setBackgroundResource(R.drawable.avatar_unselected);
                    other.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(200)
                            .start();
                }

                // Tıklananı seçili yap ve büyüt
                v.setBackgroundResource(R.drawable.avatar_selected);
                v.animate()
                        .scaleX(1.1f)
                        .scaleY(1.1f)
                        .setDuration(200)
                        .setInterpolator(new OvershootInterpolator())
                        .start();

                // Tag'den seçilen drawable id'yi al
                int selectedAvatar = (int) v.getTag();

                if (isPlayer1) {
                    p1Avatar = selectedAvatar;
                } else {
                    p2Avatar = selectedAvatar;
                }
            });
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

        // Player 1 card slide from left
        cardPlayer1.setTranslationX(-1000f);
        cardPlayer1.setAlpha(0f);
        cardPlayer1.animate()
                .translationX(0f)
                .alpha(1f)
                .setDuration(600)
                .setStartDelay(200)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        // Player 2 card slide from right
        cardPlayer2.setTranslationX(1000f);
        cardPlayer2.setAlpha(0f);
        cardPlayer2.animate()
                .translationX(0f)
                .alpha(1f)
                .setDuration(600)
                .setStartDelay(400)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        // Button bounce from bottom
        btnStartGame.setTranslationY(200f);
        btnStartGame.setAlpha(0f);
        btnStartGame.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(700)
                .setStartDelay(600)
                .setInterpolator(new OvershootInterpolator())
                .start();

        // Avatar'ları da sırayla göster
        for (int i = 0; i < p1Avatars.length; i++) {
            animateAvatar(p1Avatars[i], 300 + (i * 100));
            animateAvatar(p2Avatars[i], 500 + (i * 100));
        }
    }

    private void animateAvatar(ImageView avatar, long delay) {
        avatar.setScaleX(0f);
        avatar.setScaleY(0f);
        avatar.setAlpha(0f);
        avatar.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(400)
                .setStartDelay(delay)
                .setInterpolator(new OvershootInterpolator())
                .start();
    }

    private void animateButtonClick() {
        btnStartGame.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(() -> {
                    btnStartGame.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start();
                })
                .start();
    }

    private void startGame() {
        String p1 = etP1Name.getText().toString().trim();
        String p2 = etP2Name.getText().toString().trim();

        if (TextUtils.isEmpty(p1)) {
            p1 = "Oyuncu 1";
        }

        if (TextUtils.isEmpty(p2)) {
            p2 = "Oyuncu 2";
        }

        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("p1Name", p1);
        intent.putExtra("p2Name", p2);
        intent.putExtra("p1Avatar", p1Avatar);
        intent.putExtra("p2Avatar", p2Avatar);

        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        // Geri butonu animasyonlu
        finish();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}