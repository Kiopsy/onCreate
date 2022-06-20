package com.example.onCreate.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.example.onCreate.R;
import com.example.onCreate.fragments.Feed;
import com.example.onCreate.fragments.createPost;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.FindCallback;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavView;
    private final int REQUEST_CODE = 20;
    final FragmentManager fragmentManager = getSupportFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setActionBarIcon();

        // Bottom Navigation View listener
        bottomNavView = findViewById(R.id.bottom_navigation);
        bottomNavView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = null;
                switch (item.getItemId()) {
                    case R.id.menuCompose:
                        fragment = new createPost();
                        break;
                    case R.id.menuHome:
                        fragment = new Feed();
                        break;
                }
                fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
                return true;
            }
        });
        bottomNavView.setSelectedItemId(R.id.menuHome);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if present
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logoutButton) {
            ParseUser.logOutInBackground();
            ParseUser currentUser = ParseUser.getCurrentUser(); // this will now be null

            // navigate backwards to Login screen
            Intent i = new Intent(this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // this makes sure the Back button won't work
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // same as above
            startActivity(i);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void setActionBarIcon() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setIcon(R.drawable.nav_logo_whiteout);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
    }

}