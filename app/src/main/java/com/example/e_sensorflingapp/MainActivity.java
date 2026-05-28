package com.example.e_sensorflingapp;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private TextView textViewHighscore;
    private TextView textViewLastAttempt;
    private ProgressBar progressBarHighscore;
    private SensorManager sensorManager;
    private Sensor accelerationSensor;
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
        initSensor();
        updateScoreViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accelerationSensor != null) {
            sensorManager.registerListener(this, accelerationSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void initViews() {
        textViewHighscore = findViewById(R.id.textViewHighscore);
        textViewLastAttempt = findViewById(R.id.textViewLastAttempt);
        progressBarHighscore = findViewById(R.id.progressBarHighscore);
        Button buttonRestart = findViewById(R.id.buttonRestart);

        progressBarHighscore.setMax(FlingCalculator.MAX_SCORE);
        buttonRestart.setOnClickListener(v -> resetHighscore());
    }

    private void initSensor() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        if (accelerationSensor == null) {
            accelerationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        if (accelerationSensor == null) {
            Toast.makeText(this, R.string.no_acceleration_sensor, Toast.LENGTH_LONG).show();
        }
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
