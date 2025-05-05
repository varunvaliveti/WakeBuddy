package edu.sjsu.android.wakebuddy;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MovementAlarmActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private static final float MOVEMENT_THRESHOLD = 3.0f;
    private static final long CONTINUOUS_MOVEMENT_TIME_MS = 10000; // 10 seconds
    private static final long INACTIVITY_RESET_TIME_MS = 1000; // 1 second
    private boolean isMoving = false;
    private long lastMovementTimestamp = 0;
    private final Handler handler = new Handler();
    private Runnable successRunnable;
    private Runnable progressUpdateRunnable;
    private ProgressBar progressBar;
    private TextView statusText;
    private long movementStartTime = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movement_alarm);

        progressBar = findViewById(R.id.movementProgressBar);
        statusText = findViewById(R.id.movementStatusText);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        if (sensorManager != null && accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }

        successRunnable = () -> {
            stopService(new Intent(this, AlarmService.class));
            Toast.makeText(this, "Awesome! Alarm stopped after moving 10 seconds!", Toast.LENGTH_SHORT).show();
            finish();
        };
        progressUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                if (isMoving) {
                    long elapsed = System.currentTimeMillis() - movementStartTime;
                    int progress = (int) (elapsed * 100 / CONTINUOUS_MOVEMENT_TIME_MS);
                    progressBar.setProgress(Math.min(progress, 100));

                    if (progress >= 100) {
                        handler.removeCallbacks(progressUpdateRunnable); // Stop updating after 100%
                    } else {
                        handler.postDelayed(this, 100); // Update again after 100ms
                    }
                }
            }
        };
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onUserLeaveHint() {
        // Ignore warning telling to call super(), since we're overriding it
        // Re-launch the activity if user tries to leave
        Intent intent = new Intent(this, MovementAlarmActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        // Ignore warning telling to call super(), since we don't want
        // to do anything when back is pressed
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        float acceleration = (float) Math.sqrt(x * x + y * y + z * z);

        long currentTime = System.currentTimeMillis();

        if (Math.abs(acceleration - SensorManager.GRAVITY_EARTH) > MOVEMENT_THRESHOLD) {
            if (!isMoving) {
                isMoving = true;
                movementStartTime = currentTime;
                handler.postDelayed(successRunnable, CONTINUOUS_MOVEMENT_TIME_MS);
                handler.post(progressUpdateRunnable);
                statusText.setText(R.string.keep_moving);
            }
            lastMovementTimestamp = currentTime;
        } else {
            if (isMoving && (currentTime - lastMovementTimestamp > INACTIVITY_RESET_TIME_MS)) {
                isMoving = false;
                handler.removeCallbacks(successRunnable);
                handler.removeCallbacks(progressUpdateRunnable);
                progressBar.setProgress(0);
                statusText.setText(R.string.you_stopped_try_again);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        handler.removeCallbacks(successRunnable);
    }
}

