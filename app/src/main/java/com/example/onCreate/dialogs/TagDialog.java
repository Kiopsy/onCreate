package com.example.onCreate.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Constraints;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;

import com.example.onCreate.R;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class TagDialog extends DialogFragment {

    private EditText mEtTags;
    private LinearLayout mTagLayout;
    private ConstraintLayout mScreenLayout;
    private LinearLayout mTagDisplayLayout;
    private TextView mTvDone;
    private Dialog mDialog;
    private ArrayList<String> mTags = new ArrayList<>();
    private final String TAG = "TagDialog";
    private final int MAX_TAGS = 3;

    public TagDialog(ConstraintLayout screenLayout, LinearLayout tagDisplayLayout) {
        mScreenLayout = screenLayout;
        mTagDisplayLayout = tagDisplayLayout;
    }

    public void showDialog(Activity activity) {
        mDialog = new Dialog(activity);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setCancelable(true);
        mDialog.setContentView(R.layout.dialog_tags);

        // Remove all tags currently in the layout for display
        mTagDisplayLayout.removeAllViews();

        mEtTags = mDialog.findViewById(R.id.etTags);
        mTagLayout = mDialog.findViewById(R.id.tagLayout);
        mTvDone = mDialog.findViewById(R.id.tvDone);

        // Set focus on the edit text upon opening
        mEtTags.requestFocus();

        // Set on click listeners
        mEtTags.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Add tag to the linear layout
                    Log.i(TAG, "Tag enter pressed in dialog");

                    // A post can have a max number of tags
                    if (mTags.size() < MAX_TAGS) {
                        // Retrieve tag
                        String tag = mEtTags.getText().toString();

                        // Add tag to list and LinearLayout
                        mTags.add(tag);
                        mTagLayout.addView(createTagTextView(tag, activity));
                    } else {
                        // Create a snackbar
                        Snackbar snackbar = Snackbar.make(mDialog.findViewById(R.id.layout),
                                                    "Post only use three tags",
                                                    Snackbar.LENGTH_LONG);
                        View view = snackbar.getView();
                        FrameLayout.LayoutParams params =(FrameLayout.LayoutParams)view.getLayoutParams();
                        params.gravity = Gravity.TOP;
                        view.setLayoutParams(params);
                        snackbar.show();
                    }

                    // Clear edit text
                    mEtTags.getText().clear();
                    return true;
                }
                return false;
            }
        });

        mTvDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send tags over for saving
                Log.i(TAG, "done button pressed");

                // populate the LinLayout of the brainstorming screen with each tag
                for (int i = 0; i < mTags.size(); i++) {
                    mTagDisplayLayout.addView(createTagTextView(mTags.get(i), activity));
                }

                // Hide the dialog
                hideDialog();
            }
        });

        // Show the dialog & add animations
        mDialog.show();
        mDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        mDialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    public void hideDialog() {
        mDialog.cancel();
    }

    public ArrayList<String> getTags() {
        return mTags;
    }

    // Creates a TextView of a tag for viewing purposes
    private TextView createTagTextView(String tag, Activity activity) {
        TextView tv = new TextView(activity);

        // Set text, and attributes
        tv.setText("#" + tag);
        Drawable background = activity.getResources().getDrawable(R.drawable.layout_tag_background);
        tv.setBackground(background);
        tv.setPadding(35, 25, 35, 25);
        tv.setTextSize(16);
        tv.setTextColor(Color.WHITE);
        Typeface typeface = ResourcesCompat.getFont(activity,R.font.lato_bold);
        tv.setTypeface(typeface);
        Constraints.LayoutParams params = new Constraints.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(20, 0, 0, 0);
        tv.setLayoutParams(params);

        // Set on click listener
        tv.setClickable(true);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // If clicked, remove the tag from the layout & tag list
                mTags.remove(tag);
                tv.setVisibility(View.GONE);
            }
        });

        return tv;
    }
}
