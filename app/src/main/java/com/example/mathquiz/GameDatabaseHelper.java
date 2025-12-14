package com.example.mathquiz;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class GameDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "games.db";
    private static final int DB_VERSION = 2; // ⬅️ ARTIRDIK

    public static final String TABLE = "games";
    public static final String COL_ID = "id";
    public static final String COL_P1 = "player1";
    public static final String COL_P2 = "player2";
    public static final String COL_S1 = "score1";
    public static final String COL_S2 = "score2";
    public static final String COL_WINNER = "winner";
    public static final String COL_DIFFICULTY = "difficulty"; // ⬅️ YENİ

    public GameDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql =
                "CREATE TABLE " + TABLE + " (" +
                        COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_P1 + " TEXT, " +
                        COL_P2 + " TEXT, " +
                        COL_S1 + " INTEGER, " +
                        COL_S2 + " INTEGER, " +
                        COL_WINNER + " TEXT, " +
                        COL_DIFFICULTY + " TEXT)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }

    // 🔴 OYUN KAYDI (ZORLUK DA VAR)
    public void insertGame(String p1, String p2, int s1, int s2, String winner, String difficulty) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_P1, p1);
        cv.put(COL_P2, p2);
        cv.put(COL_S1, s1);
        cv.put(COL_S2, s2);
        cv.put(COL_WINNER, winner);
        cv.put(COL_DIFFICULTY, difficulty);
        db.insert(TABLE, null, cv);
        db.close();
    }

    public Cursor getAllGames() {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM " + TABLE + " ORDER BY " + COL_ID + " DESC",
                null
        );
    }
}
