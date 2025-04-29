package edu.sjsu.android.wakebuddy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MathAlarmActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.math_alarm);

        String label = getIntent().getStringExtra("label");
        TextView alarmLabel = findViewById(R.id.questionText);
        alarmLabel.setText(label);

        /* No Stop Alarm Button
        Button stopAlarmButton = findViewById(R.id.stopAlarmButtonMath);
        stopAlarmButton.setOnClickListener(v -> {
            // The following 3 lines are to stop the foreground service and finish the activity
            // It doesn't have to be with a button, you can add it to any logic you want
            Intent stopServiceIntent = new Intent(this, AlarmService.class);
            stopService(stopServiceIntent);
            finish();
        });
         */
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onUserLeaveHint() {
        // Ignore warning telling to call super(), since we're overriding it
        // Re-launch the activity if user tries to leave
        Intent intent = new Intent(this, MathAlarmActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onBackPressed() {
        // Ignore warning telling to call super(), since we don't want
        // to do anything when back is pressed
    }
}
