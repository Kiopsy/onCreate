package com.example.onCreate.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.onCreate.R;
import com.example.onCreate.models.Idea;
import com.parse.DeleteCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class IdeaAdapter extends RecyclerView.Adapter<IdeaAdapter.ViewHolder> {

    private Context mContext;
    private List<Idea> mIdeas;
    private boolean mIsPrivateFeed;
    private final String mTAG = "IdeaAdapter";

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
        View view= LayoutInflater.from(mContext).inflate(R.layout.item_idea, parent, false);
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

        // TODO: is m a suitable prefix for these views?
        TextView mTvTitle;
        TextView mTvDescription;
        TextView mTvTime;
        TextView mTvVotes;
        ImageView mIvStars;
        ImageView mIvTrash;
        ImageView mIvUpvote;
        ImageView mIvDownvote;
        ImageView mIvPostImage;

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
            itemView.setOnClickListener(this);
        }

        // Set the data for each of the views in the UI
        public void bind(Idea idea) {
            mTvDescription.setText(idea.getDescription());
            mTvTitle.setText(idea.getTitle()); mTvTime.setText(idea.calculateTimeAgo(idea.getCreatedAt()));

            ParseUser currentUser = ParseUser.getCurrentUser();

            // Display different feeds based on whether feed is private vs global
            if (mIsPrivateFeed) {
                // Set visibility for private feed views
                mIvStars.setVisibility(View.VISIBLE);
                mIvUpvote.setVisibility(View.GONE);
                mTvVotes.setVisibility(View.GONE);
                mIvDownvote.setVisibility(View.GONE);

                // Set functionality for private feed
                mIvStars.setSelected(idea.getStarred());
            } else {
                // Set visibility for global feed views
                mIvUpvote.setVisibility(View.VISIBLE);
                mTvVotes.setVisibility(View.VISIBLE);
                mIvDownvote.setVisibility(View.VISIBLE);
                mIvStars.setVisibility(View.GONE);

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

            // Starring an idea
            mIvStars.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean star = !idea.getStarred();
                    idea.setStarred(star);
                    mIvStars.setSelected(star);
                    idea.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                Log.e(mTAG, "error while saving", e);
                                Toast.makeText(mContext, "error while saving", Toast.LENGTH_SHORT).show();
                            }
                            Log.i(mTAG, "Post save was successful");
                        }
                    });
                }
            });

            // Upvoting a global post
            mIvUpvote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Check if current user is already upvoted on the post
                    ArrayList<ParseUser> upvoteUsers = idea.getUpvoteUsers();
                    ArrayList<ParseUser> downvoteUsers = idea.getDownvoteUsers();
                    boolean hasUpvoted = upvoteUsers != null ? containsUser(upvoteUsers, currentUser) : false;
                    int upvotes = idea.getUpvotes();
                    int downvotes = idea.getDownvotes();

                    if (hasUpvoted) {
                        // Un-upvote a post: remove current user from upvote list and change the upvote count
                        idea.setUpvoteUsers(removeUser(upvoteUsers, currentUser));
                        upvotes--;
                        idea.setUpvotes(upvotes);
                    } else {
                        // Upvote a post: add current user to upvote list and change the upvote count
                        idea.add("upvoteUsers", currentUser);
                        upvotes++;
                        idea.setUpvotes(upvotes);
                    }

                    // Change upvote text & image based on previous interaction
                    mIvUpvote.setSelected(!hasUpvoted);
                    mTvVotes.setText(Integer.toString(upvotes - downvotes));

                    idea.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                Log.e(mTAG, "error while saving", e);
                                Toast.makeText(mContext, "error while saving", Toast.LENGTH_SHORT).show();
                            }
                            Log.i(mTAG, "Post save was successful");
                        }
                    });
                }
            });

            // Downvoting a global post
            mIvDownvote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Check if current user is already downvoted on the post
                    ArrayList<ParseUser> upvoteUsers = idea.getUpvoteUsers();
                    ArrayList<ParseUser> downvoteUsers = idea.getDownvoteUsers();
                    boolean hasInteracted = downvoteUsers != null ? containsUser(downvoteUsers, currentUser) : false;;
                    int upvotes = idea.getUpvotes();
                    int downvotes = idea.getDownvotes();

                    if (hasInteracted) {
                        // Un-upvote a post: remove current user from upvote list and change the upvote count
                        idea.setDownvoteUsers(removeUser(downvoteUsers, currentUser));
                        downvotes--;
                        idea.setDownvotes(downvotes);
                    } else {
                        // Upvote a post: add current user to upvote list and change the upvote count
                        idea.add("downvoteUsers", currentUser);
                        downvotes++;
                        idea.setDownvotes(downvotes);
                    }

                    // Change upvote text & image based on previous interaction
                    mIvDownvote.setSelected(!hasInteracted);
                    mTvVotes.setText(Integer.toString(upvotes - downvotes));

                    idea.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                Log.e(mTAG, "error while saving", e);
                                Toast.makeText(mContext, "error while saving", Toast.LENGTH_SHORT).show();
                            }
                            Log.i(mTAG, "Post save was successful");
                        }
                    });
                }
            });

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
        }

        // when the user clicks post, show IdeaDetailsActivity for the selected Idea
        @Override
        public void onClick(View v) {
//            // gets item position
//            int position = getAdapterPosition();
//            // make sure the position is valid, i.e. actually exists in the view
//            if (position != RecyclerView.NO_POSITION) {
//                // get the movie at the position, this won't work if the class is static
//                Idea post = ideas.get(position);
//                // create intent for the new activity
//                Intent intent = new Intent(context, PostDetailsActivity.class);
//                intent.putExtra("post", post);
//                // show the activity
//                context.startActivity(intent);
//            }
        }
    }

    // Checks if an array of ParseUsers contains a specific user
    private boolean containsUser (ArrayList<ParseUser> allUsers, ParseUser user) {
        String userId = user.getObjectId();
        for (ParseUser u :allUsers) {
            if (u.getObjectId().equals(userId)) {
                return true;
            }
        }
        return false;
    }

    // Remove a user from a list of ParseUsers by Object id
    private ArrayList<ParseUser> removeUser(ArrayList<ParseUser> allUsers, ParseUser user) {
        String userId = user.getObjectId();
        for (ParseUser u :allUsers) {
            if (u.getObjectId().equals(userId)) {
               allUsers.remove(u);
               break;
            }
        }
        return allUsers;
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
