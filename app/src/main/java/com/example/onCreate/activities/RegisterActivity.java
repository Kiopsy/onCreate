package com.example.onCreate.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.onCreate.R;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class RegisterActivity extends AppCompatActivity {

    private static final String mTAG = "RegisterActivity";
    private EditText mEtUsername;
    private EditText mEtPassword;
    private EditText mEtEmail;
    private Button mBtnSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mEtUsername = findViewById(R.id.etUsername);
        mEtPassword = findViewById(R.id.etPassword);
        mEtEmail = findViewById(R.id.etEmail);
        mBtnSignup = findViewById(R.id.btnSignup);

        // Set signup button onClick Listener
        mBtnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(mTAG, "onClick signup button");
                String username = mEtUsername.getText().toString();
                String password = mEtPassword.getText().toString();
                String email = mEtEmail.getText().toString();
                signupUser(username, password, email);
            }
        });

        // Set up logo in action bar
        setActionBarIcon();
    }

    // Create a new Parse
    private void signupUser(String username, String password, String email) {
        Log.i(mTAG, "Attempting to create user: " + username);

        // Create the ParseUser & set core properties
        ParseUser user = new ParseUser();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);

        // Invoke signUpInBackground
        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                // Check if signup was successful
                if (e != null) {
                    Log.e(mTAG, "Issue with creating an account", e);
                    Toast.makeText(RegisterActivity.this, "Issue with signup!", Toast.LENGTH_SHORT).show();
                    return;
                }
                goMainActivity();
                Toast.makeText(RegisterActivity.this, "success", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Intent to go to the homepage
    private void goMainActivity() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }

    // Setting up action bar & onCreate logo
    private void setActionBarIcon() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setIcon(R.drawable.logo);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
    }
}