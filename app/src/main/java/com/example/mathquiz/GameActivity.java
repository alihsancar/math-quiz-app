package com.example.mathquiz;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class GameActivity extends AppCompatActivity {

    private static final String TAG = "GameActivity";

    // UI Elemanları
    private TextView tvTurnTop, tvTurnBottom;
    private TextView tvP1Name, tvP2Name, tvP1Score, tvP2Score;
    private TextView tvQuestion, tvTimer, tvResultCenter, tvScoreAnim;
    private ImageView imgP1, imgP2;
    private EditText etAnswer;
    private Button btnSubmit;
    private LinearLayout layoutP1, layoutP2;

    // Oyuncu Bilgileri
    private String p1Name, p2Name, difficulty;
    private int p1Avatar, p2Avatar;
    private int p1Score = 0, p2Score = 0;

    // Oyun Durumu
    private int timePerTurn, totalTurns, turnIndex = 0;
    private int questionCount;
    private Question currentQuestion;
    private CountDownTimer timer;
    private ObjectAnimator blink;
    private Handler handler = new Handler(Looper.getMainLooper());

    // Ses
    private MediaPlayer mpCorrect, mpWrong, mpTimeout;
    private boolean soundOn = true;

    // Online Bayrakları
    private boolean isOnline = false;
    private boolean isHost = false;
    private boolean isMyTurn = false;
    private boolean answerSubmitted = false;

    // Activity durumu
    private volatile boolean isDestroyed = false;
    private volatile boolean isFinishing = false;
    private volatile boolean gameEnded = false;

    // Timer kilidi
    private final Object timerLock = new Object();
    private volatile boolean timerRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        bindViews();
        readIntent();
        readSettings();
        initSounds();

        if (isOnline) {
            initOnlineGame();
        } else {
            startTurn();
        }
    }

    private void initSounds() {
        try {
            mpCorrect = MediaPlayer.create(this, R.raw.correct);
            mpWrong = MediaPlayer.create(this, R.raw.wrong);
            mpTimeout = MediaPlayer.create(this, R.raw.timeout);
        } catch (Exception e) {
            Log.e(TAG, "Ses dosyaları yüklenemedi: " + e.getMessage());
        }
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
        Intent intent = getIntent();

        isOnline = intent.getBooleanExtra("isOnline", false);
        isHost = intent.getBooleanExtra("isHost", false);

        p1Name = intent.getStringExtra("p1Name");
        p2Name = intent.getStringExtra("p2Name");
        p1Avatar = intent.getIntExtra("p1Avatar", R.drawable.avatar1);
        p2Avatar = intent.getIntExtra("p2Avatar", R.drawable.avatar1);

        if (p1Name == null || p1Name.isEmpty()) p1Name = isHost ? "Sen" : "Rakip";
        if (p2Name == null || p2Name.isEmpty()) p2Name = isHost ? "Rakip" : "Sen";

        tvP1Name.setText(p1Name);
        tvP2Name.setText(p2Name);
        imgP1.setImageResource(p1Avatar);
        imgP2.setImageResource(p2Avatar);

        Log.d(TAG, "isOnline=" + isOnline + ", isHost=" + isHost);
    }

    private void readSettings() {
        SharedPreferences sp = getSharedPreferences("game_settings", MODE_PRIVATE);
        difficulty = sp.getString("difficulty", "easy");
        questionCount = sp.getInt("question_count", 5);
        timePerTurn = sp.getInt("time_per_turn", 30);
        totalTurns = questionCount * 2;
        soundOn = sp.getBoolean("sound_on", true);

        Intent intent = getIntent();
        if (intent.hasExtra("difficulty")) {
            difficulty = intent.getStringExtra("difficulty");
        }
        if (intent.hasExtra("questionCount")) {
            questionCount = intent.getIntExtra("questionCount", 5);
            totalTurns = questionCount * 2;
        }
        if (intent.hasExtra("timePerTurn")) {
            timePerTurn = intent.getIntExtra("timePerTurn", 30);
        }

        Log.d(TAG, "totalTurns=" + totalTurns + " (questionCount=" + questionCount + ")");
    }

    // ==================== ONLINE OYUN ====================

    private void initOnlineGame() {
        Log.d(TAG, "Online oyun başlatılıyor, isHost=" + isHost);

        SocketManager.setListener(this::handleSocketMessage);

        SocketManager.setConnectionListener(() -> {
            Log.d(TAG, "ConnectionListener tetiklendi!");
            if (!isDestroyed && !isFinishing && !gameEnded) {
                runOnUiThread(this::showConnectionLostDialog);
            }
        });

        if (isHost) {
            handler.postDelayed(this::startTurn, 1000);
        } else {
            isMyTurn = false;
            tvQuestion.setText("Oyun başlıyor...");
            tvTimer.setText("Bekleniyor...");
            setInputEnabled(false);
        }
    }

    private void handleSocketMessage(JSONObject message) {
        if (isDestroyed || isFinishing) return;

        try {
            String type = message.getString("type");
            Log.d(TAG, "*** MESAJ ALINDI: " + type + " ***");

            switch (type) {
                case MessageType.QUESTION:
                    handleQuestionMessage(message);
                    break;
                case MessageType.ANSWER:
                    handleAnswerMessage(message);
                    break;
                case MessageType.ANSWER_RESULT:
                    handleAnswerResultMessage(message);
                    break;
                case MessageType.NEXT_TURN:
                    handleNextTurnMessage(message);
                    break;
                case MessageType.END_GAME:
                    handleEndGameMessage(message);
                    break;
                case MessageType.PLAYER_LEFT:
                    handlePlayerLeftMessage(message);
                    break;
            }
        } catch (JSONException e) {
            Log.e(TAG, "Mesaj parse hatası: " + e.getMessage());
        }
    }

    private void handlePlayerLeftMessage(JSONObject message) {
        Log.d(TAG, "*** PLAYER_LEFT alındı ***");
        if (!isDestroyed && !isFinishing && !gameEnded) {
            gameEnded = true;
            runOnUiThread(() -> {
                cancelTimer();
                showPlayerLeftDialog();
            });
        }
    }

    private void showPlayerLeftDialog() {
        if (isDestroyed || isFinishing) return;

        isFinishing = true;
        cancelTimer();

        try {
            new AlertDialog.Builder(this)
                    .setTitle("Oyun Bitti")
                    .setMessage("Rakip oyundan ayrıldı.")
                    .setPositiveButton("Tamam", (dialog, which) -> safeFinish())
                    .setCancelable(false)
                    .show();
        } catch (Exception e) {
            safeFinish();
        }
    }

    private void handleQuestionMessage(JSONObject message) {
        if (gameEnded) return;

        try {
            JSONObject data = message.getJSONObject("data");
            currentQuestion = Question.fromJson(data);
            turnIndex = message.optInt("turnIndex", turnIndex);

            // Host ayarlarını al
            if (message.has("timePerTurn")) {
                timePerTurn = message.getInt("timePerTurn");
            }
            if (message.has("totalTurns")) {
                totalTurns = message.getInt("totalTurns");
            }

            boolean isP1Turn = (turnIndex % 2 == 0);
            isMyTurn = !isP1Turn; // Client için

            Log.d(TAG, "Soru alındı: turnIndex=" + turnIndex + ", totalTurns=" + totalTurns + ", isMyTurn=" + isMyTurn);

            if (currentQuestion != null && !isDestroyed) {
                runOnUiThread(() -> {
                    if (isDestroyed || gameEnded) return;

                    answerSubmitted = false;
                    tvQuestion.setText(currentQuestion.text);
                    etAnswer.setText("");
                    tvResultCenter.setVisibility(View.GONE);

                    updateTurnUI();
                    setInputEnabled(isMyTurn);

                    cancelTimer();
                    startTimer();
                });
            }
        } catch (JSONException e) {
            Log.e(TAG, "Soru parse hatası: " + e.getMessage());
        }
    }

    private void handleAnswerMessage(JSONObject message) {
        if (!isHost || gameEnded) return;

        try {
            int answer = message.getInt("answer");
            boolean isCorrect = (currentQuestion != null && answer == currentQuestion.answer);

            if (isCorrect) {
                p2Score += 10;
            }

            sendAnswerResult(isCorrect);

            // Bu son tur mu kontrol et (turnIndex 0'dan başlıyor, totalTurns-1'e kadar)
            final boolean isLastTurn = (turnIndex >= totalTurns - 1);
            Log.d(TAG, "Host: Cevap alındı, turnIndex=" + turnIndex + ", totalTurns=" + totalTurns + ", isLastTurn=" + isLastTurn);

            runOnUiThread(() -> {
                if (isDestroyed || gameEnded) return;

                cancelTimer();
                updateScoreUI();

                if (isCorrect) {
                    showCenterText("Rakip DOĞRU!", android.R.color.holo_green_dark);
                    playSound(mpCorrect);
                } else {
                    showCenterText("Rakip YANLIŞ! Cevap: " + currentQuestion.answer, android.R.color.holo_red_dark);
                    playSound(mpWrong);
                }

                // Son tur mu?
                if (isLastTurn) {
                    Log.d(TAG, "*** SON TUR - OYUN BİTİYOR ***");
                    handler.postDelayed(this::finishGame, 2000);
                } else {
                    handler.postDelayed(this::nextTurn, 2500);
                }
            });

        } catch (JSONException e) {
            Log.e(TAG, "Cevap parse hatası: " + e.getMessage());
        }
    }

    private void handleAnswerResultMessage(JSONObject message) {
        if (isHost || gameEnded) return;

        try {
            boolean isCorrect = message.getBoolean("correct");
            p1Score = message.optInt("p1Score", p1Score);
            p2Score = message.optInt("p2Score", p2Score);

            runOnUiThread(() -> {
                if (isDestroyed || gameEnded) return;

                cancelTimer();
                updateScoreUI();

                if (isCorrect) {
                    showCenterText("DOĞRU! +10", android.R.color.holo_green_dark);
                    playSound(mpCorrect);
                } else {
                    showCenterText("YANLIŞ! Cevap: " + (currentQuestion != null ? currentQuestion.answer : "?"), android.R.color.holo_red_dark);
                    playSound(mpWrong);
                }

                setInputEnabled(false);

                // Client: Sonraki soru veya END_GAME bekle
                tvQuestion.setText("Bekleniyor...");
            });

        } catch (JSONException e) {
            Log.e(TAG, "Sonuç parse hatası: " + e.getMessage());
        }
    }

    private void handleNextTurnMessage(JSONObject message) {
        if (gameEnded) return;

        try {
            turnIndex = message.optInt("turnIndex", turnIndex);
            p1Score = message.optInt("p1Score", p1Score);
            p2Score = message.optInt("p2Score", p2Score);

            Log.d(TAG, "NEXT_TURN alındı: turnIndex=" + turnIndex);

            runOnUiThread(() -> {
                if (isDestroyed || gameEnded) return;

                cancelTimer();
                updateScoreUI();
                tvResultCenter.setVisibility(View.GONE);
                tvQuestion.setText("Sıradaki soru bekleniyor...");
                tvTimer.setText("Bekleniyor...");
                setInputEnabled(false);
            });
        } catch (Exception e) {
            Log.e(TAG, "Sonraki tur hatası: " + e.getMessage());
        }
    }

    private void handleEndGameMessage(JSONObject message) {
        Log.d(TAG, "########## END_GAME ALINDI ##########");

        if (gameEnded) {
            Log.d(TAG, "Oyun zaten bitti, ignore");
            return;
        }

        gameEnded = true;
        cancelTimer();

        try {
            p1Score = message.optInt("p1Score", p1Score);
            p2Score = message.optInt("p2Score", p2Score);
            Log.d(TAG, "Final skorlar: p1=" + p1Score + ", p2=" + p2Score);
        } catch (Exception e) {
            Log.e(TAG, "Skor parse hatası: " + e.getMessage());
        }

        // HEMEN ResultActivity'ye git
        runOnUiThread(this::goToResult);
    }

    // ==================== MESAJ GÖNDERME ====================

    private void sendQuestion(Question q) {
        try {
            JSONObject msg = new JSONObject();
            msg.put("type", MessageType.QUESTION);
            msg.put("data", q.toJson());
            msg.put("turnIndex", turnIndex);
            msg.put("totalTurns", totalTurns);
            msg.put("timePerTurn", timePerTurn);
            SocketManager.send(msg);
            Log.d(TAG, "Soru gönderildi: turnIndex=" + turnIndex);
        } catch (JSONException e) {
            Log.e(TAG, "Soru gönderme hatası: " + e.getMessage());
        }
    }

    private void sendAnswer(int answer) {
        try {
            JSONObject msg = new JSONObject();
            msg.put("type", MessageType.ANSWER);
            msg.put("answer", answer);
            msg.put("turnIndex", turnIndex);
            SocketManager.send(msg);
            Log.d(TAG, "Cevap gönderildi: " + answer);
        } catch (JSONException e) {
            Log.e(TAG, "Cevap gönderme hatası: " + e.getMessage());
        }
    }

    private void sendAnswerResult(boolean correct) {
        try {
            JSONObject msg = new JSONObject();
            msg.put("type", MessageType.ANSWER_RESULT);
            msg.put("correct", correct);
            msg.put("p1Score", p1Score);
            msg.put("p2Score", p2Score);
            SocketManager.send(msg);
        } catch (JSONException e) {
            Log.e(TAG, "Sonuç gönderme hatası: " + e.getMessage());
        }
    }

    private void sendNextTurn() {
        try {
            JSONObject msg = new JSONObject();
            msg.put("type", MessageType.NEXT_TURN);
            msg.put("turnIndex", turnIndex);
            msg.put("p1Score", p1Score);
            msg.put("p2Score", p2Score);
            SocketManager.send(msg);
            Log.d(TAG, "NEXT_TURN gönderildi: turnIndex=" + turnIndex);
        } catch (JSONException e) {
            Log.e(TAG, "Sonraki tur gönderme hatası: " + e.getMessage());
        }
    }

    /**
     * Oyunu bitirir - END_GAME gönderir ve ResultActivity'ye gider
     */
    private void finishGame() {
        if (gameEnded) return;

        Log.d(TAG, "########## finishGame() ÇAĞRILDI ##########");
        gameEnded = true;
        cancelTimer();

        if (isOnline && isHost) {
            // END_GAME mesajını ayrı thread'de gönder
            new Thread(() -> {
                try {
                    JSONObject msg = new JSONObject();
                    msg.put("type", MessageType.END_GAME);
                    msg.put("p1Score", p1Score);
                    msg.put("p2Score", p2Score);

                    Log.d(TAG, "END_GAME gönderiliyor...");
                    SocketManager.sendSync(msg);
                    Log.d(TAG, "END_GAME gönderildi!");

                    // Mesajın ulaşması için bekle
                    Thread.sleep(500);

                } catch (Exception e) {
                    Log.e(TAG, "END_GAME gönderme hatası: " + e.getMessage());
                }

                // UI thread'de ResultActivity'ye git
                runOnUiThread(this::goToResult);
            }).start();
        } else {
            // Offline veya Client (Client buraya gelmemeli)
            goToResult();
        }
    }

    private void sendPlayerLeft() {
        Log.d(TAG, "PLAYER_LEFT gönderiliyor...");
        try {
            JSONObject msg = new JSONObject();
            msg.put("type", MessageType.PLAYER_LEFT);
            msg.put("isHost", isHost);
            SocketManager.sendSync(msg);
            Thread.sleep(200);
            Log.d(TAG, "PLAYER_LEFT gönderildi");
        } catch (Exception e) {
            Log.e(TAG, "Player left gönderme hatası: " + e.getMessage());
        }
    }

    // ==================== TUR YÖNETİMİ ====================

    private void startTurn() {
        if (isDestroyed || isFinishing || gameEnded) return;

        Log.d(TAG, "startTurn: turnIndex=" + turnIndex + ", totalTurns=" + totalTurns);

        // Oyun bitti mi kontrol et
        if (turnIndex >= totalTurns) {
            Log.d(TAG, "*** OYUN BİTTİ (startTurn) ***");
            finishGame();
            return;
        }

        cancelTimer();
        tvResultCenter.setVisibility(View.GONE);
        answerSubmitted = false;

        boolean isP1Turn = (turnIndex % 2 == 0);

        if (!isOnline) {
            // OFFLINE
            isMyTurn = true;
            currentQuestion = QuestionGenerator.generate(difficulty);
            tvQuestion.setText(currentQuestion.text);
            etAnswer.setText("");
            updateTurnUI();
            setInputEnabled(true);
            startTimer();

        } else if (isHost) {
            // HOST
            isMyTurn = isP1Turn;
            currentQuestion = QuestionGenerator.generate(difficulty);
            tvQuestion.setText(currentQuestion.text);
            etAnswer.setText("");
            sendQuestion(currentQuestion);
            updateTurnUI();
            setInputEnabled(isMyTurn);
            startTimer();
        }
        // CLIENT: handleQuestionMessage'da işlenecek
    }

    private void setInputEnabled(boolean enabled) {
        etAnswer.setEnabled(enabled);
        btnSubmit.setEnabled(enabled);

        if (enabled) {
            etAnswer.setHint("Cevabınızı yazın");
            etAnswer.setAlpha(1.0f);
            btnSubmit.setAlpha(1.0f);
        } else {
            etAnswer.setHint("Rakibin sırası...");
            etAnswer.setAlpha(0.5f);
            btnSubmit.setAlpha(0.5f);
        }
    }

    private void updateTurnUI() {
        if (isDestroyed) return;

        boolean isP1Turn = (turnIndex % 2 == 0);

        layoutP1.setAlpha(isP1Turn ? 1f : 0.4f);
        layoutP2.setAlpha(isP1Turn ? 0.4f : 1f);

        int currentTurnDisplay = (turnIndex / 2) + 1;
        int totalTurnsDisplay = totalTurns / 2;
        String turnText = "Tur " + currentTurnDisplay + "/" + totalTurnsDisplay;
        tvTurnBottom.setText(turnText);
        tvTurnBottom.setVisibility(View.VISIBLE);

        if (isOnline) {
            if (isMyTurn) {
                tvTurnTop.setText("SENİN SIRAN");
                tvTurnTop.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
            } else {
                tvTurnTop.setText("RAKİBİN SIRASI");
                tvTurnTop.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
            }
        } else {
            tvTurnTop.setText(isP1Turn ? p1Name + " oynuyor" : p2Name + " oynuyor");
            tvTurnTop.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
        }
    }

    private void startTimer() {
        synchronized (timerLock) {
            if (timer != null) {
                timer.cancel();
                timer = null;
            }

            if (isDestroyed || gameEnded) return;

            timerRunning = true;
            tvTimer.setText("Süre: " + timePerTurn);
            tvTimer.setTextColor(getResources().getColor(android.R.color.black));

            timer = new CountDownTimer(timePerTurn * 1000L, 1000) {
                @Override
                public void onTick(long ms) {
                    if (!timerRunning || isDestroyed || gameEnded) {
                        cancel();
                        return;
                    }

                    int secondsLeft = (int) (ms / 1000);
                    tvTimer.setText("Süre: " + secondsLeft);

                    if (secondsLeft <= 5) {
                        tvTimer.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    }
                }

                @Override
                public void onFinish() {
                    if (!timerRunning || isDestroyed || gameEnded) return;
                    timerRunning = false;
                    tvTimer.setText("Süre: 0");
                    handleTimeout();
                }
            };

            timer.start();
        }
    }

    private void cancelTimer() {
        synchronized (timerLock) {
            timerRunning = false;
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
        }
    }

    private void handleTimeout() {
        if (answerSubmitted || isDestroyed || gameEnded) return;

        answerSubmitted = true;
        playSound(mpTimeout);
        showCenterText("SÜRE BİTTİ!", android.R.color.holo_orange_dark);
        setInputEnabled(false);

        // Son tur mu kontrol et
        final boolean isLastTurn = (turnIndex >= totalTurns - 1);

        if (isOnline) {
            if (isMyTurn && !isHost) {
                // Client timeout
                sendAnswer(-99999);
            } else if (isMyTurn && isHost) {
                // Host timeout
                if (isLastTurn) {
                    handler.postDelayed(this::finishGame, 2000);
                } else {
                    handler.postDelayed(this::nextTurn, 2000);
                }
            }
        } else {
            // Offline
            if (isLastTurn) {
                handler.postDelayed(this::finishGame, 2000);
            } else {
                handler.postDelayed(this::nextTurn, 2000);
            }
        }
    }

    private void submit() {
        if (!isMyTurn && isOnline) {
            Toast.makeText(this, "Şu an senin sıran değil!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (answerSubmitted || gameEnded) return;

        String answerText = etAnswer.getText().toString().trim();
        if (answerText.isEmpty()) {
            Toast.makeText(this, "Bir cevap girin!", Toast.LENGTH_SHORT).show();
            return;
        }

        answerSubmitted = true;
        cancelTimer();
        setInputEnabled(false);

        int userAnswer;
        try {
            userAnswer = Integer.parseInt(answerText);
        } catch (NumberFormatException e) {
            userAnswer = -99999;
        }

        if (isOnline && !isHost) {
            // CLIENT
            sendAnswer(userAnswer);
            return;
        }

        // OFFLINE veya HOST kendi cevabı
        boolean correct = (currentQuestion != null && userAnswer == currentQuestion.answer);

        if (correct) {
            boolean isP1Turn = (turnIndex % 2 == 0);
            if (!isOnline) {
                if (isP1Turn) p1Score += 10;
                else p2Score += 10;
            } else {
                p1Score += 10; // Host = P1
            }
            showCenterText("DOĞRU! +10", android.R.color.holo_green_dark);
            playSound(mpCorrect);
        } else {
            showCenterText("YANLIŞ! Cevap: " + (currentQuestion != null ? currentQuestion.answer : "?"), android.R.color.holo_red_dark);
            playSound(mpWrong);
        }

        updateScoreUI();

        // Son tur mu kontrol et
        final boolean isLastTurn = (turnIndex >= totalTurns - 1);
        Log.d(TAG, "Host cevap verdi: turnIndex=" + turnIndex + ", isLastTurn=" + isLastTurn);

        if (isLastTurn && isOnline) {
            // Online son tur - oyunu bitir
            handler.postDelayed(this::finishGame, 2000);
        } else if (isLastTurn && !isOnline) {
            // Offline son tur
            handler.postDelayed(this::finishGame, 2000);
        } else {
            handler.postDelayed(this::nextTurn, 2000);
        }
    }

    private void nextTurn() {
        if (isDestroyed || isFinishing || gameEnded) return;

        turnIndex++;
        Log.d(TAG, "nextTurn: yeni turnIndex=" + turnIndex);

        if (isOnline && isHost) {
            sendNextTurn();
        }

        startTurn();
    }

    private void updateScoreUI() {
        if (isDestroyed) return;
        tvP1Score.setText(String.valueOf(p1Score));
        tvP2Score.setText(String.valueOf(p2Score));
    }

    private void showCenterText(String text, int colorRes) {
        if (isDestroyed) return;
        tvResultCenter.setText(text);
        tvResultCenter.setTextColor(getResources().getColor(colorRes));
        tvResultCenter.setVisibility(View.VISIBLE);
    }

    private void playSound(MediaPlayer mp) {
        if (mp != null && !isDestroyed) {
            try {
                mp.seekTo(0);
                mp.start();
            } catch (Exception e) {
                Log.e(TAG, "Ses hatası: " + e.getMessage());
            }
        }
    }

    private void goToResult() {
        if (isFinishing) {
            Log.d(TAG, "goToResult: zaten finishing");
            return;
        }

        Log.d(TAG, "########## goToResult ##########");
        Log.d(TAG, "Skorlar: " + p1Name + "=" + p1Score + ", " + p2Name + "=" + p2Score);

        isFinishing = true;
        gameEnded = true;
        cancelTimer();
        handler.removeCallbacksAndMessages(null);

        // Socket'i temizle
        if (isOnline) {
            try {
                SocketManager.setListener(null);
                SocketManager.setConnectionListener(null);
                SocketManager.stop();
                SocketHolder.close();
            } catch (Exception e) {
                Log.e(TAG, "Socket kapatma hatası: " + e.getMessage());
            }
        }

        // ResultActivity'ye git
        try {
            Intent intent = new Intent(this, ResultActivity.class);
            intent.putExtra("p1Name", p1Name);
            intent.putExtra("p2Name", p2Name);
            intent.putExtra("p1Score", p1Score);
            intent.putExtra("p2Score", p2Score);
            intent.putExtra("isOnline", isOnline);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            Log.d(TAG, "ResultActivity başlatıldı!");
        } catch (Exception e) {
            Log.e(TAG, "ResultActivity hatası: " + e.getMessage());
            finish();
        }
    }

    private void showConnectionLostDialog() {
        if (isDestroyed || isFinishing || gameEnded) return;

        gameEnded = true;
        cancelTimer();

        try {
            new AlertDialog.Builder(this)
                    .setTitle("Bağlantı Koptu")
                    .setMessage("Rakiple bağlantı kesildi.")
                    .setPositiveButton("Tamam", (dialog, which) -> safeFinish())
                    .setCancelable(false)
                    .show();
        } catch (Exception e) {
            safeFinish();
        }
    }

    private void safeFinish() {
        isFinishing = true;
        cancelTimer();
        handler.removeCallbacksAndMessages(null);

        if (isOnline) {
            try {
                SocketManager.setListener(null);
                SocketManager.setConnectionListener(null);
                SocketManager.stop();
                SocketHolder.close();
            } catch (Exception ignored) {}
        }

        finish();
    }

    @Override
    protected void onDestroy() {
        isDestroyed = true;
        gameEnded = true;
        cancelTimer();
        handler.removeCallbacksAndMessages(null);

        try { if (mpCorrect != null) mpCorrect.release(); } catch (Exception ignored) {}
        try { if (mpWrong != null) mpWrong.release(); } catch (Exception ignored) {}
        try { if (mpTimeout != null) mpTimeout.release(); } catch (Exception ignored) {}

        if (isOnline) {
            try {
                SocketManager.setListener(null);
                SocketManager.setConnectionListener(null);
                SocketManager.stop();
                SocketHolder.close();
            } catch (Exception ignored) {}
        }

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (isFinishing || gameEnded) return;

        new AlertDialog.Builder(this)
                .setTitle("Oyundan Çık")
                .setMessage("Oyundan çıkmak istediğinize emin misiniz?")
                .setPositiveButton("Evet", (dialog, which) -> {
                    gameEnded = true;
                    isFinishing = true;
                    cancelTimer();

                    if (isOnline && SocketHolder.isReady()) {
                        new Thread(() -> {
                            sendPlayerLeft();
                            runOnUiThread(this::safeFinish);
                        }).start();
                    } else {
                        safeFinish();
                    }
                })
                .setNegativeButton("Hayır", null)
                .show();
    }
}