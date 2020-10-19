package com.example.todo.task;

public interface ClickListener {

    void onPositionClicked(int position);
    void onCardClicked(int position);
    void onDateClicked(int position);
    void onTimeClicked(int position);
}