package edu.sjsu.android.wakebuddy;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AlarmReceiver extends BroadcastReceiver {


    //Notification Channel for app
    public static void createWakeBuddyNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "wakebuddy_channel",
                    "WakeBuddy Alarms",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Used for launching alarm challenges");
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String label = intent.getStringExtra("label");
        String task = intent.getStringExtra("task");
        int dayOfWeek = intent.getIntExtra("day", -1);
        String time = intent.getStringExtra("time");

        Log.d("WakeBuddy", "📣 Alarm RECEIVED for: " + label + " at " + time + " (task: " + task + ")");

        Intent serviceIntent = new Intent(context, AlarmService.class);
        serviceIntent.putExtra("label", label);
        serviceIntent.putExtra("task", task);

        // Start the service in the foreground
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }

        //Fix to launch the correct challenge activity based on the task type
        Class<?> activityToLaunch;

        switch (task) {
            case "Math":
                activityToLaunch = MathAlarmActivity.class;
                break;
            case "Barcode":
                activityToLaunch = BarcodeAlarmActivity.class;
                break;
            case "Yelling":
                activityToLaunch = YellingAlarmActivity.class;
                break;
            case "Movement":
                activityToLaunch = MovementAlarmActivity.class;
                break;
            default:
                activityToLaunch = MainActivity.class; // fallback
        }

        createWakeBuddyNotificationChannel(context); // New Method

        Intent fullScreenIntent = new Intent(context, activityToLaunch);
        fullScreenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(
                context, 0, fullScreenIntent, PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "wakebuddy_channel")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Wake Up!")
                .setContentText("Solve the challenge to stop the alarm")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                        == PackageManager.PERMISSION_GRANTED) {

            NotificationManagerCompat.from(context).notify(999, builder.build());
        }

        // Reschedule the same alarm for next week
        if (dayOfWeek != -1 && time != null) {
            Calendar nextAlarm = Calendar.getInstance();

            String[] timeParts = time.split(":| ");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);
            boolean isPM = timeParts[2].equalsIgnoreCase("PM");
            if (isPM && hour != 12) hour += 12;
            if (!isPM && hour == 12) hour = 0;

            nextAlarm.set(Calendar.DAY_OF_WEEK, dayOfWeek);
            nextAlarm.set(Calendar.HOUR_OF_DAY, hour);
            nextAlarm.set(Calendar.MINUTE, minute);
            nextAlarm.set(Calendar.SECOND, 0);
            nextAlarm.set(Calendar.MILLISECOND, 0);
            nextAlarm.add(Calendar.WEEK_OF_YEAR, 1);

            Intent repeatIntent = new Intent(context, AlarmReceiver.class);
            repeatIntent.putExtra("label", label);
            repeatIntent.putExtra("task", task);
            repeatIntent.putExtra("day", dayOfWeek);
            repeatIntent.putExtra("time", time);

            int requestCode = (label + dayOfWeek).hashCode();
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    repeatIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    Intent reqSchedIntent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    context.startActivity(reqSchedIntent);
                    return;
                }
            }
            try {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        nextAlarm.getTimeInMillis(),
                        pendingIntent
                );
            } catch (SecurityException e) {
                Log.e("Alarm", "Exact alarm scheduling not permitted: " + e.getMessage());
            }
        } else {
            // Turn off one-time alarm visually on the main screen
            SharedPreferences prefs = context.getSharedPreferences("WakeBuddyPrefs", Context.MODE_PRIVATE);
            String json = prefs.getString("alarms", null);
            if (json != null) {
                Type type = new TypeToken<ArrayList<Alarm>>() {}.getType();
                List<Alarm> alarms = new Gson().fromJson(json, type);
                for (Alarm a : alarms) {
                    if (a.getLabel().equals(label)
                            && a.getTime().equals(time)
                            && a.getDays().isEmpty()) {
                        a.setEnabled(false);
                        break;
                    }
                }
                prefs.edit()
                        .putString("alarms", new Gson().toJson(alarms))
                        .apply();
            }
            // send a broadcast so the UI can reload
            Intent update = new Intent("edu.sjsu.android.wakebuddy.ALARMS_UPDATED");
            context.sendBroadcast(update);
        }
    }

}
