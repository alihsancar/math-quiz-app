package com.example.mathquiz;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SetupPlayersActivity extends AppCompatActivity {

    private EditText etP1Name, etP2Name;

    // Seçilen avatarların drawable id'si
    private int p1Avatar = R.drawable.avatar1;
    private int p2Avatar = R.drawable.avatar1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_players);

        // İsim inputları
        etP1Name = findViewById(R.id.etP1Name);
        etP2Name = findViewById(R.id.etP2Name);

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

        // Kolay yönetmek için diziye koyuyoruz
        ImageView[] p1Avatars = {p1a1, p1a2, p1a3, p1a4};
        ImageView[] p2Avatars = {p2a1, p2a2, p2a3, p2a4};

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

        // Başlat butonu
        Button btnStart = findViewById(R.id.btnStartGame);
        btnStart.setOnClickListener(v -> startGame());
    }

    private void setupAvatarSelection(ImageView[] avatars, boolean isPlayer1) {
        for (ImageView iv : avatars) {
            iv.setOnClickListener(v -> {

                // Önce hepsini seçili değil yap
                for (ImageView other : avatars) {
                    other.setBackgroundResource(R.drawable.avatar_unselected);
                }

                // Tıklananı seçili yap
                v.setBackgroundResource(R.drawable.avatar_selected);

                // Tag’den seçilen drawable id’yi al
                int selectedAvatar = (int) v.getTag();

                if (isPlayer1) {
                    p1Avatar = selectedAvatar;
                } else {
                    p2Avatar = selectedAvatar;
                }
            });
        }
    }

    private void startGame() {
        String p1 = etP1Name.getText().toString().trim();
        String p2 = etP2Name.getText().toString().trim();

        if (TextUtils.isEmpty(p1) || TextUtils.isEmpty(p2)) {
            Toast.makeText(this, "İki oyuncunun da ismini gir", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("p1Name", p1);
        intent.putExtra("p2Name", p2);
        intent.putExtra("p1Avatar", p1Avatar);
        intent.putExtra("p2Avatar", p2Avatar);

        startActivity(intent);
        finish();
    }
}
