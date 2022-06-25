package com.example.onCreate.adapters;

import android.content.Context;
import android.content.Intent;
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
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.SaveCallback;

import java.util.List;

public class IdeaAdapter extends RecyclerView.Adapter<IdeaAdapter.ViewHolder> {

    private Context mContext;
    private List<Idea> mIdeas;
    private boolean mIsPrivateFeed;
    private final String mTAG = "IdeaAdapter";

    // Pass in context and list of tweets
    public IdeaAdapter(Context context, List<Idea> ideas, boolean isPrivateFeed) {
        this.mContext = context;
        this.mIdeas = ideas;

        // Used to determine whether adapter is for private vs global feed
        this.mIsPrivateFeed = isPrivateFeed;
    }

    // Inflate the layout for each row
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(mContext).inflate(R.layout.item_private_idea, parent, false);
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
                mTvVotes.setVisibility(View.GONE);

                // Set functionality for global feed
                // mIvUpvote.setSelected(idea.get);
                mTvVotes.setText(Integer.toString(idea.getUpvotes() - idea.getDownvotes()));
            }

            // Image click listeners
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
