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
import android.widget.ImageView;
import android.widget.TextView;

import com.example.onCreate.R;
import com.example.onCreate.adapters.IdeaAdapter;
import com.example.onCreate.dialogs.FilterDialog;
import com.example.onCreate.models.Idea;
import com.example.onCreate.utilities.EndlessRecyclerViewScrollListener;
import com.parse.FindCallback;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PrivateFeedFragment extends Fragment {

    private static final String TAG = "PrivateFeedFragment";
    private EndlessRecyclerViewScrollListener mScrollListener;
    private SwipeRefreshLayout mSwipeContainer;
    private RecyclerView mRvPosts;
    private IdeaAdapter mAdapter;
    private List<Idea> mIdeas;
    private TextView mTvFilter;
    private FilterDialog mFilterDialog;

    // Query post request codes:
    private final int REQUEST_RECENTS = 1;
    private final int REQUEST_ENDLESS_SCROLL = 2;
    private final int REQUEST_STARRED  = 3;
    private final int REQUEST_OLDEST = 4;
    private int mCurrentFilterRequest = REQUEST_RECENTS;

    public PrivateFeedFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_feed, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Find recycler view
        mRvPosts = view.findViewById(R.id.rvPosts);

        // Init the list of tweets and adapter
        mIdeas = new ArrayList<Idea>();
        mAdapter = new IdeaAdapter(getContext(), mIdeas, true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());

        // Recycler view setup: layout manager and adapter
        mRvPosts.setLayoutManager(linearLayoutManager);
        mRvPosts.setAdapter(mAdapter);

        // Retain an instance so that you can call `resetState()` for fresh searches
        mScrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                Idea lastPost = mIdeas.get(mIdeas.size() - 1);
                // Add whatever code is needed to append new items to the bottom of the list
                queryPosts(lastPost, REQUEST_ENDLESS_SCROLL);
            }
        };

        // Adds the scroll listener to RecyclerView
        mRvPosts.addOnScrollListener(mScrollListener);

        // Refreshing swipe layout
        // Lookup the swipe container view
        mSwipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        mSwipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                mAdapter.clear();
                queryPosts(null, mCurrentFilterRequest);
                mSwipeContainer.setRefreshing(false);
            }
        });

        // Configure the refreshing colors
        mSwipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        // Filter button/dialog
        mTvFilter = view.findViewById(R.id.tvFilter);

        mTvFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Creating a dialog
                mFilterDialog = new FilterDialog(true);

                // Setting dialog onClick listeners
                mFilterDialog.setRecentOnClick(RecentOnClickListener());
                mFilterDialog.setStarredOnClick(StarredOnClickListener());
                mFilterDialog.setOldestOnClick(OldestOnClickListener());

                // Making the dialog visible
                mFilterDialog.showDialog(getActivity());
            }
        });

        // query posts from parse
        queryPosts(null, REQUEST_RECENTS);
    }

    // Use Parse to query for the last 20 posts
    // Optional: include date in endless scrolling case
    private void queryPosts(Idea lastPost, int requestCode) {
        // specify what type of data we want to query - Post.class
        ParseQuery<Idea> query = ParseQuery.getQuery(Idea.class);
        // include data referred by user key
        query.include(Idea.KEY_USER);
        // find only private posts
        query.whereEqualTo("isPrivate", true);
        // find only the current user's posts
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        // limit query to latest 20 items
        query.setLimit(20);
        // Clear adapter if not endless scrolling
        if (requestCode != REQUEST_ENDLESS_SCROLL) {
            mAdapter.clear();
            // Sets the request code as the current filter request
            mCurrentFilterRequest = requestCode;
        }

        // Accounting for different query cases
        switch (requestCode) {
            case REQUEST_RECENTS:
                // order posts by creation date (newest first)
                query.addDescendingOrder("createdAt");
                break;

            case REQUEST_ENDLESS_SCROLL:
                Date date = lastPost.getCreatedAt();
                // Must account for current filter state when performing endless scroll
                switch (mCurrentFilterRequest) {
                    case REQUEST_RECENTS:
                        if (date != null) {
                            query.whereLessThan("createdAt", date);
                        }
                        query.addDescendingOrder("createdAt");
                        break;

                    case REQUEST_OLDEST:
                        if (date != null) {
                            query.whereGreaterThan("createdAt", date);
                        }
                        query.addAscendingOrder("createdAt");
                        break;
                    case REQUEST_STARRED:
                        if (date != null) {
                            query.whereLessThan("createdAt", date);
                        }
                        query.whereEqualTo("starred", true);
                        query.addDescendingOrder("createdAt");
                        break;
                }
                break;

            case REQUEST_STARRED:
                // order posts by upvotes (highest upvoted first)
                query.whereEqualTo("starred", true);
                query.addDescendingOrder("createdAt");
                break;

            case REQUEST_OLDEST:
                // order posts by creation date (oldest first)
                query.addAscendingOrder("createdAt");
                break;
        }
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

                // save received posts to list and notify mAdapter of new data
                mIdeas.addAll(feed);
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    // Setting on click listeners
    public static View.OnClickListener starOnClickListener(View itemView, Idea idea) {
        ImageView mIvStars= itemView.findViewById(R.id.ivStars);
        View.OnClickListener starListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean star = !idea.getStarred();
                idea.setStarred(star);
                mIvStars.setSelected(star);
                idea.saveInBackground();
            }
        };
        return starListener;
    }

    // Filter dialog button onClick listeners
    // Starred: Queries only starred posts
    private View.OnClickListener StarredOnClickListener() {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queryPosts(null, REQUEST_STARRED);
                mFilterDialog.hideDialog();
            }
        };
        return listener;
    }

    // Recent: Queries for the most recently published posts
    private View.OnClickListener RecentOnClickListener() {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queryPosts(null, REQUEST_RECENTS);
                mFilterDialog.hideDialog();
            }
        };
        return listener;
    }

    // Oldest: Queries for the oldest posts
    private View.OnClickListener OldestOnClickListener() {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queryPosts(null, REQUEST_OLDEST);
                mFilterDialog.hideDialog();
            }
        };
        return listener;
    }
}