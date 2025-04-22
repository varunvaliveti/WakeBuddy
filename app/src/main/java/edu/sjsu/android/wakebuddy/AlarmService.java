package edu.sjsu.android.wakebuddy;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class AlarmService extends Service {
    private static final String CHANNEL_ID = "AlarmServiceChannel";
    private Ringtone ringtone;

    @Override
    public void onCreate() {
        super.onCreate();

        // Create notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Alarm Service Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String label = intent.getStringExtra("label");
        String task = intent.getStringExtra("task");

        assert task != null;
        Intent activityIntent = getActivityIntent(task, label);

        // Create pending intent for the activity
        // Alarm will activate even if app is closed or screen is off
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                activityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Creates notification for the alarm
        // Users tap the notification to open the alarm activity
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("WakeBuddy Alarm")
                .setContentText("Alarm: " + (label != null ? label : ""))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        startForeground(1, notification);

        // Vibrate
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(1000);
            }
        }

        // Play ringtone
        Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmUri == null)
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        ringtone = RingtoneManager.getRingtone(getApplicationContext(), alarmUri);
        if (ringtone != null)
            ringtone.play();

        return START_NOT_STICKY;
    }

    @NonNull
    private Intent getActivityIntent(String task, String label) {
        // Create an intent for the activity based on the task
        Intent activityIntent = new Intent(this, MovementAlarmActivity.class);

        // Sets the correct activity based on the task
        switch (task) {
            case "Yelling":
                activityIntent = new Intent(this, YellingAlarmActivity.class);
                break;
            case "Math":
                activityIntent = new Intent(this, MathAlarmActivity.class);
                break;
            case "Barcode":
                activityIntent = new Intent(this, BarcodeAlarmActivity.class);
                break;
            case "Movement":
                activityIntent = new Intent(this, MovementAlarmActivity.class);
                break;
        }

        activityIntent.putExtra("label", label);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return activityIntent;
    }

    @Override
    public void onDestroy() {
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
        }

        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.cancel();
        }

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
