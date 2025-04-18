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
import android.widget.RadioGroup;

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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_alarm, container, false);
        RadioGroup radioGroup = view.findViewById(R.id.radioGroup);

        // Set the default checked RadioButton
        radioGroup.check(R.id.physicalAddAlarm);

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
            goMain();
        });
    }

    public void goMain() {
        // TODO: handle sending data to main fragment
        controller.popBackStack(R.id.mainFragment,true);
        controller.navigate(R.id.mainFragment);
    }
}