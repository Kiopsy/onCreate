package com.example.onCreate.activities;

import static com.example.onCreate.fragments.GlobalFeedFragment.containsUser;
import static com.example.onCreate.fragments.GlobalFeedFragment.downvoteOnClickListener;
import static com.example.onCreate.fragments.GlobalFeedFragment.removeUser;
import static com.example.onCreate.fragments.GlobalFeedFragment.upvoteOnClickListener;
import static com.example.onCreate.fragments.PrivateFeedFragment.starOnClickListener;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.onCreate.R;
import com.example.onCreate.dialogs.PostShareDialog;
import com.example.onCreate.models.Idea;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.ArrayList;

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
    private ConstraintLayout mLayout;
    private Idea mIdea;
    private int mPosition;
    private final int REQUEST_DETAILS_ACTIVITY = 1231;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_idea_details);

        mIdea = (Idea) getIntent().getParcelableExtra("idea");
        mPosition = getIntent().getIntExtra("position", 0);

        Intent intent = new Intent();
        intent.putExtra("idea", mIdea);
        intent.putExtra("position", mPosition);
        setResult(REQUEST_DETAILS_ACTIVITY, intent);

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
        mTvDescription.setText(mIdea.getDescription());
        mTvTitle.setText(mIdea.getTitle());
        mTvTime.setText(mIdea.calculateTimeAgo(mIdea.getCreatedAt()));
        mShareLayout = findViewById(R.id.shareLayout);
        mLayout = findViewById(R.id.constraintLayout);

        ParseUser currentUser = ParseUser.getCurrentUser();

        // Display different feeds based on whether feed is private vs global
        if (mIdea.getVisibility()) {
            // Set visibility for private feed views
            mPrivateFeedButtonLayout.setVisibility(View.VISIBLE);
            mGlobalFeedButtonLayout.setVisibility(View.GONE);

            // Set functionality for private feed
            mIvStars.setSelected(mIdea.getStarred());
        } else {
            // Set visibility for global feed views
            mPrivateFeedButtonLayout.setVisibility(View.GONE);
            mGlobalFeedButtonLayout.setVisibility(View.VISIBLE);

            // Set functionality for global feed
            ArrayList<ParseUser> upvoteUsers = mIdea.getUpvoteUsers();
            ArrayList<ParseUser> downvoteUsers = mIdea.getDownvoteUsers();

            boolean isUpvoteSelected = upvoteUsers != null ? containsUser(upvoteUsers, currentUser) : false;
            mIvUpvote.setSelected(isUpvoteSelected);

            boolean isDownvoteSelected = downvoteUsers != null ? containsUser(downvoteUsers, currentUser) : false;
            mIvDownvote.setSelected(isDownvoteSelected);

            mTvVotes.setText(Integer.toString(mIdea.getUpvotes() - mIdea.getDownvotes()));
        }

        View rootView = findViewById(android.R.id.content).getRootView();

        // Set all the click listeners for image buttons: up/downvote, trash, star
        mIvUpvote.setOnClickListener(upvoteOnClickListener(rootView, mIdea));
        mIvDownvote.setOnClickListener(downvoteOnClickListener(rootView, mIdea));
        mIvStars.setOnClickListener(starOnClickListener(rootView, mIdea));

        // Post description
        ParseFile image = mIdea.getImage();
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

        mLayout.setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(IdeaDetailsActivity.this, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    Log.d("TEST", "onDoubleTap");
                    if (mIdea.getVisibility()) {
                        mIdea.setStarred(true);
                        mIvStars.setSelected(true);
                        mIdea.saveInBackground();
                    } else {
                        // Check if current user is already upvoted on the post
                        ParseUser currentUser = ParseUser.getCurrentUser();
                        ArrayList<ParseUser> upvoteUsers = mIdea.getUpvoteUsers();
                        ArrayList<ParseUser> downvoteUsers = mIdea.getDownvoteUsers();
                        boolean hasUpvoted = upvoteUsers != null ? containsUser(upvoteUsers, currentUser) : false;
                        boolean hasDownvoted = downvoteUsers != null ? containsUser(downvoteUsers, currentUser) : false;
                        int upvotes = mIdea.getUpvotes();
                        int downvotes = mIdea.getDownvotes();

                        if (!hasUpvoted) {
                            // Upvote a post: add current user to upvote list and change the upvote count
                            if (hasDownvoted) {
                                mIdea.setDownvoteUsers(removeUser(downvoteUsers, currentUser));
                                mIdea.setDownvotes(mIdea.getDownvotes() - 1);
                                downvotes--;
                                mIvDownvote.setSelected(false);
                            }
                            mIdea.add("upvoteUsers", currentUser);
                            mIdea.setUpvotes(mIdea.getUpvotes() + 1);
                            upvotes++;
                        }

                        // Change upvote text & image based on previous interaction
                        mIvUpvote.setSelected(true);
                        mTvVotes.setText(Integer.toString(upvotes - downvotes));

                        mIdea.saveInBackground();
                    }
                    mLayout.performHapticFeedback(
                            HapticFeedbackConstants.VIRTUAL_KEY,
                            HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING  // Ignore device's setting. Otherwise, you can use FLAG_IGNORE_VIEW_SETTING to ignore view's setting.
                    );
                    return super.onDoubleTap(e);
                }
        });

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d("TEST", "Raw event: " + event.getAction() + ", (" + event.getRawX() + ", " + event.getRawY() + ")");
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });

        setActionBarIcon();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_to_left, R.anim.exit_to_left);
    }

    public void setActionBarIcon() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setIcon(R.drawable.logo);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
    }
}