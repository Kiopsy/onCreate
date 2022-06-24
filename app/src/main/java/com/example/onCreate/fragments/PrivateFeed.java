package com.example.onCreate.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.onCreate.R;
import com.example.onCreate.adapters.IdeaAdapter;
import com.example.onCreate.models.Idea;
import com.example.onCreate.utilities.EndlessRecyclerViewScrollListener;
import com.parse.FindCallback;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PrivateFeed extends Fragment {

    public static final String TAG = "PrivateFeedFragment";
    private EndlessRecyclerViewScrollListener scrollListener;
    private SwipeRefreshLayout swipeContainer;
    protected RecyclerView rvPosts;
    protected IdeaAdapter adapter;
    protected List<Idea> ideas;

    public PrivateFeed() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_private_feed, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Find recycler view
        rvPosts = view.findViewById(R.id.rvPosts);

        // Init the list of tweets and adapter
        ideas = new ArrayList<Idea>();
        adapter = new IdeaAdapter(getContext(), ideas);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());

        // Recycler view setup: layout manager and adapter
        rvPosts.setLayoutManager(linearLayoutManager);
        rvPosts.setAdapter(adapter);

        // Retain an instance so that you can call `resetState()` for fresh searches
        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                Idea lastPost = ideas.get(ideas.size() - 1);
                // Add whatever code is needed to append new items to the bottom of the list
                loadNextData(lastPost.getCreatedAt());
            }
        };

        // Adds the scroll listener to RecyclerView
        rvPosts.addOnScrollListener(scrollListener);

        // Refreshing swipe layout
        // Lookup the swipe container view
        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                adapter.clear();
                queryPosts();
                swipeContainer.setRefreshing(false);
            }
        });

        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        // query posts from parse
        queryPosts();
    }

    public void loadNextData(Date date) {
        // specify what type of data we want to query - Post.class
        ParseQuery<Idea> query = ParseQuery.getQuery(Idea.class);
        // include data referred by user key
        query.include(Idea.KEY_USER);
        // limit query to latest 20 items
        query.setLimit(20);
        // order posts by creation date (newest first)
        query.addDescendingOrder("createdAt");

        query.whereLessThan("createdAt", date);
        // start an asynchronous call for posts
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

                // save received posts to list and notify adapter of new data
                ideas.addAll(feed);
                adapter.notifyDataSetChanged();
            }
        });
    }

    // Use Parse to query for the last 20 posts
    private void queryPosts() {
        // specify what type of data we want to query - Post.class
        ParseQuery<Idea> query = ParseQuery.getQuery(Idea.class);
        // include data referred by user key
        query.include(Idea.KEY_USER);
        // limit query to latest 20 items
        query.setLimit(20);
        // order posts by creation date (newest first)
        query.addDescendingOrder("createdAt");
        // start an asynchronous call for posts
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

                // save received posts to list and notify adapter of new data
                ideas.addAll(feed);
                adapter.notifyDataSetChanged();
            }
        });
    }
}