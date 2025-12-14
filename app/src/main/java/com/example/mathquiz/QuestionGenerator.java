package com.example.mathquiz;

import java.util.Random;

public class QuestionGenerator {

    private static Random r = new Random();

    public static Question generate(String difficulty) {

        if (difficulty.equals("easy")) {
            int a = r.nextInt(9) + 1;
            int b = r.nextInt(9) + 1;
            int op = r.nextInt(3);

            if (op == 0) return new Question(a + " + " + b, a + b);
            if (op == 1) return new Question(a + " - " + b, a - b);
            return new Question(a + " × " + b, a * b);
        }

        if (difficulty.equals("medium")) {
            if (r.nextBoolean()) {
                int a = r.nextInt(9) + 1;
                int b = r.nextInt(19) + 11;
                return new Question(a + " × " + b, a * b);
            } else {
                int a = r.nextInt(90) + 10;
                int b = r.nextInt(90) + 10;
                return new Question(a + " + " + b, a + b);
            }
        }

        // hard
        if (r.nextBoolean()) {
            int a = r.nextInt(90) + 10;
            int b = r.nextInt(90) + 10;
            return new Question(a + " × " + b, a * b);
        } else {
            int a = r.nextInt(900) + 100;
            int b = r.nextInt(900) + 100;
            return new Question(a + " + " + b, a + b);
        }
    }
}
