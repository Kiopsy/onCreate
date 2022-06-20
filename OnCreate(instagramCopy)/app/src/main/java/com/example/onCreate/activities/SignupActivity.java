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

public class SignupActivity extends AppCompatActivity {

    public static final String TAG = "SignupActivity";
    private EditText etUsername;
    private EditText etPassword;
    private EditText etEmail;
    private Button btnSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        setActionBarIcon();

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etEmail = findViewById(R.id.etEmail);
        btnSignup = findViewById(R.id.btnSignup);

        // Set signup button onClick Listener
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick signup button");
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                String email = etEmail.getText().toString();
                signupUser(username, password, email);
            }
        });
    }

    // Create a new Parse
    private void signupUser(String username, String password, String email) {
        Log.i(TAG, "Attempting to create user: " + username);

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
                    Log.e(TAG, "Issue with creating an account", e);
                    Toast.makeText(SignupActivity.this, "Issue with signup!", Toast.LENGTH_SHORT).show();
                    return;
                }
                goProfileSelect(user);
                Toast.makeText(SignupActivity.this, "success", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goProfileSelect(ParseUser user) {
        Intent i = new Intent(this, ProfileSelectorActivity.class);
        i.putExtra("user", user);
        startActivity(i);
    }

    public void setActionBarIcon() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setIcon(R.drawable.nav_logo_whiteout);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
    }
}