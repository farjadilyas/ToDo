package com.example.todo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.example.todo.task.TaskActivity;
import com.example.todo.login.LoginActivity;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

//import static com.example.todo.Util.database;
//import static com.example.todo.Util.mAuth;

import static com.example.todo.ToDoApplication.databaseReference;
import static com.example.todo.ToDoApplication.mAuth;


public class MainActivity extends Util {

    public static void wait(int ms)
    {
        try
        {
            Thread.sleep(ms);
        }
        catch(InterruptedException ex)
        {
            Thread.currentThread().interrupt();
        }
    }

    SharedPreferences preferences = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        switch (ThemeVar.getData())
        {
            case(0):
                setTheme(R.style.BlackTheme);
                break;
            case(1):
                setTheme(R.style.RedTheme);
                break;
            case(2):
                setTheme(R.style.BlueTheme);
                break;
            default:
                setTheme(R.style.BlueTheme);
                break;
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //mAuth.signOut();

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null)
        {
            final DatabaseReference settingsReference = databaseReference.child("users").child(mAuth.getUid()).child("settings");

            settingsReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Boolean syncTheme = (Boolean) snapshot.child("syncTheme").getValue();

                    if (syncTheme != null && syncTheme) {
                        long themeVar = (long) snapshot.child("themeVar").getValue();

                        ThemeVar.setData((int) themeVar);
                    }
                    else {
                        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        ThemeVar.setData(preferences.getInt(getString(R.string.preference_theme_var), 1));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            // User is signed in

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run()
                {
                    Intent i = new Intent(MainActivity.this, TaskActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    finish();
                }
            }, 600);



        }
        else
            {
            // User is signed out

            new Handler().postDelayed(new Runnable() {
            @Override
            public void run()
            {

                Intent i = new Intent(MainActivity.this, LoginActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                finish();
            }
            }, 1200);
        }


    }
}

//