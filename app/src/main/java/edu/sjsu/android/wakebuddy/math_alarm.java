package edu.sjsu.android.wakebuddy;
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
import java.util.Collections;
import java.util.Random;
import android.view.animation.AnimationSet;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.animation.LinearInterpolator;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class math_alarm extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.math_alarm, container, false);

        TextView questionText = view.findViewById(R.id.questionText);
        Button[] balloons = new Button[4];
        balloons[0] = view.findViewById(R.id.balloon1);
        balloons[1] = view.findViewById(R.id.balloon2);
        balloons[2] = view.findViewById(R.id.balloon3);
        balloons[3] = view.findViewById(R.id.balloon4);

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
                    // TODO: Stop alarm service or media player here
                    Toast.makeText(getContext(), "Have a Great Day!", Toast.LENGTH_SHORT).show();

                    //Keep Track of Successes
                    SharedPreferences prefs = requireActivity().getSharedPreferences("WakeBuddyPrefs", Context.MODE_PRIVATE);
                    int count = prefs.getInt("successfulWakeups", 0);
                    prefs.edit().putInt("successfulWakeups", count + 1).apply();

                    //Finish Alarm
                    requireActivity().finish(); //
                } else {
                    Toast.makeText(getContext(), "Wrong answer!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        floatBalloonDrifty(balloons[0], 0);
        floatBalloonDrifty(balloons[1], 200);
        floatBalloonDrifty(balloons[2], 400);
        floatBalloonDrifty(balloons[3], 600);

        /*
        floatAround(balloons[0], 0f, 0f, 0);
        floatAround(balloons[1], 50f, 0f, 300);
        floatAround(balloons[2], -50f, 0f, 600);
        floatAround(balloons[3], 0f, 100f, 900);

        loopBounce(balloons[0], 100f, 1500);
        loopBounce(balloons[1], 120f, 1800);
        loopBounce(balloons[2], 90f, 1700);
        loopBounce(balloons[3], 110f, 1600);
        */

        return view;
    }

    private void loopBounce(View balloon, float distance, long duration) {
        balloon.animate()
                .translationYBy(-distance)
                .setDuration(duration)
                .withEndAction(() -> balloon.animate()
                        .translationYBy(distance)
                        .setDuration(duration)
                        .withEndAction(() -> loopBounce(balloon, distance, duration))
                        .start()
                ).start();
    }


    private void floatBalloon(View balloon, long delay) {
        TranslateAnimation anim = new TranslateAnimation(
                0, 0,    // X from/to
                0, -250  // Y from 0 to -150 pixels (higher bounce)
        );
        anim.setDuration(1000); // faster bounce
        anim.setRepeatMode(TranslateAnimation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        anim.setStartOffset(delay); // optional stagger
        anim.setInterpolator(new BounceInterpolator()); // bounce effect!
        balloon.startAnimation(anim);
    }

    private void floatBalloonDrifty(View balloon, long delay) {
        AnimationSet set = new AnimationSet(true);

        // Vertical Bounce
        //TranslateAnimation upDown = new TranslateAnimation(0, 0, 0, -150);
        //TranslateAnimation upDown = new TranslateAnimation(0, 0, 0, -80);
        TranslateAnimation upDown = new TranslateAnimation(0, 0, 0, -50);
        upDown.setDuration(1500);
        upDown.setRepeatMode(Animation.REVERSE);
        upDown.setRepeatCount(Animation.INFINITE);
        upDown.setStartOffset(delay);
        upDown.setInterpolator(new BounceInterpolator());

        // Horizontal Sway
        //TranslateAnimation sideToSide = new TranslateAnimation(-30, 30, 0, 0);
        TranslateAnimation sideToSide = new TranslateAnimation(-20, 20, 0, 0);
        sideToSide.setDuration(2000);
        sideToSide.setRepeatMode(Animation.REVERSE);
        sideToSide.setRepeatCount(Animation.INFINITE);
        sideToSide.setStartOffset(delay + 200);
        sideToSide.setInterpolator(new AccelerateDecelerateInterpolator());

        // Combine both
        set.addAnimation(upDown);
        set.addAnimation(sideToSide);

        balloon.startAnimation(set);
    }

    private void floatAround(View balloon, float startX, float startY, long delay) {
        // Animate Y (up/down drift)
        ObjectAnimator animatorY = ObjectAnimator.ofFloat(balloon, "translationY", startY, startY - 200, startY);
        animatorY.setDuration(3000);
        animatorY.setRepeatCount(ValueAnimator.INFINITE);
        animatorY.setRepeatMode(ValueAnimator.REVERSE);
        animatorY.setInterpolator(new AccelerateDecelerateInterpolator());

        // Animate X (left/right drift)
        ObjectAnimator animatorX = ObjectAnimator.ofFloat(balloon, "translationX", startX, startX + 100, startX - 100, startX);
        animatorX.setDuration(4000);
        animatorX.setRepeatCount(ValueAnimator.INFINITE);
        animatorX.setRepeatMode(ValueAnimator.REVERSE);
        animatorX.setInterpolator(new LinearInterpolator());

        AnimatorSet set = new AnimatorSet();
        set.playTogether(animatorX, animatorY);
        set.setStartDelay(delay);
        set.start();
    }
}