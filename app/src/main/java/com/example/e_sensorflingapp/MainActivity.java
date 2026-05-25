package com.example.e_sensorflingapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private TextView textViewHighscore;
    private TextView textViewLastAttempt;
    private ProgressBar progressBarHighscore;
    private int highscore;
    private int lastAttempt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initViews();
        updateScoreViews();
    }

    private void initViews() {
        textViewHighscore = findViewById(R.id.textViewHighscore);
        textViewLastAttempt = findViewById(R.id.textViewLastAttempt);
        progressBarHighscore = findViewById(R.id.progressBarHighscore);
        Button buttonRestart = findViewById(R.id.buttonRestart);

        progressBarHighscore.setMax(FlingCalculator.MAX_SCORE);
        buttonRestart.setOnClickListener(v -> resetHighscore());
    }

    private void resetHighscore() {
        highscore = 0;
        lastAttempt = 0;
        updateScoreViews();
    }

    private void updateScoreViews() {
        textViewHighscore.setText(getString(R.string.highscore_value, highscore));
        textViewLastAttempt.setText(getString(R.string.last_attempt_value, lastAttempt));
        progressBarHighscore.setProgress(highscore);
    }
}
