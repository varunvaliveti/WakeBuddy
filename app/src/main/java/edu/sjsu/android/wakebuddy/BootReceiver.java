package edu.sjsu.android.wakebuddy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            SharedPreferences prefs = context.getSharedPreferences("WakeBuddyPrefs", Context.MODE_PRIVATE);
            String json = prefs.getString("alarms", null);

            if (json != null) {
                Type type = new TypeToken<ArrayList<Alarm>>() {}.getType();
                ArrayList<Alarm> alarms = new Gson().fromJson(json, type);

                for (Alarm alarm : alarms) {
                    if (alarm.isEnabled()) {
                        AlarmUtils.setAlarm(context, alarm);
                    }
                }
            }
        }
    }
}
