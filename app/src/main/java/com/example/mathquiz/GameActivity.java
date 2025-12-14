package com.example.mathquiz;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity {

    TextView tvTurnTop, tvTurnBottom;
    TextView tvP1Name, tvP2Name, tvP1Score, tvP2Score;
    TextView tvQuestion, tvTimer, tvResultCenter, tvScoreAnim;
    ImageView imgP1, imgP2;
    EditText etAnswer;
    Button btnSubmit;
    LinearLayout layoutP1, layoutP2;

    String p1Name, p2Name, difficulty;
    int p1Avatar, p2Avatar;
    int p1Score = 0, p2Score = 0;

    int timePerTurn, totalTurns, turnIndex = 0;

    Question currentQuestion;
    CountDownTimer timer;
    ObjectAnimator blink;
    Handler handler = new Handler(Looper.getMainLooper());

    MediaPlayer mpCorrect, mpWrong, mpTimeout;
    boolean soundOn = true;
    boolean isGameActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        bindViews();
        readIntent();
        readSettings();

        mpCorrect = MediaPlayer.create(this, R.raw.correct);
        mpWrong   = MediaPlayer.create(this, R.raw.wrong);
        mpTimeout = MediaPlayer.create(this, R.raw.timeout);

        startTurn();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isGameActive = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isGameActive = false;
        cancelTimer();
    }

    @Override
    protected void onStop() {
        super.onStop();
        cancelTimer();
    }

    private void bindViews() {
        tvTurnTop = findViewById(R.id.tvTurnTop);
        tvTurnBottom = findViewById(R.id.tvTurnBottom);
        tvP1Name = findViewById(R.id.tvP1Name);
        tvP2Name = findViewById(R.id.tvP2Name);
        tvP1Score = findViewById(R.id.tvP1Score);
        tvP2Score = findViewById(R.id.tvP2Score);
        tvQuestion = findViewById(R.id.tvQuestion);
        tvTimer = findViewById(R.id.tvTimer);
        tvResultCenter = findViewById(R.id.tvResultCenter);
        tvScoreAnim = findViewById(R.id.tvScoreAnim);
        imgP1 = findViewById(R.id.imgP1);
        imgP2 = findViewById(R.id.imgP2);
        etAnswer = findViewById(R.id.etAnswer);
        btnSubmit = findViewById(R.id.btnSubmit);
        layoutP1 = findViewById(R.id.layoutP1);
        layoutP2 = findViewById(R.id.layoutP2);

        btnSubmit.setOnClickListener(v -> submit());
    }

    private void readIntent() {
        p1Name = getIntent().getStringExtra("p1Name");
        p2Name = getIntent().getStringExtra("p2Name");
        p1Avatar = getIntent().getIntExtra("p1Avatar", R.drawable.avatar1);
        p2Avatar = getIntent().getIntExtra("p2Avatar", R.drawable.avatar1);

        tvP1Name.setText(p1Name);
        tvP2Name.setText(p2Name);
        imgP1.setImageResource(p1Avatar);
        imgP2.setImageResource(p2Avatar);
    }

    private void readSettings() {
        SharedPreferences sp = getSharedPreferences("game_settings", MODE_PRIVATE);
        difficulty = sp.getString("difficulty", "easy");
        int questionCount = sp.getInt("question_count", 5);
        timePerTurn = sp.getInt("time_per_turn", 30);
        totalTurns = questionCount * 2;
        soundOn = sp.getBoolean("sound_on", true);
    }

    private void startTurn() {
        if (turnIndex >= totalTurns) {
            endGame();
            return;
        }

        cancelTimer();
        cancelBlink();
        tvResultCenter.setVisibility(View.GONE);

        boolean isP1 = (turnIndex % 2 == 0);

        if (isP1) {
            tvTurnTop.setVisibility(View.VISIBLE);
            tvTurnBottom.setVisibility(View.GONE);
            tvTurnTop.setText(p1Name + " - Sıra Sizde");
            startBlink(tvTurnTop);
        } else {
            tvTurnTop.setVisibility(View.GONE);
            tvTurnBottom.setVisibility(View.VISIBLE);
            tvTurnBottom.setText(p2Name + " - Sıra Sizde");
            startBlink(tvTurnBottom);
        }

        layoutP1.setAlpha(isP1 ? 1f : 0.4f);
        layoutP2.setAlpha(isP1 ? 0.4f : 1f);

        currentQuestion = QuestionGenerator.generate(difficulty);
        tvQuestion.setText(currentQuestion.text);
        etAnswer.setText("");

        startTimer();
    }

    private void startBlink(TextView target) {
        blink = ObjectAnimator.ofFloat(target, "alpha", 1f, 0.3f, 1f);
        blink.setDuration(600);
        blink.setRepeatCount(ObjectAnimator.INFINITE);
        blink.start();
    }

    private void startTimer() {
        tvTimer.setText("Süre: " + timePerTurn);

        timer = new CountDownTimer(timePerTurn * 1000L, 1000) {

            boolean warningPlayed = false;

            @Override
            public void onTick(long ms) {
                long secondsLeft = ms / 1000;
                tvTimer.setText("Süre: " + secondsLeft);

                if (soundOn && isGameActive && secondsLeft == 3 && !warningPlayed) {
                    if (mpTimeout != null && !mpTimeout.isPlaying()) {
                        mpTimeout.start();
                        warningPlayed = true;
                    }
                }
            }

            @Override
            public void onFinish() {
                if (isGameActive) {
                    showCenterText("SÜRE BİTTİ", android.R.color.holo_orange_dark);
                    handler.postDelayed(GameActivity.this::nextTurn, 2000);
                }
            }
        }.start();
    }

    private void submit() {
        cancelTimer();
        boolean correct = false;

        try {
            int user = Integer.parseInt(etAnswer.getText().toString());
            correct = user == currentQuestion.answer;
        } catch (Exception ignored) {}

        if (correct) {
            if (soundOn && isGameActive && mpCorrect != null && !mpCorrect.isPlaying())
                mpCorrect.start();

            if (turnIndex % 2 == 0) {
                p1Score += 10;
                tvP1Score.setText(String.valueOf(p1Score));
                playScoreAnimation(tvP1Score);
            } else {
                p2Score += 10;
                tvP2Score.setText(String.valueOf(p2Score));
                playScoreAnimation(tvP2Score);
            }

            showCenterText("DOĞRU", android.R.color.holo_green_dark);
        } else {
            if (soundOn && isGameActive && mpWrong != null && !mpWrong.isPlaying())
                mpWrong.start();
            showCenterText("YANLIŞ", android.R.color.holo_red_dark);
        }

        handler.postDelayed(this::nextTurn, 2000);
    }

    private void playScoreAnimation(View targetScore) {
        tvScoreAnim.setVisibility(View.VISIBLE);
        tvScoreAnim.setAlpha(1f);

        float startX = tvQuestion.getX() + tvQuestion.getWidth() / 2f;
        float startY = tvQuestion.getY();

        tvScoreAnim.setX(startX);
        tvScoreAnim.setY(startY);

        tvScoreAnim.animate()
                .x(targetScore.getX())
                .y(targetScore.getY())
                .alpha(0f)
                .setDuration(700)
                .withEndAction(() -> tvScoreAnim.setVisibility(View.GONE))
                .start();
    }

    private void showCenterText(String text, int color) {
        tvResultCenter.setText(text);
        tvResultCenter.setTextColor(getResources().getColor(color));
        tvResultCenter.setVisibility(View.VISIBLE);
    }

    private void nextTurn() {
        cancelTimer();
        cancelBlink();
        turnIndex++;
        startTurn();
    }

    private void endGame() {
        Intent i = new Intent(this, ResultActivity.class);
        i.putExtra("p1Name", p1Name);
        i.putExtra("p2Name", p2Name);
        i.putExtra("p1Score", p1Score);
        i.putExtra("p2Score", p2Score);
        startActivity(i);
        finish();
    }

    private void cancelTimer() {
        if (timer != null) timer.cancel();
        timer = null;
    }

    private void cancelBlink() {
        if (blink != null) blink.cancel();
        blink = null;
        tvTurnTop.setAlpha(1f);
        tvTurnBottom.setAlpha(1f);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelTimer();
        if (mpCorrect != null) mpCorrect.release();
        if (mpWrong != null) mpWrong.release();
        if (mpTimeout != null) mpTimeout.release();
    }
}
