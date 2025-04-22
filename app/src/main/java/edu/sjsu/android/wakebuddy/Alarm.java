package edu.sjsu.android.wakebuddy;

import java.io.Serializable;

public class Alarm implements Serializable {

        private String time;
        private String label;
        private String task;
        private String days;
        private boolean isEnabled;

        public String getTask() {
            return task;
        }

        public void setTask(String task) {
            this.task = task;
        }

        public String getTime() {
            return time;
        }

        public boolean isEnabled() {
            return isEnabled;
        }

        public String getDays() {
            return days;
        }

        public String getLabel() {
            return label;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public void setDays(String days) {
            this.days = days;
        }

        public void setEnabled(boolean enabled) {
            isEnabled = enabled;
        }

        public Alarm(String time, String label,String task, String days, boolean isEnabled) {
            this.time = time;
            this.label = label;
            this.task = task;
            this.days = days;
            this.isEnabled = isEnabled;
        }


    }

