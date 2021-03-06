package com.example.todo.login;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;


import android.text.Editable;
import android.text.TextWatcher;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.todo.R;

import com.example.todo.task.TaskActivity;
import com.example.todo.Util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

import static com.example.todo.ToDoApplication.databaseReference;
import static com.example.todo.ToDoApplication.mAuth;

public class LoginActivity extends Util {
    public LoginActivity() {
    }

    //Method: Handles Login Request

    private void loginAction(final ProgressBar loadingProgressBar, final String username, final String password) {

        loadingProgressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(username, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        loadingProgressBar.setVisibility(View.INVISIBLE);

                        if (task.isSuccessful())
                        {
                            Toast.makeText(LoginActivity.this, "Sign in successful", Toast.LENGTH_LONG).show();

                            databaseReference.child("users").child(mAuth.getUid()).child("list").keepSynced(true);

                            Intent i = new Intent(LoginActivity.this, TaskActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);
                        }
                        else
                        {
                            String errorMessage;

                            try {
                                throw task.getException();
                            }
                            catch (FirebaseAuthInvalidUserException e) {errorMessage = "Invalid Email ID";}
                            catch (FirebaseAuthInvalidCredentialsException e) {errorMessage = "Invalid Password";}
                            catch (FirebaseNetworkException e) {errorMessage = "Network Error";}
                            catch (Exception e) {errorMessage = "Sign in failed (Error Unknown)";}

                            Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }

                    }
                });

    }


    //Variables kept by the acitivity itself

    private LoginViewModel loginViewModel;      // input validation
    boolean isEmailSent = false;                // forgot password transition management
    Button backFromBanner;
    ConstraintLayout forgotPasswordBanner, emailSentBanner;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        themeSelect(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        //Initialize all variables

        loginViewModel = ViewModelProviders.of(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        final EditText usernameEditText = findViewById(R.id.username);
        final EditText passwordEditText = findViewById(R.id.password);
        final EditText recoveryEmailEditText = findViewById(R.id.recoveryEmail);
        final Button loginButton = findViewById(R.id.login);
        final Button newAccountButton = findViewById(R.id.newAccountBTN);

        final ImageButton closeBanner = findViewById(R.id.closeBanner);

        backFromBanner = findViewById(R.id.backFromBanner);
        forgotPasswordBanner = findViewById(R.id.forgotPasswordBanner);
        emailSentBanner = findViewById(R.id.emailSentPanel);

        final Button forgotPasswordButton = findViewById(R.id.forgotPassword);
        final ProgressBar loadingProgressBar = findViewById(R.id.loading);
        final ConstraintLayout recoveryEmailBanner = findViewById(R.id.recoveryEmailBanner);
        final ConstraintLayout forgotPasswordBanner = findViewById(R.id.forgotPasswordBanner);
        final ProgressBar recoveryProgressBar = findViewById(R.id.recoveryProgressBar);


        // Visibility settings for forgot password banner

        forgotPasswordBanner.setVisibility(View.INVISIBLE);
        emailSentBanner.setVisibility(View.INVISIBLE);
        backFromBanner.setText(R.string.pass_reset_btn_text);




        // Displays forgot password banner

        forgotPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

                emailSentBanner.setVisibility(View.INVISIBLE);
                recoveryEmailBanner.setVisibility(View.VISIBLE);
                backFromBanner.setText(R.string.pass_reset_btn_text);

                forgotPasswordBanner.startAnimation(inFromBottomAnimation(250));
                forgotPasswordBanner.setVisibility(View.VISIBLE);
            }
        });



        // Handles forgot-pass banner interactions

        closeBanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

                forgotPasswordBanner.startAnimation(outToBottomAnimation());
                forgotPasswordBanner.setVisibility(View.INVISIBLE);
                emailSentBanner.startAnimation(outToRightAnimation(500));
                isEmailSent = false;
            }
        });


        // [ sends recovery email on first button press ] + [ retracts banner on second press]

        backFromBanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

                if (isEmailSent)
                {
                    forgotPasswordBanner.startAnimation(outToBottomAnimation());
                    forgotPasswordBanner.setVisibility(View.INVISIBLE);
                    emailSentBanner.startAnimation(outToRightAnimation(500));
                    isEmailSent = false;
                }
                else
                {
                    recoveryProgressBar.setVisibility(View.VISIBLE);

                    mAuth.sendPasswordResetEmail(recoveryEmailEditText.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            recoveryProgressBar.setVisibility(View.INVISIBLE);

                            if (task.isSuccessful()) {
                                emailSentBanner.startAnimation(inFromRightAnimation(500));
                                emailSentBanner.setVisibility(View.VISIBLE);
                                backFromBanner.setText(R.string.got_it);
                                isEmailSent = true;
                            }
                            else {
                                Toast.makeText(LoginActivity.this, R.string.error_pass_reset_email, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });


        // Launches SignupActivity

        newAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

                Intent i = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(i);
            }
        });


        // Receives and displays input validation messages - for login page

        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {

            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                loginButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getUsernameError() != null) {
                    usernameEditText.setError(getString(loginFormState.getUsernameError()));
                }
                if (loginFormState.getPasswordError() != null) {
                    passwordEditText.setError(getString(loginFormState.getPasswordError()));
                }
            }
        });


        // Receives and displays input validation messages - for password recovery banner

        loginViewModel.getRecoveryFormState().observe(this, new Observer<RecoveryFormState>() {

            @Override
            public void onChanged(@Nullable RecoveryFormState recoveryFormState) {

                if (recoveryFormState == null)
                    return;

                backFromBanner.setEnabled(recoveryFormState.isDataValid());

                if (recoveryFormState.getRecoveryEmailError() != null) {
                    recoveryEmailEditText.setError(getString(recoveryFormState.getRecoveryEmailError()));
                }
            }
        });



        // Listener for login page input fields

        TextWatcher afterLoginTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };


        // Listener for recovery page input fields

        TextWatcher afterRecoveryTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.recoveryEmailDataChanged(recoveryEmailEditText.getText().toString());
            }
        };

        usernameEditText.addTextChangedListener(afterLoginTextChangedListener);
        passwordEditText.addTextChangedListener(afterLoginTextChangedListener);
        recoveryEmailEditText.addTextChangedListener(afterRecoveryTextChangedListener);



        // Sends login requests to loginAction()

        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {

                    loginAction(loadingProgressBar, usernameEditText.getText().toString(), passwordEditText.getText().toString());
                }
                return false;
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);

                View view = getCurrentFocus();
                if (view == null)
                    view = new View(getApplicationContext());

                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                loginAction(loadingProgressBar, usernameEditText.getText().toString(), passwordEditText.getText().toString());

            }
        });
    }

    @Override
    public void onBackPressed() {
        if (isEmailSent) {
            forgotPasswordBanner.startAnimation(outToBottomAnimation());
            forgotPasswordBanner.setVisibility(View.INVISIBLE);
            emailSentBanner.startAnimation(outToRightAnimation(500));
            isEmailSent = false;
        }
        else {
            super.onBackPressed();
        }
    }
}