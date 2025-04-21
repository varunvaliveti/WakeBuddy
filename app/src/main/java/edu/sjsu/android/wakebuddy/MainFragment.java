package edu.sjsu.android.wakebuddy;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

public class MainFragment extends Fragment implements AlarmDeleteListener {
    private static ArrayList<Alarm> alarms;
    private NavController controller;

    public MainFragment() {
        // Required empty public constructor
        alarms = new ArrayList<Alarm>();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // Handle params here
        }

        // alarms = new ArrayList<>();
        // Test Values
        alarms.add(new Alarm("7:00 AM", "School","Movement", "Mon, Tues", false));
        alarms.add(new Alarm("9:00 PM", "Study","Math", "Mon, Tues, Thurs", true));
        alarms.add(new Alarm("10:00 AM", "Work","Barcode", "Mon, Fri, Sat", false));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView alarmsRecyclerView = view.findViewById(R.id.alarmsRecyclerView);

        alarmsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        AlarmAdapter adapter = new AlarmAdapter(alarms, this);
        alarmsRecyclerView.setAdapter(adapter);

        getParentFragmentManager()
                .setFragmentResultListener("alarm_request_key", this, (requestKey, bundle) -> {
                    Alarm alarm = (Alarm) bundle.getSerializable("alarm");
                    if (alarm != null) {
                        alarms.add(alarm);
                        adapter.notifyItemInserted(alarms.size() - 1);

                        if (alarm.isEnabled()) {
                            setAndroidAlarm(requireContext(), alarm);
                        }
                    }
                });

        controller = NavHostFragment.findNavController(this);

        // TODO: find way to change background based on if alarms are enabled, optional feature tbh
        ImageView background = view.findViewById(R.id.backgroundImg);
        background.setImageResource(R.drawable.sky);

        ImageButton addAlarmBtn = view.findViewById(R.id.addAlarmButton);
        addAlarmBtn.setOnClickListener(v -> {
            // TODO: handle onclick for adding alarms
            goAddAlarm();
        });

        ImageButton settingsBtn = view.findViewById(R.id.alarmSettingsButton);
        settingsBtn.setOnClickListener(v -> {
            // TODO: handle onclick for settings
            Toast.makeText(getContext(), "Settings button clicked", Toast.LENGTH_SHORT).show();
        });

        ImageButton calendarBtn = view.findViewById(R.id.calendarButton);
        calendarBtn.setOnClickListener(v -> {
            // TODO: handle onclick for calendar
            Toast.makeText(getContext(), "Add calendar button clicked", Toast.LENGTH_SHORT).show();
        });
    }

    public void goAddAlarm() {
        controller.navigate(R.id.addAlarmFragment);
    }

    private void setAndroidAlarm(Context context, Alarm alarm) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        String[] daysArray = alarm.getDays().split(",\\s*");
        String[] timeParts = alarm.getTime().split(":| ");
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);
        boolean isPM = timeParts[2].equalsIgnoreCase("PM");
        if (isPM && hour != 12) hour += 12;
        if (!isPM && hour == 12) hour = 0;

        if (alarm.getDays().isEmpty()) {
            // One-time alarm for next available time
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            // If the time has already passed today, schedule for tomorrow
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
        }
        else {
            // TODO: Recurring alarms still need to be tested
            for (String day : daysArray) {
                int dayOfWeek = mapDayToCalendar(day);
                if (dayOfWeek == -1)
                    continue;

                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);

                // If time is before now, schedule for next week
                if (calendar.before(Calendar.getInstance())) {
                    calendar.add(Calendar.WEEK_OF_YEAR, 1);
                }

                Intent intent = new Intent(context, AlarmReceiver.class);
                intent.putExtra("label", alarm.getLabel());
                intent.putExtra("task", alarm.getTask());
                intent.putExtra("day", dayOfWeek); // Save the scheduled day for rescheduling
                intent.putExtra("time", alarm.getTime()); // Also needed for rescheduling

                int requestCode = (alarm.getLabel() + day).hashCode(); // unique per label+day
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

    private int mapDayToCalendar(String day) {
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

    @Override
    public void onAlarmDelete(Alarm alarm) {
        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);

        String[] daysArray = alarm.getDays().split(",\\s*");
        if (alarm.getDays().isEmpty()) {
            int requestCode = (alarm.getLabel() + "once").hashCode();
            Intent intent = new Intent(requireContext(), AlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    requireContext(), requestCode, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            alarmManager.cancel(pendingIntent);
        } else {
            for (String day : daysArray) {
                int requestCode = (alarm.getLabel() + day).hashCode();
                Intent intent = new Intent(requireContext(), AlarmReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        requireContext(), requestCode, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );
                alarmManager.cancel(pendingIntent);
            }
        }

        int index = alarms.indexOf(alarm);
        if (index != -1) {
            alarms.remove(index);
            RecyclerView recyclerView = requireView().findViewById(R.id.alarmsRecyclerView);
            ((AlarmAdapter) recyclerView.getAdapter()).notifyItemRemoved(index);
        }
    }
}
