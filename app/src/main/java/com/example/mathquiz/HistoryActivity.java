package com.example.mathquiz;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class HistoryActivity extends AppCompatActivity {

    private LinearLayout container;
    private TextView tvTitle, tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        tvTitle = findViewById(R.id.tvTitle);
        tvEmpty = findViewById(R.id.tvEmpty);
        container = findViewById(R.id.containerHistoryRows);

        animateTitle();
        loadHistory();
    }

    private void animateTitle() {
        tvTitle.setAlpha(0f);
        tvTitle.setTranslationY(-50f);
        tvTitle.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    private void loadHistory() {
        GameDatabaseHelper db = new GameDatabaseHelper(this);
        Cursor c = db.getAllGames();

        if (c.getCount() == 0) {
            // Boş durumu göster
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setAlpha(0f);
            tvEmpty.animate()
                    .alpha(1f)
                    .setDuration(600)
                    .setStartDelay(200)
                    .start();
            c.close();
            return;
        }

        tvEmpty.setVisibility(View.GONE);
        int delay = 0;

        while (c.moveToNext()) {
            String p1 = c.getString(c.getColumnIndexOrThrow("player1"));
            String p2 = c.getString(c.getColumnIndexOrThrow("player2"));
            int s1 = c.getInt(c.getColumnIndexOrThrow("score1"));
            int s2 = c.getInt(c.getColumnIndexOrThrow("score2"));
            String winner = c.getString(c.getColumnIndexOrThrow("winner"));
            String difficulty = c.getString(c.getColumnIndexOrThrow("difficulty"));

            // Create game card
            CardView card = createGameCard(p1, p2, s1, s2, winner, difficulty);
            container.addView(card);

            // Animate card entrance
            animateCard(card, delay);
            delay += 100;
        }

        c.close();
    }

    private CardView createGameCard(String p1, String p2, int s1, int s2, String winner, String difficulty) {
        // Create CardView
        CardView card = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 16);
        card.setLayoutParams(cardParams);
        card.setCardElevation(8f);
        card.setRadius(20f);
        card.setUseCompatPadding(true);

        // Main container
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(20, 20, 20, 20);
        mainLayout.setBackgroundResource(R.drawable.history_card_bg);

        // Header with difficulty badge
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setPadding(0, 0, 0, 16);

        TextView difficultyBadge = new TextView(this);
        difficultyBadge.setText(getDifficultyEmoji(difficulty) + " " + getDifficultyText(difficulty));
        difficultyBadge.setTextSize(14);
        difficultyBadge.setTextColor(0xFF1A1A2E);
        difficultyBadge.setPadding(16, 8, 16, 8);
        difficultyBadge.setBackgroundResource(R.drawable.difficulty_badge_bg);
        header.addView(difficultyBadge);

        mainLayout.addView(header);

        // Players row
        LinearLayout playersRow = new LinearLayout(this);
        playersRow.setOrientation(LinearLayout.HORIZONTAL);
        playersRow.setPadding(0, 0, 0, 12);

        // Player 1
        TextView tvP1 = new TextView(this);
        tvP1.setText(p1);
        tvP1.setTextSize(16);
        tvP1.setTextColor(0xFF1A1A2E);
        tvP1.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams p1Params = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
        );
        tvP1.setLayoutParams(p1Params);
        playersRow.addView(tvP1);

        // Score
        TextView tvScore = new TextView(this);
        tvScore.setText(s1 + " - " + s2);
        tvScore.setTextSize(18);
        tvScore.setTextColor(0xFF6A1B9A);
        tvScore.setTypeface(null, android.graphics.Typeface.BOLD);
        tvScore.setPadding(16, 0, 16, 0);
        playersRow.addView(tvScore);

        // Player 2
        TextView tvP2 = new TextView(this);
        tvP2.setText(p2);
        tvP2.setTextSize(16);
        tvP2.setTextColor(0xFF1A1A2E);
        tvP2.setTypeface(null, android.graphics.Typeface.BOLD);
        tvP2.setGravity(android.view.Gravity.END);
        LinearLayout.LayoutParams p2Params = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
        );
        tvP2.setLayoutParams(p2Params);
        playersRow.addView(tvP2);

        mainLayout.addView(playersRow);

        // Winner row
        LinearLayout winnerRow = new LinearLayout(this);
        winnerRow.setOrientation(LinearLayout.HORIZONTAL);
        winnerRow.setGravity(android.view.Gravity.CENTER);
        winnerRow.setPadding(0, 8, 0, 0);

        TextView tvWinnerLabel = new TextView(this);
        tvWinnerLabel.setText("🏆 Kazanan: ");
        tvWinnerLabel.setTextSize(14);
        tvWinnerLabel.setTextColor(0xFF757575);
        winnerRow.addView(tvWinnerLabel);

        TextView tvWinner = new TextView(this);
        tvWinner.setText(winner);
        tvWinner.setTextSize(14);
        tvWinner.setTextColor(0xFF27AE60);
        tvWinner.setTypeface(null, android.graphics.Typeface.BOLD);
        winnerRow.addView(tvWinner);

        mainLayout.addView(winnerRow);

        card.addView(mainLayout);
        return card;
    }

    private void animateCard(CardView card, int delay) {
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

    private String getDifficultyEmoji(String difficulty) {
        switch (difficulty.toLowerCase()) {
            case "easy": return "😊";
            case "medium": return "🤔";
            case "hard": return "🔥";
            default: return "🎯";
        }
    }

    private String getDifficultyText(String difficulty) {
        switch (difficulty.toLowerCase()) {
            case "easy": return "Kolay";
            case "medium": return "Orta";
            case "hard": return "Zor";
            default: return difficulty;
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}