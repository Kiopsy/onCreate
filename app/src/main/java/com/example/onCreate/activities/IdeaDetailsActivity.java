package com.example.onCreate.activities;

import static com.example.onCreate.fragments.GlobalFeedFragment.containsUser;
import static com.example.onCreate.fragments.GlobalFeedFragment.downvoteOnClickListener;
import static com.example.onCreate.fragments.GlobalFeedFragment.upvoteOnClickListener;
import static com.example.onCreate.fragments.PrivateFeedFragment.starOnClickListener;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.onCreate.R;
import com.example.onCreate.dialogs.PostShareDialog;
import com.example.onCreate.models.Idea;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.share.model.ShareContent;
import com.facebook.share.model.ShareMediaContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.widget.ShareButton;
import com.facebook.share.widget.ShareDialog;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Arrays;

public class IdeaDetailsActivity extends AppCompatActivity {

    private String TAG = "DetailActivity";
    private TextView mTvTitle;
    private TextView mTvDescription;
    private TextView mTvTime;
    private TextView mTvVotes;
    private LinearLayout mShareLayout;
    private PostShareDialog mShareDialog;
    private ImageView mIvPostImage;
    private ImageView mIvStars;
    private ImageView mIvUpvote;
    private ImageView mIvDownvote;
    private ConstraintLayout mPrivateFeedButtonLayout;
    private ConstraintLayout mGlobalFeedButtonLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_idea_details);

        Idea idea = (Idea) getIntent().getParcelableExtra("idea");

        mTvDescription = findViewById(R.id.tvDescription);
        mTvTime = findViewById(R.id.tvTime);
        mTvTitle = findViewById(R.id.tvTitle);
        mTvVotes = findViewById(R.id.tvVotes);
        mIvStars = findViewById(R.id.ivStars);
        mIvPostImage = findViewById(R.id.ivPostImage);
        mIvUpvote = findViewById(R.id.ivUpvote);
        mIvDownvote = findViewById(R.id.ivDownvotes);
        mPrivateFeedButtonLayout = findViewById(R.id.privateFeedButtonLayout);
        mGlobalFeedButtonLayout = findViewById(R.id.globalFeedButtonLayout);
        mTvDescription.setText(idea.getDescription());
        mTvTitle.setText(idea.getTitle());
        mTvTime.setText(idea.calculateTimeAgo(idea.getCreatedAt()));
        mShareLayout = findViewById(R.id.shareLayout);

        ParseUser currentUser = ParseUser.getCurrentUser();

        // Display different feeds based on whether feed is private vs global
        if (idea.getVisibility()) {
            // Set visibility for private feed views
            mPrivateFeedButtonLayout.setVisibility(View.VISIBLE);
            mGlobalFeedButtonLayout.setVisibility(View.GONE);

            // Set functionality for private feed
            mIvStars.setSelected(idea.getStarred());
        } else {
            // Set visibility for global feed views
            mPrivateFeedButtonLayout.setVisibility(View.GONE);
            mGlobalFeedButtonLayout.setVisibility(View.VISIBLE);

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

        mShareLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mShareDialog = new PostShareDialog();
                mShareDialog.showDialog(IdeaDetailsActivity.this);
            }
        });

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