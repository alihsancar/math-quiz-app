//OnlineGameActivity.java
package com.example.mathquiz;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class OnlineGameActivity extends AppCompatActivity {

    TextView tvQuestion, tvStatus;
    EditText etAnswer;
    Button btnSend;

    boolean isHost;
    Question currentQuestion;

    Thread listenThread;
    volatile boolean running = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_game);

        tvQuestion = findViewById(R.id.tvQuestion);
        tvStatus   = findViewById(R.id.tvStatus);
        etAnswer   = findViewById(R.id.etAnswer);
        btnSend    = findViewById(R.id.btnSend);

        isHost = getIntent().getBooleanExtra("isHost", false);

        if (!SocketHolder.isReady()) {
            tvStatus.setText("Bağlantı yok");
            btnSend.setEnabled(false);
            return;
        }

        btnSend.setOnClickListener(v -> sendAnswer());

        if (isHost) {
            sendNewQuestion();
        }

        startListening();
    }

    // ================= HOST =================
    private void sendNewQuestion() {
        currentQuestion = QuestionGenerator.generate("easy");
        tvQuestion.setText(currentQuestion.text);
        tvStatus.setText("Cevap bekleniyor...");

        SocketHolder.send(
                "QUESTION|" + currentQuestion.text + "|" + currentQuestion.answer
        );
    }

    // ================= LISTENER =================
    private void startListening() {
        listenThread = new Thread(() -> {
            try {
                String line;
                while (running && (line = SocketHolder.in.readLine()) != null) {

                    if (line.startsWith("QUESTION") && !isHost) {
                        String[] p = line.split("\\|");
                        currentQuestion = new Question(p[1], Integer.parseInt(p[2]));

                        runOnUiThread(() -> {
                            tvQuestion.setText(currentQuestion.text);
                            tvStatus.setText("Cevap ver");
                        });
                    }

                    else if (line.startsWith("ANSWER") && isHost) {
                        int ans = Integer.parseInt(line.split("\\|")[1]);
                        boolean correct = ans == currentQuestion.answer;

                        SocketHolder.send("RESULT|" + (correct ? "DOĞRU" : "YANLIŞ"));

                        runOnUiThread(() ->
                                tvStatus.setText("Rakip: " + (correct ? "DOĞRU" : "YANLIŞ"))
                        );

                        Thread.sleep(1000);
                        sendNewQuestion();
                    }

                    else if (line.startsWith("RESULT") && !isHost) {
                        String res = line.split("\\|")[1];
                        runOnUiThread(() ->
                                tvStatus.setText("Sonuç: " + res)
                        );
                    }
                }
            } catch (Exception e) {
                runOnUiThread(() ->
                        tvStatus.setText("Bağlantı koptu")
                );
            }
        });

        listenThread.start();
    }

    // ================= CLIENT =================
    private void sendAnswer() {
        if (currentQuestion == null) return;

        String txt = etAnswer.getText().toString().trim();
        if (txt.isEmpty()) return;

        SocketHolder.send("ANSWER|" + txt);
        etAnswer.setText("");
        tvStatus.setText("Cevap gönderildi");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        running = false;
        SocketHolder.close();
    }
}
