package com.groupit;

import android.graphics.Matrix;

import java.util.Calendar;
import java.util.TimeZone;

public class Time {

    private long time;
    private String type = "AM";
    private Calendar current;
    private Long currentTime = System.currentTimeMillis();

    public Time (long time) {
        this.time = time;

        current = Calendar.getInstance();
        current.setTimeZone(TimeZone.getDefault());
        current.setTimeInMillis(currentTime);
    }

    public String getString() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getDefault());
        cal.setTimeInMillis(time);

        if (cal.get(Calendar.AM_PM) == 1) {
            type = "PM";
        }

        long diff = (time - currentTime) / 1000;
        int hours = (int) (diff / 3600);

        if (hours < 12) {
            return hours + " hours ago";
        }

        int minutes = (int) (diff / 60);

        if (hours == 0) {
            return minutes + " minutes ago";
        }

        String min = String.valueOf(cal.get(Calendar.MINUTE));
        if (min.length() == 1) {
            min = min + "0";
        }
        String time = cal.get(Calendar.HOUR) + ":" + min + " " + type;

        return cal.get(Calendar.WEEK_OF_MONTH) + "/" + cal.get(Calendar.MONTH) + "/" + cal.get(Calendar.YEAR) + " " + time + " " + type;
    }
}
