package edu.sjsu.android.wakebuddy;

import android.content.Intent;
import android.os.Bundle;
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
    private static final String CORRECT = "1234";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.barcode_alarm);

        // 1) register the scan‐contract
        barcodeLauncher = registerForActivityResult(
                new ScanContract(),
                result -> {
                    String contents = result.getContents();
                    if (contents == null) {
                        Toast.makeText(BarcodeAlarmActivity.this,
                                "Scan cancelled. Please try again.",
                                Toast.LENGTH_SHORT).show();

                        // re-launch if you want
                        barcodeLauncher.launch(new ScanOptions().setPrompt("Scan barcode to stop alarm"));
                    }
                    else if (CORRECT.equals(contents)) {
                        // correct! stop the alarm
                        stopService(new Intent(BarcodeAlarmActivity.this, AlarmService.class));
                        finish();
                    }
                    else {
                        Toast.makeText(BarcodeAlarmActivity.this,
                                "Wrong code. Try again.",
                                Toast.LENGTH_SHORT).show();
                        barcodeLauncher.launch(new ScanOptions().setPrompt("Scan barcode to stop alarm"));
                    }
                }
        );

        // 2) launch the scanner right away
        barcodeLauncher.launch(
                new ScanOptions()
                        .setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES)
                        .setPrompt("Scan barcode to stop alarm")
                        .setBeepEnabled(true)
        );
    }

    // Register the launcher and result handler




    @Override
    protected void onUserLeaveHint() {
        // Ignore warning telling to call super(), since we're overriding it
        // Re-launch the activity if user tries to leave
        Intent intent = new Intent(this, BarcodeAlarmActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        // Ignore warning telling to call super(), since we don't want
        // to do anything when back is pressed
    }
}

