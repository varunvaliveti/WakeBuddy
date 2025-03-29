package edu.sjsu.android.wakebuddy;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
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
        alarmList.add(new Alarm("7:00 AM", "School", "Mon, Tues", false));
        alarmList.add(new Alarm("9:00 PM", "Study", "Mon, Tues, Thurs", true));
        alarmList.add(new Alarm("10:00 AM", "Work", "Mon, Fri, Sat", false));
        AlarmAdapter adapter = new AlarmAdapter(alarmList);
        alarmsRecyclerView.setAdapter(adapter);

        ImageView background = findViewById(R.id.backgroundImg);
        background.setImageResource(R.drawable.sky);

    }
}