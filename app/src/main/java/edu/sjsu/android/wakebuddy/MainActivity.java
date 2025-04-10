package edu.sjsu.android.wakebuddy;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioGroup;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {

    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    private Button setAlarm;
    private RadioGroup radioGroupTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RadioGroup radioGroup = findViewById(R.id.radioGroup);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Fragment selectedFragment = null;

                // Use if-else to handle selection
                if (checkedId == R.id.radioButton) {
                    selectedFragment = new barcode_alarm();
                } else if (checkedId == R.id.radioButton2) {
                    selectedFragment = new math_alarm();
                } else if (checkedId == R.id.radioButton3) {
                    selectedFragment = new movement_alarm();
                }

                if (selectedFragment != null) {
                    loadFragment(selectedFragment);
                }
            }
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();
    }
}