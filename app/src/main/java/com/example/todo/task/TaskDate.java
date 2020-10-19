package com.example.todo.task;

public class TaskDate {
    int day;
    int month;
    int year;

    TaskDate() {
        day = month = year = 0;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setDate(int day, int month, int year) {
        this.day = day;
        this.month = month;
        this.year = year;
    }
};