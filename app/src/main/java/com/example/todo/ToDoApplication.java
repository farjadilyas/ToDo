package com.example.todo;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ToDoApplication extends Application {

    public static FirebaseAuth mAuth;
    public static FirebaseDatabase database;
    public static DatabaseReference databaseReference;

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseApp.initializeApp(this);

        mAuth = FirebaseAuth.getInstance();

        if (database == null) {
            database = FirebaseDatabase.getInstance();
            database.setPersistenceEnabled(true);
        }

        databaseReference = database.getReference();
    }
}