package com.example.todo.login;

import com.example.todo.settings.UserSettings;

public class User {

    private String email;
    private String list;
    private String nickname;
    private UserSettings settings;
    private int numTasks;
    private boolean isSynced;

    public User(String email, String nickname) {
        setEmail(email);
        setNickname(nickname);
        list = "";
        numTasks = 0;
        isSynced = true;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getList() {
        return list;
    }

    public void setList(String list) {
        this.list = list;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public UserSettings getSettings() {
        return settings;
    }

    public void setSettings(UserSettings settings) {
        this.settings = settings;
    }

    public int getNumTasks() {
        return numTasks;
    }

    public void setNumTasks(int numTasks) {
        this.numTasks = numTasks;
    }

    public boolean isSynced() {
        return isSynced;
    }

    public void setSynced(boolean synced) {
        isSynced = synced;
    }

}