package com.example.todo.task;

public interface RefreshListener {
    void onInboxRefresh();
    void onProjectsRefresh();
    void onTodayRefresh();
    void onUpcomingRefresh();
}
