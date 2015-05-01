package com.groupit;

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

        long diff = (currentTime - time) / 1000;
        int hours = (int) (diff / 3600);

        if (hours <= 12 && hours >= 1) {
            if (hours == 1) {
                return hours + " hour ago";
            }
            return hours + " hours ago";
        }

        int minutes = (int) (diff / 60);

        if (minutes <= 60 && minutes >= 1) {
            if (minutes == 1) {
                return minutes + " minute ago";
            }
            return minutes + " minutes ago";
        }

        if (minutes == 0) {
            if (diff == 0 || diff == 1) {
                return diff + " second ago";
            }
            return diff + " seconds ago";
        }


        String time = cal.get(Calendar.HOUR) + ":" + cal.get(Calendar.MINUTE) + " " + type;

        return cal.get(Calendar.YEAR) + "/" + cal.get(Calendar.MONTH) + "/" + cal.get(Calendar.DAY_OF_MONTH) + " " + time;
    }
}
