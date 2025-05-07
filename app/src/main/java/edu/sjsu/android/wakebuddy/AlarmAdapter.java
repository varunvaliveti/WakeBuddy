package edu.sjsu.android.wakebuddy;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.List;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {
    private List<Alarm> alarmList;
    private AlarmDeleteListener deleteListener;
    private AlarmChangeListener changeListener;

    public AlarmAdapter(List<Alarm> alarmList, AlarmDeleteListener deleteListener, AlarmChangeListener changeListener) {
        this.alarmList = alarmList;
        this.deleteListener = deleteListener;
        this.changeListener = changeListener;
    }

    public static class AlarmViewHolder extends RecyclerView.ViewHolder {
        public TextView alarmTimeTextView;
        public TextView alarmLabelTextView;
        public TextView alarmDaysTextView;
        public SwitchMaterial alarmSwitch;
        public ImageView deleteAlarmButton;
        public ImageView editAlarmButton;


        public AlarmViewHolder(View itemView) {
            super(itemView);
            alarmTimeTextView = itemView.findViewById(R.id.alarmTime);
            alarmLabelTextView = itemView.findViewById(R.id.alarmLabel);
            alarmDaysTextView = itemView.findViewById(R.id.alarmDays);
            alarmSwitch = itemView.findViewById(R.id.alarmSwitch);
            deleteAlarmButton = itemView.findViewById(R.id.deleteAlarmButton);
            editAlarmButton = itemView.findViewById(R.id.editAlarmButton);
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
        Log.d("",currAlarm.getTime());
        holder.alarmTimeTextView.setText(currAlarm.getTime());
        holder.alarmLabelTextView.setText(currAlarm.getLabel());
        holder.alarmDaysTextView.setText(currAlarm.getDays());
        holder.alarmSwitch.setChecked(currAlarm.isEnabled());

        holder.alarmSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            currAlarm.setEnabled(isChecked);

            if (isChecked) {
                AlarmUtils.setAlarm(holder.itemView.getContext(), currAlarm);
            } else {
                AlarmUtils.cancelAlarm(holder.itemView.getContext(), currAlarm);
            }

            if (changeListener != null) {
                changeListener.onAlarmChange();
            }
        });

        holder.deleteAlarmButton.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Delete Alarm")
                    .setMessage("Are you sure you want to delete this alarm?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        deleteListener.onAlarmDelete(currAlarm);
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        holder.editAlarmButton.setOnClickListener(v -> {
            Bundle result = new Bundle();
            result.putSerializable("edit_alarm", currAlarm);
            result.putInt("edit_index", holder.getAdapterPosition());
            Navigation.findNavController(v).navigate(R.id.addAlarmFragment, result);
        });

    }

    @Override
    public int getItemCount() {
        return alarmList.size();
    }
}
