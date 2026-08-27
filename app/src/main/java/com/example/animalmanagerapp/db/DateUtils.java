package com.example.animalmanagerapp.db;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Small helper around date formatting and day-difference maths, kept in one
 * place so every screen calculates "days until due" the same way.
 * Dates are always stored/compared in ISO format (yyyy-MM-dd) so they sort
 * correctly as plain text in SQLite.
 */
public class DateUtils {

    public static final String DB_FORMAT = "yyyy-MM-dd";
    public static final String DISPLAY_FORMAT = "dd MMM yyyy";

    public static String todayIso() {
        SimpleDateFormat sdf = new SimpleDateFormat(DB_FORMAT, Locale.getDefault());
        return sdf.format(new Date());
    }

    public static String isoDateNDaysFromNow(int days) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, days);
        SimpleDateFormat sdf = new SimpleDateFormat(DB_FORMAT, Locale.getDefault());
        return sdf.format(cal.getTime());
    }

    public static String toDisplayFormat(String isoDate) {
        if (isoDate == null || isoDate.isEmpty()) return "";
        try {
            SimpleDateFormat in = new SimpleDateFormat(DB_FORMAT, Locale.getDefault());
            SimpleDateFormat out = new SimpleDateFormat(DISPLAY_FORMAT, Locale.getDefault());
            Date date = in.parse(isoDate);
            return date != null ? out.format(date) : isoDate;
        } catch (ParseException e) {
            return isoDate;
        }
    }

    public static boolean isValidIsoDate(String isoDate) {
        if (isoDate == null || isoDate.trim().isEmpty()) return false;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DB_FORMAT, Locale.getDefault());
            sdf.setLenient(false);
            sdf.parse(isoDate);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    /**
     * Positive = days remaining until the date, 0 = today, negative = days
     * overdue/since. Returns Integer.MIN_VALUE if the date can't be parsed.
     */
    public static int daysUntil(String targetIsoDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DB_FORMAT, Locale.getDefault());
            Date target = sdf.parse(targetIsoDate);
            Date today = sdf.parse(todayIso());
            if (target == null || today == null) return Integer.MIN_VALUE;
            long diffMillis = target.getTime() - today.getTime();
            return (int) (diffMillis / (1000 * 60 * 60 * 24));
        } catch (ParseException e) {
            return Integer.MIN_VALUE;
        }
    }

    /**
     * Days that have passed since a past date (e.g. last treatment).
     * Returns Integer.MIN_VALUE if the date can't be parsed.
     */
    public static int daysSince(String pastIsoDate) {
        int until = daysUntil(pastIsoDate);
        return (until == Integer.MIN_VALUE) ? until : -until;
    }

    /** True if {@code laterIso} is strictly after {@code earlierIso}. */
    public static boolean isAfter(String laterIso, String earlierIso) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DB_FORMAT, Locale.getDefault());
            Date later = sdf.parse(laterIso);
            Date earlier = sdf.parse(earlierIso);
            return later != null && earlier != null && later.after(earlier);
        } catch (ParseException e) {
            return false;
        }
    }
}