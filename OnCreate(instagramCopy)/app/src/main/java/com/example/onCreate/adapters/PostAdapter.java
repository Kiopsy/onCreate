package com.example.onCreate.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.onCreate.R;
import com.example.onCreate.activities.PostDetailsActivity;
import com.example.onCreate.models.Post;
import com.parse.ParseFile;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    Context context;
    List<Post> posts;

    // Pass in context and list of tweets
    public PostAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;
    }

    // Inflate the layout for each row
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view);
    }

    // Bind values based on the position of the element
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the data at position
        Post post = posts.get(position);

        // Bind the data to the view holder
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    // Define a ViewHolder to connect UI with Backend
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView ivProfilePicture;
        ImageView ivPostImage;
        TextView tvDescription;
        TextView tvTime;
        TextView tvUsernameHeading;
        TextView tvUsernameDescription;


        // Put all Views in a ViewHolder
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ivProfilePicture = itemView.findViewById(R.id.ivProfilePicture);
            ivPostImage = itemView.findViewById(R.id.ivPostImage);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvUsernameHeading = itemView.findViewById(R.id.tvUsernameHeading);
            tvUsernameDescription = itemView.findViewById(R.id.tvUsernameDescription);

            itemView.setOnClickListener(this);
        }

        // Set the data for each of the views in the UI
        public void bind(Post post) {
            tvDescription.setText(post.getDescription());
            tvUsernameDescription.setText(post.getUser().getUsername());
            tvUsernameHeading.setText(post.getUser().getUsername());
            tvTime.setText(Post.calculateTimeAgo(post.getCreatedAt()));

            // Post description
            ParseFile image = post.getImage();
            if (image != null) {
                Glide.with(context).load(image.getUrl()).into(ivPostImage);
            }

            // Profile image
            ParseFile profImage = post.getUser().getParseFile("profileImage");
            if (profImage != null) {
                Glide.with(context).load(profImage.getUrl()).into(ivProfilePicture);
            }
        }

        // when the user clicks on a row, show MovieDetailsActivity for the selected movie
        @Override
        public void onClick(View v) {
            // gets item position
            int position = getAdapterPosition();
            // make sure the position is valid, i.e. actually exists in the view
            if (position != RecyclerView.NO_POSITION) {
                // get the movie at the position, this won't work if the class is static
                Post post = posts.get(position);
                // create intent for the new activity
                Intent intent = new Intent(context, PostDetailsActivity.class);
                intent.putExtra("post", post);
                // show the activity
                context.startActivity(intent);
            }
        }
    }

    // Clean all elements of the recycler
    public void clear() {
        posts.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Post> list) {
        posts.addAll(list);
        notifyDataSetChanged();
    }
}
