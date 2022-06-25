package com.example.onCreate.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toolbar;

import com.example.onCreate.R;
import com.example.onCreate.activities.LoginActivity;
//import com.example.instagram.adapters.PostAdapter;
//import com.example.instagram.fragments.Feed;
//import com.example.instagram.fragments.createPost;
//import com.example.instagram.models.Post;
import com.example.onCreate.fragments.Brainstorm;
import com.example.onCreate.fragments.GlobalFeed;
import com.example.onCreate.fragments.PrivateFeed;
import com.example.onCreate.fragments.Profile;
import com.example.onCreate.utilities.EndlessRecyclerViewScrollListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.FindCallback;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView mBottomNavView;
    private final FragmentManager mFragmentManager = getSupportFragmentManager();
    private final int REQUEST_CODE = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bottom navigation bar setup & view listener
        mBottomNavView = findViewById(R.id.bottom_navigation);
        mBottomNavView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = null;
                // Accounting for all fragments
                switch (item.getItemId()) {
                    case R.id.menuBrainstorm:
                        fragment = new Brainstorm();
                        break;
                    case R.id.menuGlobal:
                        fragment = new GlobalFeed();
                        break;
                    case R.id.menuIdeas:
                    fragment = new PrivateFeed();
                        break;
                    case R.id.menuProfile:
                        fragment = new Profile();
                        break;
                }
                mFragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
                return true;
            }
        });
        // Set menuIdeas as home screen fragment
        mBottomNavView.setSelectedItemId(R.id.menuIdeas);

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

    // Action bar for the screen that shows onCreate logo
    private void setActionBarIcon() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setIcon(R.drawable.logo);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
    }

}