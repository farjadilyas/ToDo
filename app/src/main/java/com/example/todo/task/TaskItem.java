package com.example.todo.task;

public class TaskItem {

    private int mCheckImageResource, mCalendarImageResource, taskID;
    private String mTaskText, mDateText, mTimeText, key;

    public TaskItem(int mCheckImageResource, int mCalendarImageResource, String mTaskText, String mDateText, String mTimeText, String key, int taskID) {
        this.mCheckImageResource = mCheckImageResource;
        this.mCalendarImageResource = mCalendarImageResource;
        this.mTaskText = mTaskText;
        this.mDateText = mDateText;
        this.mTimeText = ((mDateText.equals(""))?"00:00":mTimeText);
        this.key = key;
        this.taskID = taskID;
    }

    public int getCheckImageResource() {
        return mCheckImageResource;
    }

    public int getCalendarImageResource() {
        return mCalendarImageResource;
    }

    public String getTaskText() {
        return mTaskText;
    }

    public String getDateText() {
        return mDateText;
    }

    public String getTimeText() {
        return mTimeText;
    }

    public String getKey() { return key; }

    public int getTaskID() { return taskID; }

    public void setTaskID(int taskID) { this.taskID = taskID; }

    public void setTaskText(String text) {mTaskText = text; }

    public void setDateText(String date) {
        this.mDateText = date;
    }

    public void setTimeText(String time) {
        this.mTimeText = ((mDateText.equals(""))? "" : time);
    }

    public void setKey(String key) {this.key = key; }
}
