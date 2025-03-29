package edu.sjsu.android.wakebuddy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.List;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {
    private List<Alarm> alarmList;
    public AlarmAdapter(List<Alarm> alarmList) { this.alarmList = alarmList; }
    public static class AlarmViewHolder extends RecyclerView.ViewHolder {
        public TextView alarmTimeTextView;
        public TextView alarmLabelTextView;
        public TextView alarmDaysTextView;
        public SwitchMaterial alarmSwitch;

        public AlarmViewHolder(View itemView) {
            super(itemView);
            alarmTimeTextView = itemView.findViewById(R.id.alarmTime);
            alarmLabelTextView = itemView.findViewById(R.id.alarmLabel);
            alarmDaysTextView = itemView.findViewById(R.id.alarmDays);
            alarmSwitch = itemView.findViewById(R.id.alarmSwitch);
        }
    }

    @NonNull
    @Override
    public AlarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AlarmViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.alarm_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AlarmViewHolder holder, int position) {
        Alarm currAlarm = alarmList.get(position);
        holder.alarmTimeTextView.setText(currAlarm.getTime());
        holder.alarmLabelTextView.setText(currAlarm.getLabel());
        holder.alarmDaysTextView.setText(currAlarm.getDays());
        holder.alarmSwitch.setChecked(currAlarm.isEnabled());

        holder.alarmSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            currAlarm.setEnabled(isChecked);
            // todo: potentially handle other data changes
        });
    }

    @Override
    public int getItemCount() {
        return alarmList.size();
    }

    public void updateData(List<Alarm> newAlarms) {
        alarmList.clear();
        alarmList.addAll(newAlarms);
        notifyDataSetChanged();
    }
}
