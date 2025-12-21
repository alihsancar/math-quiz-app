package com.example.mathquiz;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

/**
 * OnlineMenuActivity - Online oyun modu seçim ekranı
 * Kullanıcı Host veya Client olarak oyuna katılabilir
 */
public class OnlineMenuActivity extends AppCompatActivity {

    private String playerName = "";
    private int playerAvatar = R.drawable.avatar1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_menu);

        Button btnHost = findViewById(R.id.btnHost);
        Button btnJoin = findViewById(R.id.btnJoin);

        btnHost.setOnClickListener(v -> {
            showNameDialog(true);
        });

        btnJoin.setOnClickListener(v -> {
            showNameDialog(false);
        });
    }

    private void showNameDialog(boolean isHost) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("İsminizi Girin");

        final EditText input = new EditText(this);
        input.setHint("İsim");
        input.setPadding(50, 30, 50, 30);
        builder.setView(input);

        builder.setPositiveButton("Tamam", (dialog, which) -> {
            playerName = input.getText().toString().trim();

            if (playerName.isEmpty()) {
                playerName = isHost ? "Host" : "Client";
            }

            if (isHost) {
                startHostActivity();
            } else {
                startClientActivity();
            }
        });

        builder.setNegativeButton("İptal", (dialog, which) -> dialog.cancel());

        builder.show();
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
}