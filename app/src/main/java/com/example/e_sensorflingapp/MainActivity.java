package com.example.e_sensorflingapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String KEY_HIGHSCORE = "highscore";
    private static final String KEY_LAST_ATTEMPT = "lastAttempt";
    private static final String KEY_GRAVITY_ACTIVE = "gravityActive";
    private static final int MIN_ATTEMPT_SCORE = 50;
    private static final int GRAVITY_DECAY_SCORE = 100;
    private static final int REQUEST_POST_NOTIFICATIONS = 10;
    private static final long GRAVITY_DELAY_MS = 1000L;

    private TextView textViewHighscore;
    private TextView textViewLastAttempt;
    private CheckBox checkBoxGravity;
    private ProgressBar progressBarHighscore;
    private SensorManager sensorManager;
    private Sensor accelerationSensor;
    private NotificationHelper notificationHelper;
    private final Handler gravityHandler = new Handler(Looper.getMainLooper());
    private final FlingCalculator flingCalculator = new FlingCalculator();
    private final Runnable gravityRunnable = new Runnable() {
        @Override
        public void run() {
            if (checkBoxGravity != null && checkBoxGravity.isChecked()) {
                decreaseHighscore();
                gravityHandler.postDelayed(this, GRAVITY_DELAY_MS);
            }
        }
    };
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
        notificationHelper = new NotificationHelper(this);
        requestNotificationPermission();
        restoreState(savedInstanceState);
        updateScoreViews();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_HIGHSCORE, highscore);
        outState.putInt(KEY_LAST_ATTEMPT, lastAttempt);
        outState.putBoolean(KEY_GRAVITY_ACTIVE, checkBoxGravity.isChecked());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accelerationSensor != null) {
            sensorManager.registerListener(this, accelerationSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (checkBoxGravity != null && checkBoxGravity.isChecked()) {
            startGravityDecay();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        stopGravityDecay();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.values.length < 2) {
            return;
        }

        int score = flingCalculator.calculateScore(event.values[0], event.values[1]);
        if (score < MIN_ATTEMPT_SCORE) {
            return;
        }

        lastAttempt = score;
        if (score > highscore) {
            highscore = score;
            notificationHelper.sendHighscoreNotification(highscore);
        }
        updateScoreViews();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void initViews() {
        textViewHighscore = findViewById(R.id.textViewHighscore);
        textViewLastAttempt = findViewById(R.id.textViewLastAttempt);
        checkBoxGravity = findViewById(R.id.checkBoxGravity);
        progressBarHighscore = findViewById(R.id.progressBarHighscore);
        Button buttonRestart = findViewById(R.id.buttonRestart);

        progressBarHighscore.setMax(FlingCalculator.MAX_SCORE);
        buttonRestart.setOnClickListener(v -> resetHighscore());
        checkBoxGravity.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                startGravityDecay();
            } else {
                stopGravityDecay();
            }
        });
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

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return;
        }

        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_POST_NOTIFICATIONS);
        }
    }

    private void restoreState(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return;
        }

        highscore = savedInstanceState.getInt(KEY_HIGHSCORE, 0);
        lastAttempt = savedInstanceState.getInt(KEY_LAST_ATTEMPT, 0);
        checkBoxGravity.setChecked(savedInstanceState.getBoolean(KEY_GRAVITY_ACTIVE, false));
    }

    private void resetHighscore() {
        highscore = 0;
        lastAttempt = 0;
        updateScoreViews();
    }

    private void decreaseHighscore() {
        highscore = Math.max(0, highscore - GRAVITY_DECAY_SCORE);
        updateScoreViews();
    }

    private void startGravityDecay() {
        gravityHandler.removeCallbacks(gravityRunnable);
        gravityHandler.postDelayed(gravityRunnable, GRAVITY_DELAY_MS);
    }

    private void stopGravityDecay() {
        gravityHandler.removeCallbacks(gravityRunnable);
    }

    private void updateScoreViews() {
        textViewHighscore.setText(getString(R.string.highscore_value, highscore));
        textViewLastAttempt.setText(getString(R.string.last_attempt_value, lastAttempt));
        progressBarHighscore.setProgress(highscore);
    }
}
