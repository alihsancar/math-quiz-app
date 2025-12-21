package com.example.mathquiz;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Question - Soru modeli
 * Soruyu ve cevabını tutar, JSON dönüşümlerini sağlar
 */
public class Question {

    public String text;
    public int answer;

    public Question(String text, int answer) {
        this.text = text;
        this.answer = answer;
    }

    /**
     * Question objesini JSON'a dönüştürür
     * @return JSONObject
     */
    public JSONObject toJson() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("text", text);
            obj.put("answer", answer);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }

    /**
     * JSON'dan Question objesi oluşturur
     * @param obj JSON objesi
     * @return Question veya null
     */
    public static Question fromJson(JSONObject obj) {
        if (obj == null) return null;

        try {
            String text = obj.optString("text", "");
            int answer = obj.optInt("answer", 0);

            if (text.isEmpty()) {
                return null;
            }

            return new Question(text, answer);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        return "Question{text='" + text + "', answer=" + answer + "}";
    }
}