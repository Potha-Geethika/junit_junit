package com.carbo.activitylog.utils;

import com.carbo.activitylog.model.ActivityLogEntry;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

public class ActivityLogUtil {
    private static final DateTimeFormatter startEndTimeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter startEndDateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm");

    public static LocalDate toLocalDate(Date from, ZoneId zoneId) {
        return from.toInstant().atZone(zoneId).toLocalDate();
    }

    public static LocalDate toLocalDate(Long from) {
        return Instant.ofEpochMilli(from).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static LocalDateTime convertToLocalDateTime(String time) {
        if (time != null) {
            if (time.length() == 5) {
                LocalDate curDate = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                LocalTime curTime = LocalTime.parse(time, startEndTimeFormatter);
                return LocalDateTime.of(curDate, curTime);
            } else {
                return LocalDateTime.parse(time, startEndDateTimeFormatter);
            }
        }
        else {
            return null;
        }
    }

    public static Float getTotalPumpTimeInMins(List<ActivityLogEntry> pumpTimeActivityLogEntries) {
        return pumpTimeActivityLogEntries
                .stream()
                .map(x -> x.getMillisecondsSpan())
                .reduce(0L, Long::sum)/60000.0f;
    }

    public static String formatTimeWithDate(String hhMM, ZonedDateTime date, DateTimeFormatter formatter) {
        LocalDate localDate = date.toLocalDate();
        ZonedDateTime tmp = ZonedDateTime.of(localDate.atTime(LocalTime.parse(hhMM)), date.getZone());
        return tmp.format(formatter);
    }
}
