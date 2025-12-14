package com.example.mathquiz;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        String p1 = getIntent().getStringExtra("p1Name");
        String p2 = getIntent().getStringExtra("p2Name");
        int s1 = getIntent().getIntExtra("p1Score", 0);
        int s2 = getIntent().getIntExtra("p2Score", 0);

        TextView tvScores = findViewById(R.id.tvScores);
        TextView tvWinner = findViewById(R.id.tvWinner);

        tvScores.setText(p1 + " : " + s1 + "\n" + p2 + " : " + s2);

        String winner;
        if (s1 > s2) {
            winner = p1;
            tvWinner.setText("Kazanan: " + p1);
        } else if (s2 > s1) {
            winner = p2;
            tvWinner.setText("Kazanan: " + p2);
        } else {
            winner = "Berabere";
            tvWinner.setText("Berabere!");
        }

        // 🔵 ZORLUK BİLGİSİNİ OKU
        SharedPreferences sp = getSharedPreferences("game_settings", MODE_PRIVATE);
        String difficulty = sp.getString("difficulty", "easy");

        // Türkçe gösterim
        if (difficulty.equals("easy")) difficulty = "Kolay";
        else if (difficulty.equals("medium")) difficulty = "Orta";
        else difficulty = "Zor";

        // 🔴 SQLITE KAYDI (ZORLUK DA VAR)
        GameDatabaseHelper db = new GameDatabaseHelper(this);
        db.insertGame(p1, p2, s1, s2, winner, difficulty);
    }
}
