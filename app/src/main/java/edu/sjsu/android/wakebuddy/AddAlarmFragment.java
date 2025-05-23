package edu.sjsu.android.wakebuddy;

import static edu.sjsu.android.wakebuddy.AlarmUtils.mapCalendarToDay;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Calendar;

public class AddAlarmFragment extends Fragment {
    private NavController controller;

    public AddAlarmFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Log.d("", "clicked add");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_alarm, container, false);
        RadioGroup radioGroup = view.findViewById(R.id.radioGroup);

        // Set the default checked RadioButton
        radioGroup.check(R.id.movementButton);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        controller = NavHostFragment.findNavController(this);
        Alarm editingAlarm = null;
        int editingIndex;

        // Check if this is an edit operation
        if (getArguments() != null && getArguments().containsKey("edit_alarm")) {
            editingAlarm = (Alarm) getArguments().getSerializable("edit_alarm");
            editingIndex = getArguments().getInt("edit_index");

            assert editingAlarm != null;
            ((EditText) view.findViewById(R.id.alarm_name_entry)).setText(editingAlarm.getLabel());

            String[] timeParts = editingAlarm.getTime().split(":| ");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);
            boolean isPM = timeParts[2].equalsIgnoreCase("PM");
            if (isPM && hour != 12) hour += 12;
            if (!isPM && hour == 12) hour = 0;

            TimePicker timePicker = view.findViewById(R.id.timePicker);
            timePicker.setHour(hour);
            timePicker.setMinute(minute);

            String task = editingAlarm.getTask();
            RadioGroup radioGroup = view.findViewById(R.id.radioGroup);
            switch (task) {
                case "Movement":
                    radioGroup.check(R.id.movementButton);
                    break;
                case "Yelling":
                    radioGroup.check(R.id.yellingButton);
                    break;
                case "Math":
                    radioGroup.check(R.id.mathButton);
                    break;
                case "Barcode":
                    radioGroup.check(R.id.barcodeButton);
                    break;
            }

            String[] selectedDays = editingAlarm.getDays().split(",\\s*");
            LinearLayout linearLayout = view.findViewById(R.id.linearLayout);
            for (int i = 0; i < linearLayout.getChildCount(); i++) {
                View child = linearLayout.getChildAt(i);
                if (child instanceof CheckBox) {
                    CheckBox cb = (CheckBox) child;
                    for (String day : selectedDays) {
                        if (cb.getText().toString().equalsIgnoreCase(day)) {
                            cb.setChecked(true);
                        }
                    }
                }
            }
        } else {
            editingIndex = -1;
        }

        view.findViewById(R.id.add_cancel_btn).setOnClickListener(v -> {
            goMain();
        });

        view.findViewById(R.id.add_confirm_btn).setOnClickListener(v -> {
            RadioGroup radioGroup = view.findViewById(R.id.radioGroup);
            int checkedID = radioGroup.getCheckedRadioButtonId();
            String label;
            String task;

            if(checkedID == R.id.movementButton) task = "Movement";
            else if (checkedID == R.id.yellingButton) task = "Yelling";
            else if (checkedID == R.id.mathButton) task = "Math";
            else if (checkedID == R.id.barcodeButton) task = "Barcode";
            else task = "None"; // Maybe handle this no task with its own screen, but task should never be null anyways so idk

            Log.d("", "yeah clicked confirm " + task);
            TimePicker timePicker = view.findViewById(R.id.timePicker);
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();

            label = ((EditText) view.findViewById(R.id.alarm_name_entry)).getText().toString();

            String ampm = hour < 12 ? "AM":"PM";
            int displayHour = hour > 12 ? hour - 12 : hour;
            if (displayHour == 0) {
                displayHour = 12;
            }
            String formattedMinute = String.format("%02d", minute);
            String time = displayHour + ":" + formattedMinute + " " + ampm;

            LinearLayout linearLayout = view.findViewById(R.id.linearLayout);
            ArrayList<String> days = new ArrayList<>();
            for (int i = 0; i < linearLayout.getChildCount(); i++) {
                View child = linearLayout.getChildAt(i);
                if (child instanceof CheckBox) {
                    CheckBox checkBox = (CheckBox) child;
                    if (checkBox.isChecked()) {
                        days.add(checkBox.getText().toString());
                    }
                }
            }

            // If no days are selected, select current day of the week
            if (days.isEmpty()) {
                Calendar calendar = Calendar.getInstance();
                int currentDayInt = calendar.get(Calendar.DAY_OF_WEEK);
                String currentDayString = mapCalendarToDay(currentDayInt);
                days.add(currentDayString);
            }

            String theDays = TextUtils.join(", ",days);
            int id = (int) System.currentTimeMillis();
            Alarm newAlarm = new Alarm(id, time, label, task, theDays, true);

            Bundle result = new Bundle();
            result.putSerializable("alarm", newAlarm);
            result.putString("task", task);
            result.putInt("edit_index", editingIndex);
            getParentFragmentManager().setFragmentResult("alarm_request_key", result);

            Log.d("", "time picker is "+ timePicker.getHour() + " : "+timePicker.getMinute()+" "+ampm);
            goMain();
        });
    }

    public void goMain() {
        controller.popBackStack();
        // controller.navigate(R.id.mainFragment);
    }
}