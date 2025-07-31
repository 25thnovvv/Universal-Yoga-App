package com.example.universalyogaapp.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class YogaDateUtils {

    // Day of week mapping
    private static final String[] DAYS_OF_WEEK = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    
    // Calendar constants
    private static final int DAYS_IN_WEEK = 7;
    private static final String DEFAULT_LOCALE = "Not scheduled";
    private static final String INVALID_SCHEDULE = "Invalid schedule";

    /**
     * Converts day name to calendar day constant
     */
    private static int convertDayNameToCalendarConstant(String dayName) {
        String normalizedDay = dayName.toLowerCase().trim();
        switch (normalizedDay) {
            case "sunday": return Calendar.SUNDAY;
            case "monday": return Calendar.MONDAY;
            case "tuesday": return Calendar.TUESDAY;
            case "wednesday": return Calendar.WEDNESDAY;
            case "thursday": return Calendar.THURSDAY;
            case "friday": return Calendar.FRIDAY;
            case "saturday": return Calendar.SATURDAY;
            default: return -1;
        }
    }

    /**
     * Calculates the next upcoming class date based on schedule
     */
    public static String calculateNextUpcomingDate(String weeklySchedule) {
        if (weeklySchedule == null || weeklySchedule.trim().isEmpty()) {
            return DEFAULT_LOCALE;
        }

        List<String> scheduledDays = Arrays.asList(weeklySchedule.split(","));
        List<Calendar> upcomingDates = new ArrayList<>();
        Calendar currentDate = Calendar.getInstance();

        for (String dayName : scheduledDays) {
            int targetDayConstant = convertDayNameToCalendarConstant(dayName);
            if (targetDayConstant == -1) continue;

            Calendar nextClassDate = (Calendar) currentDate.clone();
            int currentDayOfWeek = nextClassDate.get(Calendar.DAY_OF_WEEK);

            int daysToAdd = targetDayConstant - currentDayOfWeek;
            if (daysToAdd <= 0) {
                daysToAdd += DAYS_IN_WEEK;
            }
            nextClassDate.add(Calendar.DAY_OF_YEAR, daysToAdd);
            upcomingDates.add(nextClassDate);
        }

        if (upcomingDates.isEmpty()) {
            return INVALID_SCHEDULE;
        }

        Collections.sort(upcomingDates);
        Calendar nextUpcomingDate = upcomingDates.get(0);

        SimpleDateFormat dateFormatter = new SimpleDateFormat("EEE, MMM d", Locale.getDefault());
        return dateFormatter.format(nextUpcomingDate.getTime());
    }

    // Legacy method for backward compatibility
    public static String getNextUpcomingDate(String schedule) {
        return calculateNextUpcomingDate(schedule);
    }

    /**
     * Converts day name to calendar day constant (legacy method)
     */
    private static int getDayOfWeekAsInt(String day) {
        return convertDayNameToCalendarConstant(day);
    }
} 