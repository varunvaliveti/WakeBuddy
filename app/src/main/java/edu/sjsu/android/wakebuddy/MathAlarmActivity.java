package edu.sjsu.android.wakebuddy;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;
import java.util.Random;
import android.view.animation.AnimationSet;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.animation.LinearInterpolator;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;


public class MathAlarmActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.math_alarm); // balloon layout

        String label = getIntent().getStringExtra("label");

        //Show Alarm Name
        TextView labelText = findViewById(R.id.alarmLabelText);
        labelText.setText(label);
        //Show Math Question
        TextView questionText = findViewById(R.id.questionText);

        Button[] balloons = new Button[4];
        balloons[0] = findViewById(R.id.balloon1);
        balloons[1] = findViewById(R.id.balloon2);
        balloons[2] = findViewById(R.id.balloon3);
        balloons[3] = findViewById(R.id.balloon4);

        Random rand = new Random();
        int a = rand.nextInt(10) + 1;
        int b = rand.nextInt(10) + 1;
        char[] ops = {'+', '-', '*'};
        char op = ops[rand.nextInt(ops.length)];

        int correctAnswer;
        switch (op) {
            case '+':
                correctAnswer = a + b;
                break;
            case '-':
                correctAnswer = a - b;
                break;
            default:
                correctAnswer = a * b;
                break;
        }

        questionText.setText("What is " + a + " " + op + " " + b + "?");

        ArrayList<Integer> answers = new ArrayList<>();
        answers.add(correctAnswer);
        while (answers.size() < 4) {
            int fake = rand.nextInt(20);
            if (!answers.contains(fake)) answers.add(fake);
        }

        Collections.shuffle(answers);

        for (int i = 0; i < 4; i++) {
            balloons[i].setText(String.valueOf(answers.get(i)));
            int selected = answers.get(i);
            balloons[i].setOnClickListener(v -> {
                if (selected == correctAnswer) {

                    Toast.makeText(this, getString(R.string.toast_correct), Toast.LENGTH_SHORT).show();

                    // Stop the alarm service (ringtone)
                    stopService(new Intent(this, AlarmService.class));

                    // Record successful wake-up
                    SharedPreferences prefs = getSharedPreferences("WakeBuddyPrefs", MODE_PRIVATE);

                    // Update counter
                    int count = prefs.getInt("successfulWakeups", 0);
                    prefs.edit().putInt("successfulWakeups", count + 1).apply();

                   // Track today's date as a completed wake-up
                    Set<String> completedDates = prefs.getStringSet("completedDates", new HashSet<>());
                    completedDates = new HashSet<>(completedDates);
                    Calendar calendar = Calendar.getInstance();
                    int year = calendar.get(Calendar.YEAR);
                    int month = calendar.get(Calendar.MONTH) + 1; // Month is 0-indexed
                    int day = calendar.get(Calendar.DAY_OF_MONTH);
                    String today = String.format(Locale.US, "%04d-%02d-%02d", year, month, day);  // Format: 2025-05-06
                    completedDates.add(today);
                    prefs.edit().putStringSet("completedDates", completedDates).apply();

                    finish(); // Exit screen
                } else {
                    Toast.makeText(this, getString(R.string.toast_wrong), Toast.LENGTH_SHORT).show();
                }
            });
        }

        floatBalloonDrifty(balloons[0], 0);
        floatBalloonDrifty(balloons[1], 200);
        floatBalloonDrifty(balloons[2], 400);
        floatBalloonDrifty(balloons[3], 600);
    }

    private void floatBalloonDrifty(View balloon, long delay) {
        AnimationSet set = new AnimationSet(true);

        TranslateAnimation upDown = new TranslateAnimation(0, 0, 0, -50);
        upDown.setDuration(1500);
        upDown.setRepeatMode(Animation.REVERSE);
        upDown.setRepeatCount(Animation.INFINITE);
        upDown.setStartOffset(delay);
        upDown.setInterpolator(new BounceInterpolator());

        TranslateAnimation sideToSide = new TranslateAnimation(-20, 20, 0, 0);
        sideToSide.setDuration(2000);
        sideToSide.setRepeatMode(Animation.REVERSE);
        sideToSide.setRepeatCount(Animation.INFINITE);
        sideToSide.setStartOffset(delay + 200);
        sideToSide.setInterpolator(new AccelerateDecelerateInterpolator());

        set.addAnimation(upDown);
        set.addAnimation(sideToSide);

        balloon.startAnimation(set);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onUserLeaveHint() {
        // Prevent user from leaving — relaunch the challenge
        Intent intent = new Intent(this, MathAlarmActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        // Disable back button — no escape!
    }
}
