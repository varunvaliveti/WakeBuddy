package edu.sjsu.android.wakebuddy;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Switch;

import com.google.android.material.materialswitch.MaterialSwitch;

public class SettingsFragment extends Fragment {

    public SettingsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        RadioGroup rg = view.findViewById(R.id.radioGroup2);


        MaterialSwitch darkModeSwitch = view.findViewById(R.id.darkModeSwitch);

        SharedPreferences prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE);
        boolean isDark = prefs.getBoolean("dark_mode", false);
        darkModeSwitch.setChecked(isDark);

        String savedCode = prefs.getString("correct_code", "1234");
        BarcodeAlarmActivity.setCorrect(savedCode);
        switch (savedCode) {
            case "1234":
                rg.check(R.id.radioButton);
                break;
            case "5678":
                rg.check(R.id.radioButton2);
                break;
            case "1111":
                rg.check(R.id.radioButton3);
                break;
        }

        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("dark_mode", isChecked).apply();
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });


        rg.setOnCheckedChangeListener((group, checkedId) -> {
            String code;
            if (checkedId == R.id.radioButton) {
                code = "1234";
            } else if (checkedId == R.id.radioButton2) {
                code = "5678";
            } else {
                code = "1111";
            }
            prefs.edit().putString("correct_code", code).apply();
            BarcodeAlarmActivity.setCorrect(code);
        });


    }
}
