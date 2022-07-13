package com.example.onCreate.fragments;

import static com.example.onCreate.utilities.IdeaService.REQUEST_ENDLESS_SCROLL;
import static com.example.onCreate.utilities.IdeaService.REQUEST_OLDEST;
import static com.example.onCreate.utilities.IdeaService.REQUEST_RECENTS;
import static com.example.onCreate.utilities.IdeaService.REQUEST_SEARCH;
import static com.example.onCreate.utilities.IdeaService.REQUEST_TOP;

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
import android.widget.SearchView;
import android.widget.TextView;

import com.example.onCreate.R;
import com.example.onCreate.adapters.IdeaAdapter;
import com.example.onCreate.dialogs.FilterDialog;
import com.example.onCreate.models.Idea;
import com.example.onCreate.utilities.EndlessRecyclerViewScrollListener;
import com.example.onCreate.utilities.IdeaService;
import com.parse.FindCallback;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GlobalFeedFragment extends Fragment {

    public static final String TAG = "GlobalFeedFragment";
    private EndlessRecyclerViewScrollListener mScrollListener;
    private SwipeRefreshLayout mSwipeContainer;
    private TextView mTvFilter;
    private FilterDialog mFilterDialog;
    protected RecyclerView mRvPosts;
    protected IdeaAdapter mAdapter;
    protected List<Idea> mIdeas;
    private IdeaService mIdeaService;
    private SearchView mSearchView;
    private int mCurrentFilterRequest = REQUEST_RECENTS;

    public GlobalFeedFragment() {
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
        mAdapter = new IdeaAdapter(getContext(), mIdeas, false);

        // Init the idea Parse manager
        mIdeaService = new IdeaService(mAdapter, mIdeas, false);

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
                if (mCurrentFilterRequest != REQUEST_SEARCH) {
                    mIdeaService.queryPosts(lastPost, REQUEST_ENDLESS_SCROLL);
                    clearSearch();
                }
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
                if (mCurrentFilterRequest == REQUEST_SEARCH) {
                    mCurrentFilterRequest = REQUEST_RECENTS;
                }

                mIdeaService.queryPosts(null, mCurrentFilterRequest);
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
                mFilterDialog.setTopOnClick(TopOnClickListener());
                mFilterDialog.setOldestOnClick(OldestOnClickListener());

                // Making the dialog visible
                mFilterDialog.showDialog(getActivity());
            }
        });

        // Search functionalities
        mSearchView = view.findViewById(R.id.searchView);
        mIdeaService.queryPosts(null, REQUEST_RECENTS);

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.clear();
                mCurrentFilterRequest = REQUEST_SEARCH;
                mIdeas.addAll(mIdeaService.searchIdeas(newText));
                mAdapter.notifyDataSetChanged();
                return true;
            }
        });
    }

    // Setting on click listeners
    // Upvoting
    public static View.OnClickListener upvoteOnClickListener(View itemView, Idea idea) {
        // Retrieving idea & view from a post
        ImageView ivUpvote = itemView.findViewById(R.id.ivUpvote);
        ImageView ivDownvote = itemView.findViewById(R.id.ivDownvotes);
        TextView tvVotes = itemView.findViewById(R.id.tvVotes);

        // Creating the upvoteListener
        View.OnClickListener upvoteListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if current user is already upvoted on the post
                ParseUser currentUser = ParseUser.getCurrentUser();
                ArrayList<ParseUser> upvoteUsers = idea.getUpvoteUsers();
                ArrayList<ParseUser> downvoteUsers = idea.getDownvoteUsers();
                boolean hasUpvoted = upvoteUsers != null ? containsUser(upvoteUsers, currentUser) : false;
                boolean hasDownvoted = downvoteUsers != null ? containsUser(downvoteUsers, currentUser) : false;
                int upvotes = idea.getUpvotes();
                int downvotes = idea.getDownvotes();

                if (hasUpvoted) {
                    // Un-upvote a post: remove current user from upvote list and change the upvote count
                    unUpvote(idea, currentUser, downvoteUsers);
                    upvotes--;
                } else {
                    // Upvote a post: add current user to upvote list and change the upvote count
                    if (hasDownvoted) {
                        unDownvote(idea, currentUser, upvoteUsers);
                        downvotes--;
                        ivDownvote.setSelected(false);
                    }
                    upvote(idea, currentUser, downvoteUsers);
                    upvotes++;
                }

                // Change upvote text & image based on previous interaction
                ivUpvote.setSelected(!hasUpvoted);
                tvVotes.setText(Integer.toString(upvotes - downvotes));

                idea.saveInBackground();
            }
        };
        return upvoteListener;
    }

    // Downvoting
    public static View.OnClickListener downvoteOnClickListener(View itemView, Idea idea) {
        // Retrieving idea & view from a post
        ImageView ivUpvote = itemView.findViewById(R.id.ivUpvote);
        ImageView ivDownvote = itemView.findViewById(R.id.ivDownvotes);
        TextView tvVotes = itemView.findViewById(R.id.tvVotes);

        // Creating the upvoteListener
        View.OnClickListener upvoteListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if current user is already upvoted on the post
                ParseUser currentUser = ParseUser.getCurrentUser();
                ArrayList<ParseUser> upvoteUsers = idea.getUpvoteUsers();
                ArrayList<ParseUser> downvoteUsers = idea.getDownvoteUsers();
                boolean hasUpvoted = upvoteUsers != null ? containsUser(upvoteUsers, currentUser) : false;
                boolean hasDownvoted = downvoteUsers != null ? containsUser(downvoteUsers, currentUser) : false;
                int upvotes = idea.getUpvotes();
                int downvotes = idea.getDownvotes();

                if (hasDownvoted) {
                    // Un-downvote a post: remove current user from upvote list and change the upvote count
                    unDownvote(idea, currentUser, downvoteUsers);
                    downvotes--;
                } else {
                    // Downvote a post: add current user to upvote list and change the upvote count
                    if (hasUpvoted) {
                        unUpvote(idea, currentUser, upvoteUsers);
                        upvotes--;
                        ivUpvote.setSelected(false);
                    }
                    downvote(idea, currentUser, downvoteUsers);
                    downvotes++;
                }

                // Change upvote text & image based on previous interaction
                ivDownvote.setSelected(!hasDownvoted);
                tvVotes.setText(Integer.toString(upvotes - downvotes));

                idea.saveInBackground();
            }
        };
        return upvoteListener;
    }

    // Filter dialog button onClick listeners
    // Top: Queries the top upvoted posts
    private View.OnClickListener TopOnClickListener() {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIdeaService.queryPosts(null, REQUEST_TOP);
                mFilterDialog.hideDialog();
                mCurrentFilterRequest = REQUEST_TOP;
                clearSearch();
            }
        };
        return listener;
    }

    // Recent: Queries for the most recently published posts
    private View.OnClickListener RecentOnClickListener() {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIdeaService.queryPosts(null, REQUEST_RECENTS);
                mFilterDialog.hideDialog();
                mCurrentFilterRequest = REQUEST_RECENTS;
                clearSearch();
            }
        };
        return listener;
    }

    // Oldest: Queries for the oldest posts
    private View.OnClickListener OldestOnClickListener() {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIdeaService.queryPosts(null, REQUEST_OLDEST);
                mFilterDialog.hideDialog();
                mCurrentFilterRequest = REQUEST_OLDEST;
                clearSearch();
            }
        };
        return listener;
    }

    // Bridging functionality between up/downvote buttons and Parse
    private static void upvote(Idea idea, ParseUser currentUser, ArrayList<ParseUser> upvoteUsers) {
        idea.add("upvoteUsers", currentUser);
        idea.setUpvotes(idea.getUpvotes() + 1);
    }

    private static void unUpvote(Idea idea, ParseUser currentUser, ArrayList<ParseUser> upvoteUsers) {
        idea.setUpvoteUsers(removeUser(upvoteUsers, currentUser));
        idea.setUpvotes(idea.getUpvotes() - 1);
    }

    private static void downvote(Idea idea, ParseUser currentUser, ArrayList<ParseUser> downvoteUsers) {
        idea.add("downvoteUsers", currentUser);
        idea.setDownvotes(idea.getDownvotes() + 1);
    }

    private static void unDownvote(Idea idea, ParseUser currentUser, ArrayList<ParseUser> downvoteUsers) {
        idea.setDownvoteUsers(removeUser(downvoteUsers, currentUser));
        idea.setDownvotes(idea.getDownvotes() - 1);
    }

    // Checks if an array of ParseUsers contains a specific user
    public static boolean containsUser (ArrayList<ParseUser> allUsers, ParseUser user) {
        String userId = user.getObjectId();
        for (ParseUser u :allUsers) {
            if (u.getObjectId().equals(userId)) {
                return true;
            }
        }
        return false;
    }

    // Remove a user from a list of ParseUsers by Object id
    public static ArrayList<ParseUser> removeUser(ArrayList<ParseUser> allUsers, ParseUser user) {
        String userId = user.getObjectId();
        for (ParseUser u :allUsers) {
            if (u.getObjectId().equals(userId)) {
                allUsers.remove(u);
                break;
            }
        }
        return allUsers;
    }

    // Clear the SearchView upon refresh
    private void clearSearch() {
        mSearchView.setQuery("", false);
        mSearchView.setIconified(true);;
    }
}