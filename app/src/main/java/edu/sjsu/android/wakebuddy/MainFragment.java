package edu.sjsu.android.wakebuddy;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class MainFragment extends Fragment implements AlarmDeleteListener, AlarmChangeListener {
    private static ArrayList<Alarm> alarms;
    private NavController controller;

    public MainFragment() {
        // Required empty public constructor
        alarms = new ArrayList<Alarm>();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadAlarmsFromStorage();
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
        AlarmAdapter adapter = new AlarmAdapter(alarms, this, this);
        alarmsRecyclerView.setAdapter(adapter);

        getParentFragmentManager()
                .setFragmentResultListener("alarm_request_key", this, (requestKey, bundle) -> {
                    Alarm alarm = (Alarm) bundle.getSerializable("alarm");
                    if (alarm != null) {
                        alarms.add(alarm);
                        adapter.notifyItemInserted(alarms.size() - 1);
                        saveAlarmsToStorage();

                        if (alarm.isEnabled()) {
                            AlarmUtils.setAlarm(requireContext(), alarm);
                        }
                    }
                });

        controller = NavHostFragment.findNavController(this);

        TextView wakeupCounterText = view.findViewById(R.id.wakeupCounterText);
        SharedPreferences prefs = requireActivity().getSharedPreferences("WakeBuddyPrefs", Context.MODE_PRIVATE);
        int count = prefs.getInt("successfulWakeups", 0);
        wakeupCounterText.setText("Wakeups: " + count);

        // TODO: find way to change background based on if alarms are enabled, optional feature tbh
        //ImageView background = view.findViewById(R.id.backgroundImg);
        //background.setImageResource(R.drawable.sky);
        ImageView background = view.findViewById(R.id.backgroundImg);

        boolean hasEnabledAlarms = false;
        for (Alarm alarm : alarms) {
            if (alarm.isEnabled()) {
                hasEnabledAlarms = true;
                break;
            }
        }

        if (hasEnabledAlarms) {
            background.setImageResource(R.drawable.sky); // active alarms = daytime
        } else {
            background.setImageResource(R.drawable.night); // no alarms = sleepy mode
        }

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

    private void saveAlarmsToStorage() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("WakeBuddyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(alarms);
        editor.putString("alarms", json);
        editor.apply();
    }

    private void loadAlarmsFromStorage() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("WakeBuddyPrefs", Context.MODE_PRIVATE);
        String json = prefs.getString("alarms", null);
        if (json != null) {
            Type type = new TypeToken<ArrayList<Alarm>>() {}.getType();
            alarms = new Gson().fromJson(json, type);
        } else {
            alarms = new ArrayList<>();
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
            saveAlarmsToStorage();
        }
    }

    @Override
    public void onAlarmChange() {
        saveAlarmsToStorage();
    }
}
