package com.fitness.common.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * 日期工具类
 * 提供日期相关的工具方法，如日期格式化、日期解析等
 * 例：
 * <pre>
 *     // 格式化日期
 *     String formattedDate = DateUtils.formatDate(LocalDate.now());
 *     // 解析日期
 *     LocalDate parsedDate = DateUtils.parseDate("2023-12-31");
 * </pre>
 */
public class DateUtils {

    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";
    public static final String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT);
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATETIME_FORMAT);

    private DateUtils() {
    }

    public static String formatDate(LocalDate date) {
        return date == null ? null : date.format(DATE_FORMATTER);
    }

    public static String formatDate(LocalDate date, String pattern) {
        if (date == null) {
            return null;
        }
        return date.format(DateTimeFormatter.ofPattern(pattern));
    }

    public static String formatTime(LocalTime time) {
        return time == null ? null : time.format(TIME_FORMATTER);
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(DATETIME_FORMATTER);
    }

    public static String formatDateTime(LocalDateTime dateTime, String pattern) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    public static LocalDate parseDate(String dateStr) {
        return dateStr == null || dateStr.isEmpty() ? null : LocalDate.parse(dateStr, DATE_FORMATTER);
    }

    public static LocalDate parseDate(String dateStr, String pattern) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(pattern));
    }

    public static LocalDateTime parseDateTime(String dateTimeStr) {
        return dateTimeStr == null || dateTimeStr.isEmpty() ? null : LocalDateTime.parse(dateTimeStr, DATETIME_FORMATTER);
    }

    public static LocalDateTime parseDateTime(String dateTimeStr, String pattern) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern(pattern));
    }

    public static LocalDateTime getStartOfDay(LocalDate date) {
        return date == null ? null : date.atStartOfDay();
    }

    public static LocalDateTime getEndOfDay(LocalDate date) {
        return date == null ? null : date.atTime(LocalTime.MAX);
    }

    public static long daysBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    public static long hoursBetween(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (startDateTime == null || endDateTime == null) {
            return 0;
        }
        return ChronoUnit.HOURS.between(startDateTime, endDateTime);
    }

    public static long minutesBetween(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (startDateTime == null || endDateTime == null) {
            return 0;
        }
        return ChronoUnit.MINUTES.between(startDateTime, endDateTime);
    }

    public static LocalDateTime plusDays(LocalDateTime dateTime, long days) {
        return dateTime == null ? null : dateTime.plusDays(days);
    }

    public static LocalDateTime minusDays(LocalDateTime dateTime, long days) {
        return dateTime == null ? null : dateTime.minusDays(days);
    }

    public static LocalDateTime plusHours(LocalDateTime dateTime, long hours) {
        return dateTime == null ? null : dateTime.plusHours(hours);
    }

    public static LocalDateTime minusHours(LocalDateTime dateTime, long hours) {
        return dateTime == null ? null : dateTime.minusHours(hours);
    }

    public static LocalDateTime getCurrentDateTime() {
        return LocalDateTime.now();
    }

    public static LocalDate getCurrentDate() {
        return LocalDate.now();
    }

    public static String getCurrentDateTimeStr() {
        return formatDateTime(LocalDateTime.now());
    }

    public static String getCurrentDateStr() {
        return formatDate(LocalDate.now());
    }
}
