package edu.sjsu.android.wakebuddy;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;

public class YellingAlarmActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 1001;
    private static final int AMP_THRESHOLD = 8000;
    private static final int YELLING_DURATION_MS = 3000;
    private static final int POLL_INTERVAL_MS = 100;
    public static final String RECORD_AUDIO = "android.permission.RECORD_AUDIO";

    private MediaRecorder recorder;
    private final Handler handler = new Handler();
    private long startTime = -1;
    private ProgressBar progressBar;
    private TextView statusText;
    private Runnable meterRunnable;
    private Runnable successRunnable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.yelling_alarm);
        progressBar  = findViewById(R.id.yellingProgressBar);
        statusText  = findViewById(R.id.yellingAlarmText);


        successRunnable = () -> {
            stopService(new Intent(this, AlarmService.class));
            Toast.makeText(this, "Awesome! Alarm stopped after yelling for 3 seconds!", Toast.LENGTH_SHORT).show();
            finish();
        };
        meterRunnable = new Runnable() {
            @Override public void run() {
                int amp = recorder != null ? recorder.getMaxAmplitude() : 0;

                if (amp > AMP_THRESHOLD) {
                    if (startTime == -1)
                        startTime = System.currentTimeMillis();

                    long elapsed = System.currentTimeMillis() - startTime;
                    int pct = (int)(elapsed * 100 / YELLING_DURATION_MS);
                    progressBar.setProgress(Math.min(pct, 100));
                    statusText.setText(R.string.keep_yelling);

                    if (elapsed >= YELLING_DURATION_MS) {
                        stopAlarmAndFinish();
                        return;
                    }
                } else {
                    startTime = -1;
                    progressBar.setProgress(0);
                    statusText.setText(R.string.you_stopped_try_again);
                }
                handler.postDelayed(this, POLL_INTERVAL_MS);
            }
        };

        if (ContextCompat.checkSelfPermission(
                this, RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{RECORD_AUDIO}, PERMISSION_REQUEST_CODE);
        } else {
            startMeter();
        }
    }

    @Override
    public void onRequestPermissionsResult(int req, @NonNull String[] p, @NonNull int[] g) {
        super.onRequestPermissionsResult(req, p, g);
        if (req == PERMISSION_REQUEST_CODE && g.length > 0 && g[0] == PackageManager.PERMISSION_GRANTED) {
            startMeter();
        } else {
            Toast.makeText(this, "Mic permission required!", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void startMeter() {
        try {
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            recorder.setOutputFile("/dev/null");
            recorder.prepare();
            recorder.start();
        } catch (IOException e) {
            Toast.makeText(this, "Mic error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        handler.post(meterRunnable);
    }

    private void stopAlarmAndFinish() {
        Intent stopServiceIntent = new Intent(this, AlarmService.class);
        stopService(stopServiceIntent);
        if (recorder != null) {
            recorder.stop();
            recorder.release();
        }

        SharedPreferences prefs = getSharedPreferences("WakeBuddyPrefs", MODE_PRIVATE);
        int count = prefs.getInt("successfulWakeups", 0);
        prefs.edit().putInt("successfulWakeups", count + 1).apply();

        Toast.makeText(this, "Nice lungs! Alarm dismissed.", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(successRunnable);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onUserLeaveHint() {
        // Ignore warning telling to call super(), since we're overriding it
        // Re-launch the activity if user tries to leave
        Intent intent = new Intent(this, YellingAlarmActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        // Ignore warning telling to call super(), since we don't want
        // to do anything when back is pressed
    }
}
