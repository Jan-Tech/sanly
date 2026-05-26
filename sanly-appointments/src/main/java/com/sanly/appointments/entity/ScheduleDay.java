package com.sanly.appointments.entity;

/**
 * Custom day-of-week enum for availability schedules.
 * Named ScheduleDay (not DayOfWeek) to avoid conflict with java.time.DayOfWeek.
 */
public enum ScheduleDay {
    MON,
    TUE,
    WED,
    THU,
    FRI,
    SAT,
    SUN
}
