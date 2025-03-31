package edu.sjsu.android.wakebuddy;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        RecyclerView alarmsRecyclerView = findViewById(R.id.alarmsRecyclerView);
        alarmsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<Alarm> alarmList = new ArrayList<>();
        // Test Values
        alarmList.add(new Alarm("7:00 AM", "School", "Mon, Tues", false));
        alarmList.add(new Alarm("9:00 PM", "Study", "Mon, Tues, Thurs", true));
        alarmList.add(new Alarm("10:00 AM", "Work", "Mon, Fri, Sat", false));
        AlarmAdapter adapter = new AlarmAdapter(alarmList);
        alarmsRecyclerView.setAdapter(adapter);

        // TODO: find way to change background based on if alarms are enabled, optional feature tbh
        ImageView background = findViewById(R.id.backgroundImg);
        background.setImageResource(R.drawable.sky);

        ImageButton addAlarmBtn = findViewById(R.id.addAlarmButton);
        addAlarmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: handle onclick for adding alarms
                Toast.makeText(MainActivity.this, "Add alarm button clicked", Toast.LENGTH_SHORT).show();
            }
        });

        ImageButton settingsBtn = findViewById(R.id.alarmSettingsButton);
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: handle onclick for settings
                Toast.makeText(MainActivity.this, "Add settings button clicked", Toast.LENGTH_SHORT).show();
            }
        });

        ImageButton calendarBtn = findViewById(R.id.calendarButton);
        calendarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: handle onclick for calendar
                Toast.makeText(MainActivity.this, "Add calendar button clicked", Toast.LENGTH_SHORT).show();
            }
        });

    }
}