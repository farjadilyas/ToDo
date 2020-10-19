package com.example.todo.task;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;

import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.todo.DatePickerFragment;
import com.example.todo.task.frag.InboxFragment;
import com.example.todo.task.frag.ProjectsFragment;
import com.example.todo.QuickEditText;
import com.example.todo.R;
import com.example.todo.TimePickerFragment;
import com.example.todo.task.frag.TodayFragment;
import com.example.todo.task.frag.UpcomingFragment;
import com.example.todo.Util;
import com.example.todo.notification.AlertReceiver;
import com.example.todo.notification.NotificationHelper;
import com.example.todo.settings.SettingsActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.base.Strings;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.Scanner;

import static com.example.todo.ToDoApplication.databaseReference;
import static com.example.todo.ToDoApplication.mAuth;


public class TaskActivity extends Util implements NavigationView.OnNavigationItemSelectedListener, DatePickerDialog.OnDateSetListener, DatePickerDialog.OnCancelListener,
        TimePickerDialog.OnTimeSetListener, ClickListener, RefreshListener {

    //Root tag
    private DrawerLayout drawer;

    //Recycler view items
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    //Fragments accessed by navbar
    InboxFragment inboxFragment;
    TodayFragment todayFragment;
    UpcomingFragment upcomingFragment;
    ProjectsFragment projectsFragment;


    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
    DateFormat dateTextFormat = new SimpleDateFormat("dd MMM"),
            timeTextFormat = new SimpleDateFormat("HH:mm"),
            originalDateFormat = new SimpleDateFormat("yyy/MM/dd");
    Date date = new Date();


    //Methods: Deal with task cards - saving, adding, removing

        //Declarations

    boolean isAddingTask = false, isTaskTextEmpty = true, setBeforeInsert = false;
    //int itemCount = 0;

    public ImageButton addTaskToListButton;

    ArrayList<TaskItem> taskList = new ArrayList<>();   //Stores all tasks currently loaded on screen
    TaskAdapter taskAdapter;

    String[] undoInfo = new String[] {"","","00:00"};

    String undoText = "";
    long undoDateTime = 0, numTasks, cardMillis, undoTaskID = -1, taskID = -1;



        //Methods

    private void saveCardInfo(int position) {

        if (position >= taskList.size()) {
            onInboxRefresh();
            return;
        }

        TaskItem taskItem = taskList.get(position);

        final DatabaseReference ref = databaseReference.child("users").child(mAuth.getUid()).child("list").child(taskItem.getKey());

        if (taskItem.getTaskText()== null)
            undoText = "";
        else
            undoText = Objects.requireNonNull(taskItem.getTaskText());


        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                undoDateTime = (Long) snapshot.child("dateTime").getValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void restoreCard() {
        final HashMap<String, Object> newList = new HashMap<>();
        newList.put("task", undoText);
        newList.put("dateTime", undoDateTime);

        if (undoTaskID != -1) {
            final DatabaseReference ref = databaseReference.child("users").child(Objects.requireNonNull(mAuth.getUid()));

            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    numTasks = (Long) snapshot.child("numTasks").getValue();
                    ref.child("numTasks").setValue(numTasks+1);
                    newList.put("taskID", numTasks);

                    String key = databaseReference.child("users").child(Objects.requireNonNull(mAuth.getUid())).child("list").push().getKey();

                    assert key != null;
                    databaseReference.child("users").child(Objects.requireNonNull(mAuth.getUid())).child("list").child(key).setValue(newList);

                    date.setTime(undoDateTime);
                    taskList.add(taskList.size(), new TaskItem(R.drawable.ic_unchecked, R.drawable.ic_today, undoText, dateTextFormat.format(date.getTime()), timeTextFormat.format(date.getTime()), key, (int) numTasks));
                    taskAdapter.notifyItemInserted(taskList.size());

                    undoText = "";
                    undoDateTime = 0;
                    undoTaskID = -1;

                    startAlarm(undoDateTime, (int) numTasks, "Task: " + undoText, key);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        else {
            String key = databaseReference.child("users").child(Objects.requireNonNull(mAuth.getUid())).child("list").push().getKey();

            assert key != null;
            databaseReference.child("users").child(Objects.requireNonNull(mAuth.getUid())).child("list").child(key).setValue(newList);

            date.setTime(undoDateTime);
            taskList.add(taskList.size(), new TaskItem(R.drawable.ic_unchecked, R.drawable.ic_today, undoText, dateTextFormat.format(date.getTime()), timeTextFormat.format(date.getTime()), key, -1));
            taskAdapter.notifyItemInserted(taskList.size());

            undoText = "";
            undoDateTime = 0;
            undoTaskID = -1;
        }
    }

    public long addCard(final TaskItem taskItem) {
        final HashMap<String, Object> newList = new HashMap<>();
        newList.put("task", taskItem.getTaskText());
        Date temp = new Date();
        cardMillis = 0;

        if (taskItem.getDateText().equals(""))
            newList.put("dateTime", 0);
        else {
            try {
                temp = sdf.parse(taskItem.getDateText() + " " + taskItem.getTimeText());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            assert temp != null;
            cardMillis = temp.getTime();
            taskItem.setDateText(dateTextFormat.format(cardMillis));
        }
        newList.put("dateTime", cardMillis);


        if (notificationEnabled && cardMillis != 0 && !taskItem.getTimeText().equals("00:00")) {
            final DatabaseReference ref = databaseReference.child("users").child(Objects.requireNonNull(mAuth.getUid()));

            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    numTasks = (Long) snapshot.child("numTasks").getValue();
                    ref.child("numTasks").setValue(numTasks+1);
                    newList.put("taskID", numTasks);

                    String key = databaseReference.child("users").child(Objects.requireNonNull(mAuth.getUid())).child("list").push().getKey();

                    assert key != null;

                    databaseReference.child("users").child(Objects.requireNonNull(mAuth.getUid())).child("list").child(key).setValue(newList);
                    taskItem.setKey(key);
                    taskItem.setTaskID((int) numTasks);
                    taskList.add(taskList.size(), taskItem);
                    taskAdapter.notifyItemInserted(taskList.size());
                    timeSet = "00:00";
                    dateSet = "";

                    startAlarm(cardMillis, (int) numTasks, "Task: " + taskItem.getTaskText(), key);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        else {
            String key = databaseReference.child("users").child(Objects.requireNonNull(mAuth.getUid())).child("list").push().getKey();
            assert key != null;
            databaseReference.child("users").child(Objects.requireNonNull(mAuth.getUid())).child("list").child(key).setValue(newList);
            taskItem.setKey(key);
            taskList.add(taskList.size(), taskItem);
            taskAdapter.notifyItemInserted(taskList.size());
            timeSet = "00:00";
            dateSet = "";
        }

        inboxFragment.hideBanner();

        return cardMillis;
    }

    public void removeCard(final int position) {

        if (position >= taskList.size()) {
            onInboxRefresh();
            return;
        }

        final TaskItem taskItem = taskList.get(position);

        DatabaseReference ref = databaseReference.child("users").child(mAuth.getUid()).child("list");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (taskItem.getTaskID() != -1)
                    cancelAlarm(taskItem.getTaskID());

                if (snapshot.child(taskItem.getKey()).getValue() == null) {
                    onInboxRefresh();
                }
                else {
                    taskList.remove(position);
                    taskAdapter.notifyItemRemoved(position);
                    databaseReference.child("users").child(mAuth.getUid()).child("list").child(taskItem.getKey()).setValue(null);
                    if (taskList.size() == 0)
                        inboxFragment.displayBanner();
                }



            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }




    //Methods: deal with updating date and time for a task

        //Declarations

    OnDateTimeUpdatedListener dateTimeUpdatedListener;

    String dateSelected = "", dateSet = "", timeSelected = "00:00", timeSet = "00:00";

    public interface OnDateTimeUpdatedListener {
        void OnDateClicked();
        void OnTimeClicked();
    }


        //Methods

    public void updateDate(int position) {
/*
        long millis = 0;
        TaskItem taskItem = taskList.get(position);

        if (!dateSelected.equals("")) {
            Date date1 = null;
            try {
                date1 = sdf.parse(dateSelected + " " + timeSelected);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            assert date1 != null;
            millis = date1.getTime();
        }

        databaseReference.child("users").child(mAuth.getUid()).child("list").child(taskItem.getKey()).child("dateTime").setValue(millis);
        taskItem.setDateText(dateTextFormat.format(millis));
        taskAdapter.notifyItemChanged(position);

        if (taskItem.getTaskID() > 0)
            startAlarm(millis, taskItem.getTaskID(), taskItem.getTaskText(), taskItem.getKey());*/
    }

    public void updateTime(int position) {

        TaskItem taskItem = taskList.get(position);
        long millis = 0;

        if (!dateSelected.equals("")) {
            Date date1 = null;
            try {
                date1 = sdf.parse(dateSelected + " " + timeSelected);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            assert date1 != null;
            millis = date1.getTime();
        }

        taskItem.setDateText(dateTextFormat.format(millis));
        taskItem.setTimeText(timeSelected);
        taskAdapter.notifyItemChanged(position);
        databaseReference.child("users").child(mAuth.getUid()).child("list").child(taskItem.getKey()).child("dateTime").setValue(millis);

        if (taskItem.getTaskID() > 0)
            startAlarm(millis, taskItem.getTaskID(), taskItem.getTaskText(), taskItem.getKey());
    }




    // Set of buttons below 'Add a task field' - declaration and methods

    LinearLayout addTaskPanel, ImageTextButtonDate, ImageTextButtonTime, ImageTextButtonAlarm, ImageTextButtonNotification;
    TextView ImageTextButtonDateText, ImageTextButtonTimeText, ImageTextButtonAlarmText, ImageTextButtonNotificationText;
    ImageView addTaskPanelImg, ImageTextButtonDateImg, ImageTextButtonTimeImg, ImageTextButtonAlarmImg, ImageTextButtonNotificationImg;

    boolean notificationEnabled, alarmEnabled ;

    QuickEditText addTaskEditText;

    View headerView;
    TextView navUsername, navEmail;
    NavigationView navigationView;


    public void imageTextButtonClicked(LinearLayout it_btn, TextView it_btn_txt, ImageView it_btn_img, String newString) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getTheme();
        theme.resolveAttribute(R.attr.negativeTextColor, typedValue, true);
        @ColorInt int color = typedValue.data;

        it_btn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.transparent_rounded_btn_light_pressed, getTheme()));
        it_btn_txt.setText(newString);
        it_btn_txt.setTextColor(color);
        it_btn_img.setColorFilter(color);
    }

    public void imageTextButtonReset(LinearLayout it_btn, TextView it_btn_txt, ImageView it_btn_img, int res_id) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getTheme();
        theme.resolveAttribute(R.attr.foregroundColor, typedValue, true);
        @ColorInt int color = typedValue.data;

        it_btn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.transparent_rounded_btn_light, getTheme()));
        it_btn_txt.setText(res_id);
        it_btn_txt.setTextColor(color);
        it_btn_img.setColorFilter(color);
    }

    public void resetAllImageTextButtons() {
        imageTextButtonReset(ImageTextButtonDate, ImageTextButtonDateText, ImageTextButtonDateImg, R.string.set_date);
        imageTextButtonReset(ImageTextButtonTime, ImageTextButtonTimeText, ImageTextButtonTimeImg, R.string.set_time);
        imageTextButtonReset(ImageTextButtonAlarm, ImageTextButtonAlarmText, ImageTextButtonAlarmImg, R.string.enable_alarm);
        imageTextButtonReset(ImageTextButtonNotification, ImageTextButtonNotificationText, ImageTextButtonNotificationImg, R.string.enable_notification);
        notificationEnabled = false;
    }


    private void retrievePersistentData(boolean firstFragment) {
        taskList = taskViewModel.getTaskList();
        //itemCount = taskViewModel.getItemCount();

        navUsername.setText(taskViewModel.getUsername());
        navEmail.setText(taskViewModel.getEmail());


        taskAdapter = new TaskAdapter(taskList, this);

        inboxFragment = new InboxFragment(taskAdapter, this);

        if (firstFragment) {
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container,
                    inboxFragment).commit();
        }
        else {

            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    inboxFragment).commit();
        }

        if (taskList.size() == 0)
            inboxFragment.displayBanner();
        else
            inboxFragment.hideBanner();

        navigationView.setCheckedItem(R.id.nav_inbox);
    }



    private NotificationHelper notificationHelper;

    private void startAlarm(long timeInMillis, int notificationID, String message, String listID) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, AlertReceiver.class);
        intent.putExtra("message", message);
        intent.putExtra("notificationID", notificationID);
        intent.putExtra("listID", listID);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), notificationID, intent, 0);

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
    }

    private void cancelAlarm(int notificationID) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, notificationID, intent, 0);

        alarmManager.cancel(pendingIntent);
    }


    private TaskViewModel taskViewModel;

    @Override
    public void onResume() {
        super.onResume();

        themeSelect(this);
        retrievePersistentData(false);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        themeSelect(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        notificationHelper = new NotificationHelper(this);

        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        new LoadDataTask().execute("http://b299381268bb.ngrok.io/user_details");



        drawer = findViewById(R.id.drawer_layout);
        addTaskToListButton = findViewById(R.id.add_task_to_list_button);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        //Get nav-bar, its header's nickname and email address fields

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        headerView = navigationView.getHeaderView(0);
        navUsername = headerView.findViewById(R.id.header_nickname);
        navEmail = headerView.findViewById(R.id.header_email);

        databaseReference.child("users").child(Objects.requireNonNull(mAuth.getUid())).keepSynced(true);



        // Initialize taskAdapter with ClickListener - when app is first opened

        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);
        retrievePersistentData(true);

        taskViewModel.isDataLoaded().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    if (taskList.size() == 0) {
                        inboxFragment.displayBanner();
                    }
                    else {
                        inboxFragment.hideBanner();
                    }
                }
            }
        });

        //Assigning all items in the add task panel

        final LinearLayout taskLinearLayout = findViewById(R.id.task_linear_layout);
        final FloatingActionButton addTaskButton = findViewById(R.id.add_task_button);
        addTaskPanel = findViewById(R.id.add_task_panel);
        addTaskEditText = findViewById(R.id.add_task_input);
        final ImageButton addTaskToListButton = findViewById(R.id.add_task_to_list_button);
        addTaskToListButton.setEnabled(false);

        ImageTextButtonDate = findViewById(R.id.it_btn_date);
        ImageTextButtonDateText = findViewById(R.id.it_btn_date_text);
        ImageTextButtonDateImg = findViewById(R.id.it_btn_date_img);
        ImageTextButtonTime = findViewById(R.id.it_btn_time);
        ImageTextButtonTimeText = findViewById(R.id.it_btn_time_text);
        ImageTextButtonTimeImg = findViewById(R.id.it_btn_time_img);
        ImageTextButtonAlarm = findViewById(R.id.it_btn_alarm);
        ImageTextButtonAlarmText = findViewById(R.id.it_btn_alarm_text);
        ImageTextButtonAlarmImg = findViewById(R.id.it_btn_alarm_img);
        ImageTextButtonNotification = findViewById(R.id.it_btn_notification);
        ImageTextButtonNotificationText = findViewById(R.id.it_btn_notification_text);
        ImageTextButtonNotificationImg = findViewById(R.id.it_btn_notification_img);




        //Handling OnClick Events for buttons below 'add a task' field

        ImageTextButtonDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

                setBeforeInsert = true;
                DialogFragment datePicker = new DatePickerFragment();
                datePicker.show(getSupportFragmentManager(), "date picker");
            }
        });

        ImageTextButtonTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

                setBeforeInsert = true;
                DialogFragment timePicker = new TimePickerFragment();
                timePicker.show(getSupportFragmentManager(), "time picker");
            }
        });

        ImageTextButtonAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            }
        });

        ImageTextButtonNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

                if (notificationEnabled) {
                    imageTextButtonReset(ImageTextButtonNotification, ImageTextButtonNotificationText,
                            ImageTextButtonNotificationImg, R.string.enable_notification);
                    notificationEnabled = false;
                }
                else {
                    imageTextButtonClicked(ImageTextButtonNotification, ImageTextButtonNotificationText,
                            ImageTextButtonNotificationImg, getResources().getString(R.string.notification_enabled));
                    
                    notificationEnabled = true;
                }
            }
        });




        //Follow input text chances (toggle whether adding a card is allowed)

        addTaskEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if ((addTaskEditText.getText() == null || addTaskEditText.getText().toString().isEmpty()) && !isTaskTextEmpty) {
                    addTaskToListButton.setImageResource(R.drawable.ic_send_circle_outline);
                    isTaskTextEmpty = true;
                    addTaskToListButton.setEnabled(false);
                }
                else if ((addTaskEditText.getText() != null && !addTaskEditText.getText().toString().isEmpty()) && isTaskTextEmpty) {
                    addTaskToListButton.setImageResource(R.drawable.ic_send_circle_fill);
                    isTaskTextEmpty = false;
                    addTaskToListButton.setEnabled(true);
                }
            }
        });




        //Override KeyIme (back pressed while keyboard is displayed)
        //hides keyboard + add task panel

        addTaskEditText.setKeyImeChangeListener(new QuickEditText.KeyImeChange(){
            @Override
            public void onKeyIme(int keyCode, KeyEvent event)
            {
                if (KeyEvent.KEYCODE_BACK == event.getKeyCode())
                {
                    addTaskPanel.setAnimation(outToRightAnimation(200));
                    addTaskPanel.setVisibility(View.GONE);

                    resetAllImageTextButtons();

                    addTaskButton.setAnimation(inFromRightAnimation(300));
                    addTaskButton.setVisibility(View.VISIBLE);
                    taskLinearLayout.getBackground().clearColorFilter();
                    addTaskEditText.setText("");
                    addTaskToListButton.setImageResource(R.drawable.ic_send_circle_outline);
                    isTaskTextEmpty = true;

                    dateSet = "";
                    timeSet = "00:00";
                }
            }});


            


        //To request focus for add a task field without having to click it

        final Runnable r = new Runnable() {
            @Override
            public void run() {
                addTaskEditText.requestFocusFromTouch();
                addTaskEditText.requestFocus();
            }
        };


        //Bring up keyboard, take EditText focus, bring in Add a Task panel

        addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

                addTaskButton.requestFocusFromTouch();
                addTaskButton.requestFocus();

                addTaskButton.postDelayed(r,300);

                imm.toggleSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(), InputMethodManager.SHOW_FORCED, 0);

                isAddingTask = true;
                taskLinearLayout.getBackground().setColorFilter(Color.argb(110,0,0,0), PorterDuff.Mode.DARKEN);
                addTaskPanel.setAnimation(inFromRightAnimation(200));
                addTaskButton.setAnimation(outToRightAnimation(200));
                addTaskPanel.setVisibility(View.VISIBLE);
                addTaskButton.setVisibility(View.GONE);
            }
        });


        addTaskToListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                long millis = addCard(new TaskItem(R.drawable.ic_unchecked, R.drawable.ic_today,
                        Objects.requireNonNull(addTaskEditText.getText()).toString(), dateSet, timeSet, "", -1));
                addTaskEditText.setText("");
                isTaskTextEmpty = true;
                isTaskTextEmpty = true;

                resetAllImageTextButtons();
            }
        });

    }



    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case (R.id.nav_inbox):
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        inboxFragment).commit();
                break;
            case (R.id.nav_today):
                todayFragment = new TodayFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        todayFragment).commit();
                break;
            case (R.id.nav_upcoming):
                upcomingFragment = new UpcomingFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        upcomingFragment).commit();
                break;
            case (R.id.nav_projects):
                projectsFragment = new ProjectsFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        projectsFragment).commit();
                break;
            case (R.id.nav_settings):
                Intent i = new Intent(TaskActivity.this, SettingsActivity.class);
                //i.putExtra("themeChange", false);
                startActivity(i);
                break;
        }

        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else if (isAddingTask) {
            addTaskEditText.setText("");
            addTaskPanel.setVisibility(View.GONE);
            resetAllImageTextButtons();
            addTaskToListButton.setImageResource(R.drawable.ic_send_circle_outline);
            isTaskTextEmpty = true;
            isAddingTask = false;
            dateSet = "";
            timeSet = "00:00";
        }
        else {
            super.onBackPressed();
        }
    }


    //DatePickerFragment - when user selects a date

    public String padWithZeroes(String text, int length) {
        String pad = Strings.repeat("0", length);
        return (pad + text).substring(text.length());
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

        /*
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DATE, dayOfMonth);

        String monthStr = c.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault());*/
        dateSelected = padWithZeroes(Integer.toString(year),4) + "/" + ((month+1 < 10)? "0" + (month+1) : "" + (month+1))
                + "/" + ((dayOfMonth < 10)?"0" + dayOfMonth : "" + dayOfMonth);

        if (setBeforeInsert) {
            dateSet = dateSelected;
            setBeforeInsert = false;
            imageTextButtonClicked(ImageTextButtonDate, ImageTextButtonDateText, ImageTextButtonDateImg, dateSet);
        }
        else
            dateTimeUpdatedListener.OnDateClicked();
    }


    //TimePickerFragment - when user picks a time

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

        //Pad with zero if either number if < 10
        //timeSelected = ((hourOfDay < 10)?"0" + hourOfDay : hourOfDay) + ":" + ((minute < 10)?"0" + minute: minute);

        timeSelected = ((hourOfDay < 10)?"0" + hourOfDay : "" + hourOfDay) + ":" + ((minute < 10)?"0" + minute : "" + minute);

        if (setBeforeInsert) {
            timeSet = timeSelected;
            setBeforeInsert = false;
            imageTextButtonClicked(ImageTextButtonTime, ImageTextButtonTimeText, ImageTextButtonTimeImg, timeSet);
        }
        else
            dateTimeUpdatedListener.OnTimeClicked();
    }


    //If user cancels setting a date/time

    @Override
    public void onCancel(DialogInterface dialog) {
        if (setBeforeInsert)
            setBeforeInsert = false;
    }


    @Override
    public void onPositionClicked(int position) {
        saveCardInfo(position);
        removeCard(position);

        Snackbar snackbar = Snackbar
                .make(drawer, "Completed.", Snackbar.LENGTH_LONG)
                .setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        restoreCard();

                        Snackbar snackbar1 = Snackbar.make(drawer, "Message is restored!", Snackbar.LENGTH_SHORT);
                        snackbar1.show();
                    }
                });

        snackbar.show();
    }


    @Override
    public void onDateClicked(final int position) {
        DialogFragment datePicker = new DatePickerFragment();
        datePicker.show(getSupportFragmentManager(), "date picker");

        dateTimeUpdatedListener = new OnDateTimeUpdatedListener() {
            @Override
            public void OnDateClicked() {
                updateDate(position);

                DialogFragment timePicker = new TimePickerFragment();
                timePicker.show(getSupportFragmentManager(), "time picker");
            }

            @Override
            public void OnTimeClicked() {
                updateTime(position);
            }
        };
    }


    @Override
    public void onTimeClicked(final int position) {

        DialogFragment timePicker = new TimePickerFragment();
        timePicker.show(getSupportFragmentManager(), "time picker");

        dateTimeUpdatedListener = new OnDateTimeUpdatedListener() {

            @Override
            public void OnDateClicked() {
            }

            @Override
            public void OnTimeClicked() {
                updateTime(position);
            }
        };
    }


    @Override
    public void onCardClicked(int position) {
        Toast.makeText(TaskActivity.this, "You clicked item number" + position, Toast.LENGTH_LONG).show();
    }


    @Override
    public void onInboxRefresh() {
        //taskViewModel.refresh();
        taskAdapter.notifyDataSetChanged();
    }

    @Override
    public void onProjectsRefresh() {

    }

    @Override
    public void onTodayRefresh() {

    }

    @Override
    public void onUpcomingRefresh() {

    }


}