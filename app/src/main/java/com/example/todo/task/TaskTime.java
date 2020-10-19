package com.example.todo.task;

public class TaskTime {
    int hour;
    int min;

    TaskTime() {
        hour = min = 0;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public void setTime(int hour, int min) {
        this.hour = hour;
        this.min = min;
    }
}