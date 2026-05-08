package com.carbo.activitylog.model;

public class ActivityDay implements Comparable<ActivityDay> {
    private Integer day;
    private Double totalHours;

    public ActivityDay(Integer day, Double totalHours) {
        this.day = day;
        this.totalHours = totalHours;
    }

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public Double getTotalHours() {
        return totalHours;
    }

    public void setTotalHours(Double totalHours) {
        this.totalHours = totalHours;
    }

    @Override
    public int compareTo(ActivityDay o) {
        return this.day.compareTo(o.getDay());
    }
}
