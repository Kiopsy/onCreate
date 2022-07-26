package com.example.onCreate.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Constraints;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.onCreate.R;
import com.example.onCreate.activities.IdeaDetailsActivity;
import com.example.onCreate.dialogs.PostShareDialog;
import com.example.onCreate.models.Idea;
import com.parse.DeleteCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import static com.example.onCreate.fragments.GlobalFeedFragment.containsUser;
import static com.example.onCreate.fragments.GlobalFeedFragment.downvoteOnClickListener;
import static com.example.onCreate.fragments.GlobalFeedFragment.upvoteOnClickListener;
import static com.example.onCreate.fragments.PrivateFeedFragment.starOnClickListener;

public class IdeaAdapter extends RecyclerView.Adapter<IdeaAdapter.ViewHolder> {

    private Context mContext;
    private List<Idea> mIdeas;
    private boolean mIsPrivateFeed;
    private final String TAG = "IdeaAdapter";
    private final int REQUEST_DETAIL_ACTIVITY = 1231;

    // Pass in context, list of ideas, and a bool to distinguish whether adapter is used for global vs private feeds
    public IdeaAdapter(Context context, List<Idea> ideas, boolean isPrivateFeed) {
        this.mContext = context;
        this.mIdeas = ideas;
        this.mIsPrivateFeed = isPrivateFeed;
    }

    // Inflate the layout for each row
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_idea, parent, false);
        return new ViewHolder(view);
    }

    // Bind values based on the position of the element
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the data at position
        Idea idea = mIdeas.get(position);
        // Bind the data to the view holder
        holder.bind(idea);
    }

    @Override
    public int getItemCount() {
        return mIdeas.size();
    }

    // Define a ViewHolder to connect UI with Backend
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mTvTitle;
        private TextView mTvDescription;
        private TextView mTvTime;
        private TextView mTvVotes;
        private ImageView mIvStars;
        private ImageView mIvTrash;
        private ImageView mIvUpvote;
        private ImageView mIvDownvote;
        private ImageView mIvPostImage;
        private ConstraintLayout mPrivateFeedButtonLayout;
        private ConstraintLayout mGlobalFeedButtonLayout;
        private LinearLayout mShareLayout;
        private PostShareDialog mShareDialog;
        private LinearLayout mTagLayout;

        // Put all Views in a ViewHolder
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mTvDescription = itemView.findViewById(R.id.tvDescription);
            mTvTime= itemView.findViewById(R.id.tvTime);
            mTvTitle = itemView.findViewById(R.id.tvTitle);
            mTvVotes = itemView.findViewById(R.id.tvVotes);
            mIvStars= itemView.findViewById(R.id.ivStars);
            mIvTrash = itemView.findViewById(R.id.ivTrash);
            mIvPostImage = itemView.findViewById(R.id.ivPostImage);
            mIvUpvote = itemView.findViewById(R.id.ivUpvote);
            mIvDownvote = itemView.findViewById(R.id.ivDownvotes);
            mPrivateFeedButtonLayout = itemView.findViewById(R.id.privateFeedButtonLayout);
            mGlobalFeedButtonLayout = itemView.findViewById(R.id.globalFeedButtonLayout);
            // mShareLayout = itemView.findViewById(R.id.shareLayout);
            mTagLayout = itemView.findViewById(R.id.tagLayout);
            itemView.setOnClickListener(this);
        }

        // Set the data for each of the views in the UI
        public void bind(Idea idea) {
            mTvDescription.setText(idea.getDescription());
            mTvTitle.setText(idea.getTitle());
            mTvTime.setText(idea.calculateTimeAgo(idea.getCreatedAt()));

            setIdeaVisuals(idea);

            // Set all the click listeners for image buttons: up/downvote, trash, star
            mIvUpvote.setOnClickListener(upvoteOnClickListener(itemView, idea));
            mIvDownvote.setOnClickListener(downvoteOnClickListener(itemView, idea));
            mIvStars.setOnClickListener(starOnClickListener(itemView, idea));

            mIvTrash.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog diaBox = AskOption(idea);
                    diaBox.show();
                }
            });

            // Post description
            ParseFile image = idea.getImage();
            if (image != null) {
                mIvPostImage.setVisibility(View.VISIBLE);
                Glide.with(mContext).load(image.getUrl()).into(mIvPostImage);
            } else {
                mIvPostImage.setVisibility(View.GONE);
            }

//            for (int i = 0; i<3; i++) {
//                TextView tv = new TextView(mContext);
//                tv.setText("testing");
//                Drawable background = mContext.getResources().getDrawable(R.drawable.layout_tag_background);
//                tv.setBackground(background);
//                tv.setPadding(20, 10, 20, 10);
//                tv.setTextSize(16);
//                tv.setTextColor(Color.WHITE);
//                Typeface typeface = ResourcesCompat.getFont(mContext,R.font.lato_bold);
//                tv.setTypeface(typeface);
//
//                Constraints.LayoutParams params = new Constraints.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//                params.setMargins(20, 0, 0, 0);
//                tv.setLayoutParams(params);
//                mTagLayout.addView(tv);
//            }
//            mShareLayout.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    mShareDialog = new PostShareDialog();
//                    mShareDialog.showDialog(mContext);
//                }
//            });
        }

        // when the user clicks post, show IdeaDetailsActivity for the selected Idea
        @Override
        public void onClick(View v) {
            // gets item position
            int position = getAdapterPosition();
            // make sure the position is valid, i.e. actually exists in the view
            if (position != RecyclerView.NO_POSITION) {
                Idea idea = mIdeas.get(position);
                // get the movie at the position, this won't work if the class is static
                // create intent for the new activity

                Intent intent = new Intent(mContext, IdeaDetailsActivity.class);
                intent.putExtra("idea", idea);
                intent.putExtra("position", position);

                // show the activity
                Activity activity = (Activity) mContext;

                // start the activity and set transitions
                activity.startActivityForResult(intent, REQUEST_DETAIL_ACTIVITY);
                activity.overridePendingTransition(R.anim.slide_to_right, R.anim.exit_to_right);
            }
        }


        // Sets the idea visuals (starred, up/downvotes) based on the idea's state
        public void setIdeaVisuals(Idea idea) {
            ParseUser currentUser = ParseUser.getCurrentUser();

            // Display different feeds based on whether feed is private vs global
            if (mIsPrivateFeed) {
                // Set visibility for private feed views
                mPrivateFeedButtonLayout.setVisibility(View.VISIBLE);
                mGlobalFeedButtonLayout.setVisibility(View.GONE);

                // Set functionality for private feed
                mIvStars.setSelected(idea.getStarred());
            } else {
                // Set visibility for global feed views
                mPrivateFeedButtonLayout.setVisibility(View.GONE);
                mGlobalFeedButtonLayout.setVisibility(View.VISIBLE);

                // Only allow users to delete their own posts
                if (idea.getUser().getObjectId().equals(currentUser.getObjectId())) {
                    mIvTrash.setVisibility(View.VISIBLE);
                } else {
                    mIvTrash.setVisibility(View.GONE);
                }

                // Set functionality for global feed
                ArrayList<ParseUser> upvoteUsers = idea.getUpvoteUsers();
                ArrayList<ParseUser> downvoteUsers = idea.getDownvoteUsers();

                boolean isUpvoteSelected = upvoteUsers != null ? containsUser(upvoteUsers, currentUser) : false;
                mIvUpvote.setSelected(isUpvoteSelected);

                boolean isDownvoteSelected = downvoteUsers != null ? containsUser(downvoteUsers, currentUser) : false;
                mIvDownvote.setSelected(isDownvoteSelected);

                mTvVotes.setText(Integer.toString(idea.getUpvotes() - idea.getDownvotes()));
            }
        }
    }

    private AlertDialog AskOption(Idea idea) {
        AlertDialog myQuittingDialogBox = new AlertDialog.Builder(mContext)
                // set message, title, and icon
                .setTitle("Delete")
                .setMessage("Are you sure you want to delete?")
                .setIcon(R.drawable.ic_trashcan)

                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {

                        mIdeas.remove(idea);
                        idea.deleteInBackground(new DeleteCallback() {
                            @Override
                            public void done(ParseException e) {
                                // inside done method checking if the error is null or not.
                                if (e == null) {
                                    // if the error is not null then we are displaying a toast message and opening our home activity.
                                    Toast.makeText(mContext, "POST Deleted", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                    notifyDataSetChanged();
                                } else {
                                    // if we get error we are displaying it in below line.
                                    Toast.makeText(mContext, "Fail to delete course..", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();

        return myQuittingDialogBox;
    }

    // Clean all elements of the recycler
    public void clear() {
        mIdeas.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Idea> list) {
        mIdeas.addAll(list);
        notifyDataSetChanged();
    }
}
