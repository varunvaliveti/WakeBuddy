package edu.sjsu.android.wakebuddy;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import java.util.Calendar;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String label = intent.getStringExtra("label");
        String task = intent.getStringExtra("task");
        int dayOfWeek = intent.getIntExtra("day", -1);
        String time = intent.getStringExtra("time");

        Intent serviceIntent = new Intent(context, AlarmService.class);
        serviceIntent.putExtra("label", label);
        serviceIntent.putExtra("task", task);

        // Start the service in the foreground
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
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
        }
    }
}