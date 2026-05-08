package com.carbo.pad.utils;

import com.carbo.pad.model.ActivityLogEntry;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ActivityLogUtil {

    private static final DateTimeFormatter startEndTimeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter startEndDateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm");


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
        return getTotalPumpTimeInMilliSec(pumpTimeActivityLogEntries)/60000.0f;
    }

    public static Float getTotalPumpTimeInMilliSec(List<ActivityLogEntry> pumpTimeActivityLogEntries) {
        return pumpTimeActivityLogEntries
                .stream()
                .map(each -> each.getMillisecondsSpan())
                .reduce(0L, Long::sum).floatValue();
    }

    public static List<Float> getPumpTimeInMinsList(List<ActivityLogEntry> pumpTimeActivityLogEntries) {
        return pumpTimeActivityLogEntries
                .stream()
                .collect(Collectors.groupingBy(
                        ActivityLogEntry::getStage,
                        Collectors.collectingAndThen(
                                Collectors.maxBy(Comparator.comparing(ActivityLogEntry::getModified)),
                                optional -> optional.orElse(null)
                        )
                ))
                .values()
                .stream()
                .filter(Objects::nonNull)
                .map(each -> each.getMillisecondsSpan() / 60000.0f)
                .collect(Collectors.toList());
    }

    public static Double round(Double number, int decimalPlaces) {
        double pow = Math.pow(10, decimalPlaces);
        return Math.round(number * pow) / pow;
    }

    public static Float round(Float number, int decimalPlaces) {
        if (number == null) return 0f;
        double pow = Math.pow(10, decimalPlaces);
        Double result = Math.round(number * pow) / pow;
        return result.floatValue();
    }
}
