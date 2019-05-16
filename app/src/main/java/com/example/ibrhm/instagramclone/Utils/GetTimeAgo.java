package com.example.ibrhm.instagramclone.Utils;

import android.text.format.DateFormat;

import java.util.Calendar;

public class GetTimeAgo {

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;


    public static String getTimeAgo(long time) {
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return "NOW";
        }

        // TODO: localize
        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return "JUST NOW";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "1 MINUTE AGO";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " MINUTES AGO";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "1 HOUR AGO";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " HOURS AGO";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "1 DAYS AGO";
        } else {
            return diff / DAY_MILLIS + " DAYS AGO";
        }
    }

    public static String getTimeAgoSmall(long time) {
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return "now";
        }

        // TODO: localize
        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return diff / SECOND_MILLIS + 1 + "s";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "1m";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + "m";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "1h";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + "h";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "1d";
        } else {
            return diff / DAY_MILLIS + "d";
        }
    }

    public static String getMessageAgo(long time){

        Calendar mTime = Calendar.getInstance();
        mTime.setTimeInMillis(time);

        Calendar now = Calendar.getInstance();

        String timeFormatString;
        String dateTimeFormatString;
        String yearTimeFormatString;

        timeFormatString = "h:mm aa";
        dateTimeFormatString = "MMMM d, h:mm aa";
        yearTimeFormatString = "dd/MM/yy h:mm aa";

        if(now.get(Calendar.YEAR) != mTime.get(Calendar.YEAR)){
            return DateFormat.format(yearTimeFormatString, mTime).toString();
        } else if (now.get(Calendar.DATE) == mTime.get(Calendar.DATE)
                && now.get(Calendar.MONTH) == mTime.get(Calendar.MONTH)) {
            return "Today " + DateFormat.format(timeFormatString, mTime);
        } else if (now.get(Calendar.DATE) - mTime.get(Calendar.DATE) == 1
                && now.get(Calendar.MONTH) == mTime.get(Calendar.MONTH)){
            return "Yesterday " + DateFormat.format(timeFormatString, mTime);
        } else {
            return DateFormat.format(dateTimeFormatString, mTime).toString();
        }
    }
}
