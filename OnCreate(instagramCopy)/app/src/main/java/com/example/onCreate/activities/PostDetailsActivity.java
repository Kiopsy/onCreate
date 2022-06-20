package com.example.onCreate.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.onCreate.R;
import com.example.onCreate.models.Post;
import com.parse.ParseFile;

public class PostDetailsActivity extends AppCompatActivity {

    private ImageView ivProfilePicture;
    private ImageView ivPostImage;
    private TextView tvDescription;
    private TextView tvTime;
    private TextView tvUsernameHeading;
    private TextView tvUsernameDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);

        Post post = (Post) getIntent().getParcelableExtra("post");

        ivProfilePicture = findViewById(R.id.ivProfilePicture);
        ivPostImage = findViewById(R.id.ivPostImage);
        tvDescription = findViewById(R.id.tvDescription);
        tvTime = findViewById(R.id.tvTime);
        tvUsernameHeading = findViewById(R.id.tvUsernameHeading);
        tvUsernameDescription = findViewById(R.id.tvUsernameDescription);

        tvDescription.setText(post.getDescription());
        tvUsernameDescription.setText(post.getUser().getUsername());
        tvUsernameHeading.setText(post.getUser().getUsername());
        tvTime.setText(Post.calculateTimeAgo(post.getCreatedAt()));

        // Post description
        ParseFile image = post.getImage();
        if (image != null) {
            Glide.with(this).load(image.getUrl()).into(ivPostImage);
        }

        // Profile image
        ParseFile profImage = post.getUser().getParseFile("profileImage");
        if (profImage != null) {
            Glide.with(this).load(profImage.getUrl()).into(ivProfilePicture);
        }

        setActionBarIcon();
    }

    public void setActionBarIcon() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setIcon(R.drawable.nav_logo_whiteout);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
    }
}