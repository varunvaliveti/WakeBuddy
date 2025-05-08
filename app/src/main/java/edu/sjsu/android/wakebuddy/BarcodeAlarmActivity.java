package edu.sjsu.android.wakebuddy;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class BarcodeAlarmActivity extends AppCompatActivity {
    private ActivityResultLauncher<ScanOptions> barcodeLauncher;
    public static String correct = "1234";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = getSharedPreferences("settings", Context.MODE_PRIVATE);
        String saved_code = prefs.getString("correct_code", "1234");
        setCorrect(saved_code);
        setContentView(R.layout.barcode_alarm);

        barcodeLauncher = registerForActivityResult(
                new ScanContract(),
                result -> {
                    String contents = result.getContents();
                    if (contents == null) {
                        Toast.makeText(BarcodeAlarmActivity.this,
                                getString(R.string.scan_cancelled),
                                Toast.LENGTH_SHORT).show();

                        // re-launch if you want
                        barcodeLauncher.launch(new ScanOptions().setPrompt(getString(R.string.scan_prompt)));
                    }
                    else if (correct.equals(contents)) {
                        // correct! stop the alarm
                        stopService(new Intent(BarcodeAlarmActivity.this, AlarmService.class));
                        SharedPreferences prefsWakeups = getSharedPreferences("WakeBuddyPrefs", MODE_PRIVATE);
                        int count = prefsWakeups.getInt("successfulWakeups", 0);
                        prefsWakeups.edit().putInt("successfulWakeups", count + 1).apply();
                        finish();
                    }
                    else {
                        Toast.makeText(BarcodeAlarmActivity.this,
                                getString(R.string.wrong_code),
                                Toast.LENGTH_SHORT).show();
                        barcodeLauncher.launch(new ScanOptions().setPrompt(getString(R.string.scan_prompt)));
                    }
                }
        );

        barcodeLauncher.launch(
                new ScanOptions()
                        .setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES)
                        .setPrompt(getString(R.string.scan_prompt))
                        .setBeepEnabled(true)
        );
    }

    public static void setCorrect(String newCorrect){
        correct = newCorrect;
    }

    public String getCorrect(){
        return correct;
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onUserLeaveHint() {
        // Ignore warning telling to call super(), since we're overriding it
        // Re-launch the activity if user tries to leave
        Intent intent = new Intent(this, BarcodeAlarmActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        // Ignore warning telling to call super(), since we don't want
        // to do anything when back is pressed
    }
}

