package edu.sjsu.android.wakebuddy;

import static android.content.Context.MODE_PRIVATE;

import static androidx.core.app.ActivityCompat.finishAffinity;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.Settings;
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
    private SharedPreferences prefs;
    private SharedPreferences.OnSharedPreferenceChangeListener wakeupListener;
    private TextView wakeupCounterText;

    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    result -> {
                        // result: Map<permission, granted>
                        if (result.containsValue(false)) {
                            Toast.makeText(
                                    requireContext(),
                                    getString(R.string.camera_microphone_required),
                                    Toast.LENGTH_LONG
                            ).show();
                            finishAffinity(requireActivity());  // closes all activities
                        }
                    }
            );

    public MainFragment() {
        alarms = new ArrayList<>();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkAndRequestPermissions();
        loadAlarmsFromStorage();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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
                    int editIndex = bundle.getInt("edit_index", -1);
                    Alarm alarm = (Alarm) bundle.getSerializable("alarm");
                    if (alarm != null) {
                        if (editIndex != -1) {
                            Alarm oldAlarm = alarms.get(editIndex);
                            AlarmUtils.cancelAlarm(requireContext(), oldAlarm);
                            alarms.set(editIndex, alarm);
                            adapter.notifyItemChanged(editIndex);
                        } else {
                            alarms.add(alarm);
                            adapter.notifyItemInserted(alarms.size() - 1);
                        }
                        if (alarm.isEnabled()) {
                            AlarmUtils.setAlarm(requireContext(), alarm);
                        }
                        saveAlarmsToStorage();
                    }
                });

        controller = NavHostFragment.findNavController(this);

        wakeupCounterText = view.findViewById(R.id.wakeupCounterText);
        prefs = requireActivity().getSharedPreferences("WakeBuddyPrefs", Context.MODE_PRIVATE);

        updateWakeupText();

        wakeupListener = (sharedPreferences, key) -> {
            if ("successfulWakeups".equals(key)) {
                updateWakeupText();
            }
        };
        prefs.registerOnSharedPreferenceChangeListener(wakeupListener);

        ImageView background = view.findViewById(R.id.backgroundImg);
        int mode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (mode == Configuration.UI_MODE_NIGHT_YES) {
            background.setImageResource(R.drawable.night);
        } else {
            background.setImageResource(R.drawable.sky);
        }

        ImageButton addAlarmBtn = view.findViewById(R.id.addAlarmButton);
        addAlarmBtn.setOnClickListener(v -> goAddAlarm());

        ImageButton settingsBtn = view.findViewById(R.id.alarmSettingsButton);
        settingsBtn.setOnClickListener(v -> controller.navigate(R.id.settingsFragment));

        ImageButton calendarBtn = view.findViewById(R.id.calendarButton);
        calendarBtn.setOnClickListener(v -> controller.navigate(R.id.calendarFragment));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                new AlertDialog.Builder(requireContext())
                        .setTitle(getString(R.string.exact_alarm_title))
                        .setMessage(getString(R.string.exact_alarm_message))
                        .setPositiveButton(getString(R.string.grant), (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                            startActivity(intent);
                        })
                        .setNegativeButton(getString(R.string.later), null)
                        .show();
            }
        }
    }

    public void goAddAlarm() {
        controller.navigate(R.id.addAlarmFragment);
    }

    private void updateWakeupText() {
        int count = prefs.getInt("successfulWakeups", 0);
        wakeupCounterText.setText(getString(R.string.wakeup_counter, count));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (prefs != null && wakeupListener != null) {
            prefs.unregisterOnSharedPreferenceChangeListener(wakeupListener);
        }
    }

    private void saveAlarmsToStorage() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("WakeBuddyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(alarms);
        editor.putString("alarms", json);
        editor.apply();
    }

    private void loadAlarmsFromStorage() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("WakeBuddyPrefs", MODE_PRIVATE);
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
        AlarmUtils.cancelAlarm(requireContext(), alarm);
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


    private void checkAndRequestPermissions() {
        String[] required = {
                "android.permission.CAMERA",
                "android.permission.RECORD_AUDIO"
        };
        boolean needRequest = false;
        for (String p : required) {
            if (ContextCompat.checkSelfPermission(requireContext(), p)
                    != PackageManager.PERMISSION_GRANTED) {
                needRequest = true;
                break;
            }
        }
        if (needRequest) {
            permissionLauncher.launch(required);
        }
    }
}
