package edu.sjsu.android.wakebuddy;

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

import java.lang.reflect.Array;
import java.util.ArrayList;

public class AddAlarmFragment extends Fragment {
    private NavController controller;

    public AddAlarmFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // handle params here
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Log.d("", "clicked add");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_alarm, container, false);
        RadioGroup radioGroup = view.findViewById(R.id.radioGroup);

        // Set the default checked RadioButton
        radioGroup.check(R.id.physicalButton);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        controller = NavHostFragment.findNavController(this);
        view.findViewById(R.id.add_cancel_btn).setOnClickListener(v -> {
            // TODO: handle cancel button
            goMain();
        });
        view.findViewById(R.id.add_confirm_btn).setOnClickListener(v -> {
            // TODO: handle confirm button
            RadioGroup radioGroup = view.findViewById(R.id.radioGroup);
            int checkedID = radioGroup.getCheckedRadioButtonId();
            String label;
            if(checkedID == R.id.physicalButton) label = "Physical";
            else if (checkedID == R.id.yellingButton) label = "Yelling";
            else if (checkedID == R.id.mathButton) label = "Math";
            else if (checkedID == R.id.barcodeButton) label = "Barcode";
            else label = "None";



            Log.d("", "yeah clicked confirm "+label);
            TimePicker timePicker = view.findViewById(R.id.timePicker);

            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();

            String ampm = hour>12?"AM":"PM";

            // TODO: Consider changing this time format when we actually create an alarm for the system
            //also, label seems kind of complicated atp, we can lowk just remove that field lol.
            label = ((EditText) view.findViewById(R.id.alarm_name_entry)).getText().toString();

            String time = String.valueOf(hour>12?hour-12:hour)+":"+String.format("%02d",minute)+" "+ampm;

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

            String theDays = TextUtils.join(", ",days);



           Alarm newAlarm = new Alarm(time, label, theDays, true);

           Bundle result = new Bundle();
           result.putSerializable("alarm", newAlarm);
           getParentFragmentManager().setFragmentResult("alarm_request_key", result);

            Log.d("", "time picker is "+ timePicker.getHour() + " : "+timePicker.getMinute()+" "+ampm);
            goMain();
        });
    }

    public void goMain() {
        // TODO: handle sending data to main fragment
        controller.popBackStack();
        // controller.navigate(R.id.mainFragment);
    }
}