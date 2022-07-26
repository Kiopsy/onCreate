package com.example.onCreate.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.onCreate.R;
import com.example.onCreate.fragments.BrainstormFragment;
import com.example.onCreate.fragments.GlobalFeedFragment;
import com.example.onCreate.fragments.PrivateFeedFragment;
import com.example.onCreate.fragments.ProfileFragment;
import com.example.onCreate.models.Idea;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.ParseUser;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView mBottomNavView;
    private final FragmentManager mFragmentManager = getSupportFragmentManager();
    private Fragment mFragment = null;
    private int currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bottom navigation bar setup & view listener
        mBottomNavView = findViewById(R.id.bottom_navigation);
        mBottomNavView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                currentFragment = item.getItemId();
                Fragment fragment = null;
                // Accounting for all fragments/screens
                switch (item.getItemId()) {
                    case R.id.menuBrainstorm:
                        mFragment = new BrainstormFragment();
                        break;
                    case R.id.menuGlobal:
                        mFragment = new GlobalFeedFragment();
                        break;
                    case R.id.menuIdeas:
                        mFragment = new PrivateFeedFragment();
                        break;
                    case R.id.menuProfile:
                        mFragment = new ProfileFragment();
                        break;
                }
                mFragmentManager.beginTransaction()
                                .replace(R.id.flContainer, mFragment)
                                .commit();
                return true;
            }
        });
        // Set menuIdeas as home screen fragment
        mBottomNavView.setSelectedItemId(R.id.menuIdeas);

        // Set up logo in action bar
        setActionBarIcon();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if present
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Logout functionality
        if (item.getItemId() == R.id.logoutButton) {
            ParseUser.logOutInBackground();

            // navigate back to Login screen
            Intent i = new Intent(this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // this makes sure the Back button won't work
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // same as above
            startActivity(i);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    // Action bar for the screen that shows onCreate logo
    private void setActionBarIcon() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setIcon(R.drawable.logo);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && requestCode == 1231) {
            Idea idea = (Idea) data.getParcelableExtra("idea");
            int position = data.getIntExtra("position", 0);

            if (currentFragment == R.id.menuIdeas) {
                PrivateFeedFragment privateFeed = (PrivateFeedFragment) mFragment;
                privateFeed.updateVisuals(idea, position);
            } else if (currentFragment == R.id.menuGlobal) {
                GlobalFeedFragment globalFeed = (GlobalFeedFragment) mFragment;
                globalFeed.updateVisuals(idea, position);
            }
        }
    }
}