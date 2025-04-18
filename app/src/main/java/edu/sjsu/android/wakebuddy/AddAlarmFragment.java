package edu.sjsu.android.wakebuddy;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

public class AddAlarmFragment extends Fragment {


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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_alarm, container, false);
        RadioGroup radioGroup = view.findViewById(R.id.radioGroup);

        // Set the default checked RadioButton
        radioGroup.check(R.id.physicalAddAlarm);

        return view;
    }
}