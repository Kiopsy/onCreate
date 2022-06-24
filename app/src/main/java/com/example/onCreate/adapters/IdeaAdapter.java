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
import com.example.onCreate.models.Idea;
import com.parse.ParseFile;

import java.util.List;

public class IdeaAdapter extends RecyclerView.Adapter<IdeaAdapter.ViewHolder> {

    Context context;
    List<Idea> ideas;

    // Pass in context and list of tweets
    public IdeaAdapter(Context context, List<Idea> ideas) {
        this.context = context;
        this.ideas = ideas;
    }

    // Inflate the layout for each row
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.item_private_idea, parent, false);
        return new ViewHolder(view);
    }

    // Bind values based on the position of the element
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the data at position
        Idea idea = ideas.get(position);

        // Bind the data to the view holder
        holder.bind(idea);
    }

    @Override
    public int getItemCount() {
        return ideas.size();
    }

    // Define a ViewHolder to connect UI with Backend
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tvTitle;
        TextView tvDescription;
        TextView tvTime;
        ImageView ivStars;
        ImageView ivTrash;
        ImageView ivPostImage;

        // Put all Views in a ViewHolder
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            ivStars = itemView.findViewById(R.id.ivStars);
            ivTrash = itemView.findViewById(R.id.ivTrash);
            ivPostImage = itemView.findViewById(R.id.ivPostImage);
            itemView.setOnClickListener(this);
        }

        // Set the data for each of the views in the UI
        public void bind(Idea idea) {
            tvDescription.setText(idea.getDescription());
            tvTitle.setText(idea.getTitle());
            tvTime.setText(idea.calculateTimeAgo(idea.getCreatedAt()));

//            ivStars = itemView.findViewById(R.id.ivStars);
            //ivTrash = itemView.findViewById(R.id.ivTrash);

            // Post description
            ParseFile image = idea.getImage();
            if (image != null) {
                Glide.with(context).load(image.getUrl()).into(ivPostImage);
            }
        }

        // when the user clicks on a row, show MovieDetailsActivity for the selected movie
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
        ideas.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Idea> list) {
        ideas.addAll(list);
        notifyDataSetChanged();
    }
}
