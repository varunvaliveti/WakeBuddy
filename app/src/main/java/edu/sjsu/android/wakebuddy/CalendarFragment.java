package edu.sjsu.android.wakebuddy;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarFragment extends Fragment {

    private TextView alarmsText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);
        CalendarView calendarView = view.findViewById(R.id.calendarView);
        alarmsText = view.findViewById(R.id.alarmsText);

        ImageView logoOverlay = view.findViewById(R.id.loadLogoOverlay);
        ImageView skyBg = view.findViewById(R.id.skyBackground);

        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            logoOverlay.setVisibility(View.GONE);
            skyBg.setImageResource(R.drawable.night);  // Optional: set darker image
        }

        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            String selectedDay = getDayOfWeek(year, month, dayOfMonth);
            showAlarmsForDay(selectedDay);
        });

        return view;
    }


    private String getDayOfWeek(int year, int month, int day) {
        try {
            String dateStr = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date date = format.parse(dateStr);
            assert date != null;
            return new SimpleDateFormat("EEE", Locale.US).format(date);  // e.g., "Mon", "Tue"
        } catch (Exception e) {
            return "";
        }
    }

    private void showAlarmsForDay(String selectedDay) {
        SharedPreferences prefs = requireActivity().getSharedPreferences("WakeBuddyPrefs", Context.MODE_PRIVATE);
        String json = prefs.getString("alarms", null);
        StringBuilder sb = new StringBuilder();

        if (json != null) {
            Type type = new TypeToken<ArrayList<Alarm>>() {}.getType();
            List<Alarm> alarms = new Gson().fromJson(json, type);

            for (Alarm alarm : alarms) {
                if (alarm.isEnabled() && alarm.getDays().toLowerCase().contains(selectedDay.toLowerCase())) {
                    sb.append(alarm.getTime())
                            .append(" - ")
                            .append(alarm.getLabel())
                            .append(" (")
                            .append(alarm.getDays())
                            .append(")\n");
                }
            }
        }

        alarmsText.setText(sb.length() > 0 ? sb.toString() : getString(R.string.no_alarms_message));
    }
}
