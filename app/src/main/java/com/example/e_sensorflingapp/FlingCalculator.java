package com.example.e_sensorflingapp;

public class FlingCalculator {

    public static final int MAX_SCORE = 5000;
    private static final int SCORE_MULTIPLIER = 350;

    public int calculateScore(float x, float y) {
        double horizontalAcceleration = Math.sqrt((x * x) + (y * y));
        int score = (int) Math.round(horizontalAcceleration * SCORE_MULTIPLIER);
        return clamp(score);
    }

    private int clamp(int score) {
        if (score < 0) {
            return 0;
        }
        if (score > MAX_SCORE) {
            return MAX_SCORE;
        }
        return score;
    }
}
