package com.example.daniel.todule_android.utilities;

/**
 * Created by danieL on 9/29/2017.
 */

public final class DateTimeUtils {

    private DateTimeUtils(){}

    public static long millisDiffFromNow (long then) {
        long now = System.currentTimeMillis();
        return then - now;
    }

    public static String dateTimeDiff (long then) {
        String result;
        String pastOrFuture;
        long diff = millisDiffFromNow(then);
        if (diff < 0 ) {
            pastOrFuture = " ago";
        } else {
            pastOrFuture = " left";
        }
        diff = Math.abs(diff);
        long diffInSeconds = diff / 1000;
        long diffInMinutes = diffInSeconds / 60;
        long diffInHours = diffInMinutes / 60;
        long diffInDays = diffInHours / 24;
        if (diffInDays >= 1) {
            result = String.valueOf(diffInDays);
            result += (diffInDays == 1) ? " day" : " days";
        } else if (diffInHours >= 1) {
            result = String.valueOf(diffInHours);
            result += (diffInHours == 1) ? " hr " : " hrs ";
            long minutes = diffInMinutes % 60;
            result += String.valueOf(minutes);
            result += (minutes == 1) ? " min" : " mins";
        } else if (diffInMinutes >= 1) {
            result = String.valueOf(diffInMinutes);
            result += (diffInMinutes == 1) ? " min" : " mins";
        } else {
            result = String.valueOf(diffInSeconds);
            result += (diffInSeconds == 1) ? " second" : " seconds";
        }
        return result + pastOrFuture;

    }
}
