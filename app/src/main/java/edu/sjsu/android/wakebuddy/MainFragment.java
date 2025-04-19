package edu.sjsu.android.wakebuddy;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainFragment extends Fragment {
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
        alarms.add(new Alarm("7:00 AM", "School", "Mon, Tues", false));
        alarms.add(new Alarm("9:00 PM", "Study", "Mon, Tues, Thurs", true));
        alarms.add(new Alarm("10:00 AM", "Work", "Mon, Fri, Sat", false));
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
        AlarmAdapter adapter = new AlarmAdapter(alarms);
        alarmsRecyclerView.setAdapter(adapter);

        getParentFragmentManager()
                .setFragmentResultListener("alarm_request_key", this, (requestKey, bundle) -> {
                    Alarm alarm = (Alarm) bundle.getSerializable("alarm");
                    if (alarm != null) {
                        alarms.add(alarm);
                        adapter.notifyItemInserted(alarms.size() - 1);
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
}