package com.example.animalmanagerapp.analytics;

import com.example.animalmanagerapp.model.OutputLog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Groups a list of OutputLog entries into daily, weekly, or monthly
 * totals for charting. Buckets are returned oldest-first so a bar chart
 * reads left-to-right chronologically.
 */
public class OutputAggregator {

    private static final String ISO_FORMAT = "yyyy-MM-dd";

    public static Map<String, Float> getDailyTotals(List<OutputLog> logs, int daysBack) {
        Map<String, Float> buckets = new LinkedHashMap<>();
        SimpleDateFormat iso = new SimpleDateFormat(ISO_FORMAT, Locale.getDefault());
        SimpleDateFormat label = new SimpleDateFormat("dd MMM", Locale.getDefault());

        Calendar cal = Calendar.getInstance();
        List<String> keys = new ArrayList<>();
        for (int i = daysBack - 1; i >= 0; i--) {
            Calendar day = (Calendar) cal.clone();
            day.add(Calendar.DAY_OF_YEAR, -i);
            String isoKey = iso.format(day.getTime());
            String labelKey = label.format(day.getTime());
            buckets.put(labelKey, 0f);
            keys.add(isoKey);
        }

        for (OutputLog log : logs) {
            int idx = keys.indexOf(log.getOutputDate());
            if (idx >= 0) {
                String labelKey = new ArrayList<>(buckets.keySet()).get(idx);
                buckets.put(labelKey, buckets.get(labelKey) + parseOrZero(log.getQuantity()));
            }
        }
        return buckets;
    }

    public static Map<String, Float> getWeeklyTotals(List<OutputLog> logs, int weeksBack) {
        Map<String, Float> buckets = new LinkedHashMap<>();
        SimpleDateFormat iso = new SimpleDateFormat(ISO_FORMAT, Locale.getDefault());
        SimpleDateFormat label = new SimpleDateFormat("dd MMM", Locale.getDefault());

        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        List<Calendar[]> weekRanges = new ArrayList<>();
        List<String> weekLabels = new ArrayList<>();
        for (int i = weeksBack - 1; i >= 0; i--) {
            Calendar start = (Calendar) cal.clone();
            start.add(Calendar.WEEK_OF_YEAR, -i);
            Calendar end = (Calendar) start.clone();
            end.add(Calendar.DAY_OF_YEAR, 6);
            weekRanges.add(new Calendar[]{start, end});
            String weekLabel = label.format(start.getTime());
            weekLabels.add(weekLabel);
            buckets.put(weekLabel, 0f);
        }

        for (OutputLog logEntry : logs) {
            try {
                java.util.Date logDate = iso.parse(logEntry.getOutputDate());
                if (logDate == null) continue;
                for (int i = 0; i < weekRanges.size(); i++) {
                    Calendar start = weekRanges.get(i)[0];
                    Calendar end = weekRanges.get(i)[1];
                    if (!logDate.before(start.getTime()) && !logDate.after(end.getTime())) {
                        String key = weekLabels.get(i);
                        buckets.put(key, buckets.get(key) + parseOrZero(logEntry.getQuantity()));
                        break;
                    }
                }
            } catch (Exception ignored) { }
        }
        return buckets;
    }

    public static Map<String, Float> getMonthlyTotals(List<OutputLog> logs, int monthsBack) {
        Map<String, Float> buckets = new LinkedHashMap<>();
        SimpleDateFormat monthKeyFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        SimpleDateFormat label = new SimpleDateFormat("MMM yyyy", Locale.getDefault());

        Calendar cal = Calendar.getInstance();
        List<String> monthKeys = new ArrayList<>();
        List<String> monthLabels = new ArrayList<>();
        for (int i = monthsBack - 1; i >= 0; i--) {
            Calendar month = (Calendar) cal.clone();
            month.add(Calendar.MONTH, -i);
            String key = monthKeyFormat.format(month.getTime());
            String labelText = label.format(month.getTime());
            monthKeys.add(key);
            monthLabels.add(labelText);
            buckets.put(labelText, 0f);
        }

        for (OutputLog logEntry : logs) {
            String outputMonthKey = logEntry.getOutputDate().length() >= 7
                    ? logEntry.getOutputDate().substring(0, 7) : "";
            int idx = monthKeys.indexOf(outputMonthKey);
            if (idx >= 0) {
                String key = monthLabels.get(idx);
                buckets.put(key, buckets.get(key) + parseOrZero(logEntry.getQuantity()));
            }
        }
        return buckets;
    }

    private static float parseOrZero(String text) {
        try { return Float.parseFloat(text.trim()); } catch (Exception e) { return 0f; }
    }
}