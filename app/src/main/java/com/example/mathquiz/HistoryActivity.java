package com.example.mathquiz;

import android.content.res.TypedArray;
import android.database.Cursor;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class HistoryActivity extends AppCompatActivity {

    LinearLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        container = findViewById(R.id.containerHistoryRows);

        GameDatabaseHelper db = new GameDatabaseHelper(this);
        Cursor c = db.getAllGames();

        int surfaceColor = getColorFromAttr(com.google.android.material.R.attr.colorSurface);
        int textColor = getColorFromAttr(com.google.android.material.R.attr.colorOnSurface);

        while (c.moveToNext()) {

            String p1 = c.getString(c.getColumnIndexOrThrow("player1"));
            String p2 = c.getString(c.getColumnIndexOrThrow("player2"));
            int s1 = c.getInt(c.getColumnIndexOrThrow("score1"));
            int s2 = c.getInt(c.getColumnIndexOrThrow("score2"));
            String winner = c.getString(c.getColumnIndexOrThrow("winner"));
            String difficulty = c.getString(c.getColumnIndexOrThrow("difficulty"));

            // SATIR
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(8, 12, 8, 12);
            row.setBackgroundColor(surfaceColor);

            row.addView(makeCell(p1, 2f, textColor));
            row.addView(makeCell(s1 + "-" + s2, 1.5f, textColor));
            row.addView(makeCell(p2, 2f, textColor));
            row.addView(makeCell(difficulty, 1.5f, textColor));
            row.addView(makeCell(winner, 2f, textColor));

            LinearLayout.LayoutParams lp =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, 8);

            container.addView(row, lp);
        }

        c.close();
    }

    private TextView makeCell(String text, float weight, int textColor) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                weight
        ));
        tv.setTextSize(14);
        tv.setTextColor(textColor);
        return tv;
    }

    // 🔑 Tema attribute → gerçek renk çevirici
    private int getColorFromAttr(int attr) {
        TypedValue typedValue = new TypedValue();
        TypedArray a = obtainStyledAttributes(typedValue.data, new int[]{attr});
        int color = a.getColor(0, 0);
        a.recycle();
        return color;
    }
}
