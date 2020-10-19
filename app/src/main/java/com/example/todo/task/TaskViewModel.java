package com.example.todo.task;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.todo.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import static com.example.todo.ToDoApplication.databaseReference;
import static com.example.todo.ToDoApplication.mAuth;

public class TaskViewModel extends ViewModel {

    private DateFormat dateTextFormat = new SimpleDateFormat("dd MMM");
    private DateFormat timeTextFormat = new SimpleDateFormat("HH:mm");

    private CharSequence username, email;

    private ArrayList<TaskItem> taskList;

    private int itemCount = 0;

    MutableLiveData<Boolean> dataLoaded = new MutableLiveData<>();

    public TaskViewModel() {
        taskList = new ArrayList<>();
        dataLoaded.setValue(false);
        initialize();
    }

    private void resetMembers() {
        taskList.clear();
        setItemCount(0);
        setUsername(null);
        setEmail(null);
    }

    LiveData<Boolean> isDataLoaded() {
        return dataLoaded;
    }

    public void initialize() {

        if (mAuth.getUid() == null)
            return;

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (mAuth.getUid() == null || dataSnapshot.child("users").child(mAuth.getUid()).getValue() == null)
                    return;

                resetMembers();

                Log.d("SYNC VAL", String.valueOf((boolean) dataSnapshot.child("users").child(Objects.requireNonNull(mAuth.getUid())).child("isSynced").getValue()));

                if (!((boolean) dataSnapshot.child("users").child(Objects.requireNonNull(mAuth.getUid())).child("isSynced").getValue()))
                    databaseReference.child("users").child(mAuth.getUid()).keepSynced(true);

                //Load user info from database

                setUsername((java.lang.CharSequence) dataSnapshot.child("users").child(Objects.requireNonNull(mAuth.getUid())).child("nickname").getValue());
                setEmail((java.lang.CharSequence) dataSnapshot.child("users").child(Objects.requireNonNull(mAuth.getUid())).child("email").getValue());


                //Load tasks from database

                TaskItem taskItem;
                long taskID;

                long millis;
                Date date = new Date();

                String dateString, timeString;

                for (DataSnapshot list : dataSnapshot.child("users").child(Objects.requireNonNull(mAuth.getUid())).child("list").getChildren())
                {
                    millis = (long) list.child("dateTime").getValue();
                    date.setTime(millis);

                    if (millis == 0) {
                        dateString = timeString = "";
                    }
                    else {
                        dateString = dateTextFormat.format(date);
                        timeString = timeTextFormat.format(date);
                    }


                    if (list.child("taskID").getValue() == null)
                        taskID = -1;
                    else
                        taskID = (Long) list.child("taskID").getValue();

                    taskItem = new TaskItem(R.drawable.ic_unchecked, R.drawable.ic_today, (String) list.child("task").getValue(), dateString,
                            timeString, list.getKey(), (int) taskID);
                    taskList.add(getItemCount(), taskItem);
                    incItemCount();
                }

                //databaseReference.removeEventListener(this);
                dataLoaded.setValue(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

    }

    private void setItemCount(int itemCount) { this.itemCount = itemCount; }

    public void incItemCount() {
        ++this.itemCount;
    }

    public void decItemCount() {
        --this.itemCount;
    }

    public int getItemCount() {
        return itemCount;
    }

    public CharSequence getUsername() {
        return username;
    }

    private void setUsername(CharSequence username) {
        this.username = username;
    }

    public CharSequence getEmail() {
        return email;
    }

    private void setEmail(CharSequence email) {
        this.email = email;
    }

    public DateFormat getDateTextFormat() {
        return dateTextFormat;
    }
    public DateFormat getTimeTextFormat() {
        return timeTextFormat;
    }

    public ArrayList<TaskItem> getTaskList() {
        return taskList;
    }
}
