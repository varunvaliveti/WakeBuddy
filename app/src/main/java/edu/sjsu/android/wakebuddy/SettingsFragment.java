package edu.sjsu.android.wakebuddy;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.app.AlertDialog;
import com.google.android.material.materialswitch.MaterialSwitch;
import java.util.Locale;
import com.google.android.material.materialswitch.MaterialSwitch;

public class SettingsFragment extends Fragment {

    public SettingsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView logoOverlay = view.findViewById(R.id.loadLogoOverlay);
        ImageView skyBg = view.findViewById(R.id.skyBackground);

        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            logoOverlay.setVisibility(View.GONE);
            skyBg.setImageResource(R.drawable.night);
        } else {
            logoOverlay.setVisibility(View.VISIBLE);
            skyBg.setImageResource(R.drawable.sky);
        }

        RadioGroup rg = view.findViewById(R.id.radioGroup2);
        MaterialSwitch darkModeSwitch = view.findViewById(R.id.darkModeSwitch);

        SharedPreferences prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE);
        boolean isDark = prefs.getBoolean("dark_mode", false);
        darkModeSwitch.setChecked(isDark);

        String savedCode = prefs.getString("correct_code", "1234");
        BarcodeAlarmActivity.setCorrect(savedCode);

        if (savedCode.equals("1234")) {
            rg.check(R.id.radioButton);
        } else if (savedCode.equals("5678")) {
            rg.check(R.id.radioButton2);
        } else {
            rg.check(R.id.radioButton3);
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

        // Language button setup
        Button btnLanguage = view.findViewById(R.id.btn_language);
        btnLanguage.setOnClickListener(v -> showLanguageDialog());
    }

    private void showLanguageDialog() {
        final String[] languages = { getString(R.string.english), getString(R.string.spanish) };

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.select_language)
                .setItems(languages, (dialog, which) -> {
                    if (which == 0) {
                        setLocale("en");
                    } else if (which == 1) {
                        setLocale("es");
                    }
                }).show();
    }

    private void setLocale(String langCode) {
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        requireActivity().getBaseContext().getResources().updateConfiguration(
                config, requireActivity().getBaseContext().getResources().getDisplayMetrics());

        // Save preference
        SharedPreferences prefs = requireContext().getSharedPreferences("Settings", Context.MODE_PRIVATE);
        prefs.edit().putString("App_Lang", langCode).apply();

        // Restart activity to apply changes
        requireActivity().recreate();
    }
}


