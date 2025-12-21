package com.example.mathquiz;

import java.util.Random;

/**
 * QuestionGenerator - Zorluk seviyesine göre matematik sorusu üretici
 */
public class QuestionGenerator {

    private static final Random random = new Random();

    /**
     * Belirtilen zorluk seviyesine göre soru üretir
     * @param difficulty "easy", "medium" veya "hard"
     * @return Question objesi
     */
    public static Question generate(String difficulty) {
        if (difficulty == null) {
            difficulty = "easy";
        }

        switch (difficulty.toLowerCase()) {
            case "medium":
                return generateMedium();
            case "hard":
                return generateHard();
            case "easy":
            default:
                return generateEasy();
        }
    }

    /**
     * Kolay seviye soru üretir
     * - Tek haneli toplama/çıkarma/çarpma
     */
    private static Question generateEasy() {
        int a = random.nextInt(9) + 1;  // 1-9
        int b = random.nextInt(9) + 1;  // 1-9
        int op = random.nextInt(3);     // 0, 1, 2

        switch (op) {
            case 0:
                return new Question(a + " + " + b, a + b);
            case 1:
                // Negatif sonuç olmasın
                if (a < b) {
                    int temp = a;
                    a = b;
                    b = temp;
                }
                return new Question(a + " - " + b, a - b);
            case 2:
            default:
                return new Question(a + " × " + b, a * b);
        }
    }

    /**
     * Orta seviye soru üretir
     * - Tek haneli × çift haneli çarpma
     * - İki haneli toplama
     */
    private static Question generateMedium() {
        if (random.nextBoolean()) {
            // Çarpma
            int a = random.nextInt(9) + 1;     // 1-9
            int b = random.nextInt(19) + 11;   // 11-29
            return new Question(a + " × " + b, a * b);
        } else {
            // Toplama
            int a = random.nextInt(90) + 10;   // 10-99
            int b = random.nextInt(90) + 10;   // 10-99
            return new Question(a + " + " + b, a + b);
        }
    }

    /**
     * Zor seviye soru üretir
     * - İki haneli çarpma
     * - Üç haneli toplama
     */
    private static Question generateHard() {
        if (random.nextBoolean()) {
            // Çarpma
            int a = random.nextInt(90) + 10;   // 10-99
            int b = random.nextInt(90) + 10;   // 10-99
            return new Question(a + " × " + b, a * b);
        } else {
            // Toplama
            int a = random.nextInt(900) + 100; // 100-999
            int b = random.nextInt(900) + 100; // 100-999
            return new Question(a + " + " + b, a + b);
        }
    }
}