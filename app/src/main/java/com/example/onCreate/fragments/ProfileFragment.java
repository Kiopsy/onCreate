package com.example.onCreate.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.onCreate.R;
import com.example.onCreate.models.Idea;
import com.parse.FindCallback;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

public class ProfileFragment extends Fragment {

    private TextView mTvName;
    private TextView mTvJobDescription;
    private TextView mTvGeneralDescription;
    private TextView mTvKarma;
    private TextView mTvIdeas;
    private ImageView mIvProfilePicture;
    private List<Idea> mAllPosts;
    private final static String TAG = "Profile Fragment";

    public ProfileFragment() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTvName = view.findViewById(R.id.tvName);
        mTvJobDescription = view.findViewById(R.id.tvJobDescription);
        mTvGeneralDescription = view.findViewById(R.id.tvGeneralDescription);
        mTvKarma = view.findViewById(R.id.tvKarma);
        mTvIdeas = view.findViewById(R.id.tvIdeas);
        mIvProfilePicture = view.findViewById(R.id.ivProfilePicture);

        // Setting profile text descriptions from ParseUser
        ParseUser currentUser = ParseUser.getCurrentUser();
        mTvName.setText("Victor Goncalves");
        mTvJobDescription.setText(currentUser.getString("jobDescription"));
        mTvGeneralDescription.setText(currentUser.getString("generalDescription"));

        // Query for all users posts in order to find karma and idea count
        queryPosts();

        // Calculate and set karma and idea count
        mTvKarma.setText(Integer.toString(getKarmaCount(currentUser)));
        mTvIdeas.setText(Integer.toString(getIdeaCount(currentUser)));

        // Setting Profile image
        ParseFile profImage = currentUser.getParseFile("profileImage");
        if (profImage != null) {
            Glide.with(getContext()).load(profImage.getUrl()).into(mIvProfilePicture);
        }
    }

    // Calculates users's total global karma
    private int getKarmaCount(ParseUser user) {
        int karma = 0;
        if (mAllPosts != null) {
            // Iterate through all user's
            for (Idea idea : mAllPosts) {
                // Only global posts can contribute to karma
                if (!idea.getVisibility()) {
                    karma += idea.getUpvotes() - idea.getDownvotes();
                }
            }
        }
        return karma;
    }

    // Calculates user's total idea post count
    private int getIdeaCount(ParseUser user) {
        if (mAllPosts != null) {
            return mAllPosts.size();
        } else {
            return 0;
        }
    }

    // Use Parse to query for all of the user's posts
    private List<Idea> queryPosts() {
        // specify what type of data we want to query - Post.class
        ParseQuery<Idea> query = ParseQuery.getQuery(Idea.class);
        // include data referred by user key
        query.include(Idea.KEY_USER);
        // find all user's posts
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        // order posts by creation date (newest first)
        query.findInBackground(new FindCallback<Idea>() {
            @Override
            public void done(List<Idea> feed, com.parse.ParseException e) {
                // check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                    return;
                }

                // for debugging purposes let's print every post description to logcat
                for (Idea idea : feed) {
                    Log.i(TAG, "Post: " + idea.getDescription() + ", username: " + idea.getUser().getUsername());
                }
                mAllPosts = feed;
            }
        });
    }
}