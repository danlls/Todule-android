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
            result = diffInDays + " days";
        } else if (diffInHours >= 1) {
            result = diffInHours + " hours";
        } else if (diffInMinutes >= 1) {
            result = diffInMinutes + " minutes";
        } else {
            result = diffInSeconds + " seconds";
        }
        return result + pastOrFuture;

    }
}
