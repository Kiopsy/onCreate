package com.example.onCreate.activities;

import static com.example.onCreate.fragments.GlobalFeedFragment.containsUser;
import static com.example.onCreate.fragments.GlobalFeedFragment.downvoteOnClickListener;
import static com.example.onCreate.fragments.GlobalFeedFragment.upvoteOnClickListener;
import static com.example.onCreate.fragments.PrivateFeedFragment.starOnClickListener;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.onCreate.R;
import com.example.onCreate.models.Idea;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.ArrayList;

public class IdeaDetailsActivity extends AppCompatActivity {

    private TextView mTvTitle;
    private TextView mTvDescription;
    private TextView mTvTime;
    private TextView mTvVotes;
    private ImageView mIvTrash;
    private ImageView mIvPostImage;
    private ImageView mIvStars;
    private ImageView mIvUpvote;
    private ImageView mIvDownvote;
    private ConstraintLayout privateFeedButtonLayout;
    private ConstraintLayout globalFeedButtonLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_idea_details);

        Idea idea = (Idea) getIntent().getParcelableExtra("idea");

        mTvDescription = findViewById(R.id.tvDescription);
        mTvTime= findViewById(R.id.tvTime);
        mTvTitle = findViewById(R.id.tvTitle);
        mTvVotes = findViewById(R.id.tvVotes);
        mIvStars= findViewById(R.id.ivStars);
        mIvPostImage = findViewById(R.id.ivPostImage);
        mIvUpvote = findViewById(R.id.ivUpvote);
        mIvDownvote = findViewById(R.id.ivDownvotes);
        privateFeedButtonLayout = findViewById(R.id.privateFeedButtonLayout);
        globalFeedButtonLayout = findViewById(R.id.globalFeedButtonLayout);
        mTvDescription.setText(idea.getDescription());
        mTvTitle.setText(idea.getTitle()); mTvTime.setText(idea.calculateTimeAgo(idea.getCreatedAt()));

        ParseUser currentUser = ParseUser.getCurrentUser();

        // Display different feeds based on whether feed is private vs global
        if (idea.getVisibility()) {
            // Set visibility for private feed views
            privateFeedButtonLayout.setVisibility(View.VISIBLE);
            globalFeedButtonLayout.setVisibility(View.GONE);

            // Set functionality for private feed
            mIvStars.setSelected(idea.getStarred());
        } else {
            // Set visibility for global feed views
            privateFeedButtonLayout.setVisibility(View.GONE);
            globalFeedButtonLayout.setVisibility(View.VISIBLE);

            // Set functionality for global feed
            ArrayList<ParseUser> upvoteUsers = idea.getUpvoteUsers();
            ArrayList<ParseUser> downvoteUsers = idea.getDownvoteUsers();

            boolean isUpvoteSelected = upvoteUsers != null ? containsUser(upvoteUsers, currentUser) : false;
            mIvUpvote.setSelected(isUpvoteSelected);

            boolean isDownvoteSelected = downvoteUsers != null ? containsUser(downvoteUsers, currentUser) : false;
            mIvDownvote.setSelected(isDownvoteSelected);

            mTvVotes.setText(Integer.toString(idea.getUpvotes() - idea.getDownvotes()));
        }

        View rootView = findViewById(android.R.id.content).getRootView();

        // Set all the click listeners for image buttons: up/downvote, trash, star
        mIvUpvote.setOnClickListener(upvoteOnClickListener(rootView, idea));
        mIvDownvote.setOnClickListener(downvoteOnClickListener(rootView, idea));
        mIvStars.setOnClickListener(starOnClickListener(rootView, idea));

        // Post description
        ParseFile image = idea.getImage();
        if (image != null) {
            mIvPostImage.setVisibility(View.VISIBLE);
            Glide.with(IdeaDetailsActivity.this).load(image.getUrl()).into(mIvPostImage);
        } else {
            mIvPostImage.setVisibility(View.GONE);
        }

        setActionBarIcon();
    }

    public void setActionBarIcon() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setIcon(R.drawable.logo);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
    }
}