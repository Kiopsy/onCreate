package com.example.onCreate.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

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
import com.parse.ParseFile;
import com.parse.ParseUser;

public class Profile extends Fragment {

    private TextView mTvName;
    private TextView mTvJobDescription;
    private TextView mTvGeneralDescription;
    private TextView mTvKarma;
    private TextView mTvIdeas;
    private ImageView mIvProfilePicture;
    private final static String mTAG = "Profile Fragment";

    public Profile() {
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
        return 1;
    }

    // Calculates user's total idea post count
    private int getIdeaCount(ParseUser user) {
        return 1;
    }
}