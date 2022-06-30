package com.example.onCreate.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.onCreate.R;
import com.example.onCreate.models.Idea;
import com.parse.FindCallback;
import com.parse.ParseException;
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
        mTvName.setText(currentUser.getString("name"));
        mTvJobDescription.setText(currentUser.getString("jobDescription"));
        mTvGeneralDescription.setText(currentUser.getString("generalDescription"));

        // Setting the count labels for karma and ideas
        try {
            setCountLabels();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        // Setting Profile image
        ParseFile profImage = currentUser.getParseFile("profileImage");
        if (profImage != null) {
            Glide.with(getContext()).load(profImage.getUrl()).into(mIvProfilePicture);
        }
    }

    // Calculates user's total global karma and number of ideas
    private void setCountLabels() throws ParseException {
        // Query for all users posts in order to find karma and idea count
        ParseQuery<Idea> query = ParseQuery.getQuery(Idea.class);
        query.include(Idea.KEY_USER);
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        List<Idea> feed = query.find();
        mTvKarma.setText(Integer.toString(getKarmaCount(feed)));
        mTvIdeas.setText(Integer.toString(getIdeaCount(feed)));
    }

    // Calculates user's total karma post count
    private int getKarmaCount(List<Idea> allPosts) {
        int karma = 0;
        if (allPosts != null) {
            // Iterate through all user's
            for (Idea idea : allPosts) {
                // Only global posts can contribute to karma
                if (!idea.getVisibility()) {
                    karma += idea.getUpvotes() - idea.getDownvotes();
                }
            }
        }
        return karma;
    }

    // Calculates user's total idea post count
    private int getIdeaCount(List<Idea> allPosts) {
        if (allPosts != null) {
            return allPosts.size();
        } else {
            return 0;
        }
    }
}