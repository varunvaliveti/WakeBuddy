package edu.sjsu.android.wakebuddy;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import java.util.Calendar;

public class AlarmUtils {
    public static void cancelAlarm(Context context, Alarm alarm) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        String[] daysArray = alarm.getDays().split(",\\s*");

        if (alarm.getDays().isEmpty()) {
            int requestCode = (alarm.getLabel() + "once").hashCode();
            Intent intent = new Intent(context, AlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, requestCode, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            alarmManager.cancel(pendingIntent);
        } else {
            for (String day : daysArray) {
                int requestCode = (alarm.getLabel() + day).hashCode();
                Intent intent = new Intent(context, AlarmReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        context, requestCode, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );
                alarmManager.cancel(pendingIntent);
            }
        }
    }

    public static void setAlarm(Context context, Alarm alarm) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        String[] daysArray = alarm.getDays().split(",\\s*");
        String[] timeParts = alarm.getTime().split(":| ");
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);
        boolean isPM = timeParts[2].equalsIgnoreCase("PM");
        if (isPM && hour != 12) hour += 12;
        if (!isPM && hour == 12) hour = 0;

        if (alarm.getDays().isEmpty()) {
            // One-time alarm
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);


            if (calendar.before(Calendar.getInstance())) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            Intent intent = new Intent(context, AlarmReceiver.class);
            intent.putExtra("label", alarm.getLabel());
            intent.putExtra("task", alarm.getTask());
            intent.putExtra("time", alarm.getTime());

            int requestCode = (alarm.getLabel() + "once").hashCode();
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
        } else {
            // Recurring alarms
            for (String day : daysArray) {
                int dayOfWeek = mapDayToCalendar(day);
                if (dayOfWeek == -1) continue;

                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);

                if (calendar.before(Calendar.getInstance())) {
                    calendar.add(Calendar.WEEK_OF_YEAR, 1);
                }

                Intent intent = new Intent(context, AlarmReceiver.class);
                intent.putExtra("label", alarm.getLabel());
                intent.putExtra("task", alarm.getTask());
                intent.putExtra("day", dayOfWeek);
                intent.putExtra("time", alarm.getTime());

                int requestCode = (alarm.getLabel() + day).hashCode();
                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        context,
                        requestCode,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

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
                            calendar.getTimeInMillis(),
                            pendingIntent
                    );
                } catch (SecurityException e) {
                    Log.e("Alarm", "Exact alarm scheduling not permitted: " + e.getMessage());
                }
            }
        }
    }

    private static int mapDayToCalendar(String day) {
        switch (day) {
            case "Sun": return Calendar.SUNDAY;
            case "Mon": return Calendar.MONDAY;
            case "Tues": return Calendar.TUESDAY;
            case "Wed": return Calendar.WEDNESDAY;
            case "Thurs": return Calendar.THURSDAY;
            case "Fri": return Calendar.FRIDAY;
            case "Sat": return Calendar.SATURDAY;
            default: return -1;
        }
    }
}