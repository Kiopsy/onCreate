package com.example.onCreate.fragments;

import static com.example.onCreate.utilities.IdeaService.REQUEST_ENDLESS_SCROLL;
import static com.example.onCreate.utilities.IdeaService.REQUEST_OLDEST;
import static com.example.onCreate.utilities.IdeaService.REQUEST_RECENTS;
import static com.example.onCreate.utilities.IdeaService.REQUEST_SEARCH;
import static com.example.onCreate.utilities.IdeaService.REQUEST_TOP;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.codepath.asynchttpclient.AsyncHttpClient;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.example.onCreate.R;
import com.example.onCreate.activities.BrainstormActivity;
import com.example.onCreate.adapters.IdeaAdapter;
import com.example.onCreate.dialogs.FilterDialog;
import com.example.onCreate.models.Idea;
import com.example.onCreate.models.StringSuggestion;
import com.example.onCreate.utilities.EndlessRecyclerViewScrollListener;
import com.example.onCreate.utilities.IdeaService;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.parse.FindCallback;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.Headers;

public class GlobalFeedFragment extends Fragment {

    public static final String TAG = "GlobalFeedFragment";
    private EndlessRecyclerViewScrollListener mScrollListener;
    private SwipeRefreshLayout mSwipeContainer;
    private FilterDialog mFilterDialog;
    protected RecyclerView mRvPosts;
    protected IdeaAdapter mAdapter;
    protected List<Idea> mIdeas;
    private IdeaService mIdeaService;
    private FloatingSearchView mSearchView;
    private ExtendedFloatingActionButton mFloatingActionBtn;
    private int mCurrentFilterRequest = REQUEST_RECENTS;
    private AtomicBoolean mClear = new AtomicBoolean();

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
                    mIdeaService.queryPosts(lastPost, REQUEST_ENDLESS_SCROLL, false);
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

                mIdeaService.queryPosts(null, mCurrentFilterRequest, false);
                mSwipeContainer.setRefreshing(false);
            }
        });

        // Configure the refreshing colors
        mSwipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        // Search functionalities
        mSearchView = view.findViewById(R.id.searchView);

        mIdeaService.queryPosts(null, REQUEST_SEARCH, false);

        // Filter menu button/dialog
        mSearchView.setOnMenuItemClickListener(new FloatingSearchView.OnMenuItemClickListener() {
            @Override
            public void onActionMenuItemSelected(MenuItem item) {
                if (item.getItemId() == R.id.filterBtn) {
                    // Creating a dialog
                    mFilterDialog = new FilterDialog(false);

                    // Setting dialog onClick listeners
                    mFilterDialog.setRecentOnClick(RecentOnClickListener());
                    mFilterDialog.setTopOnClick(TopOnClickListener());
                    mFilterDialog.setOldestOnClick(OldestOnClickListener());

                    // Making the dialog visible
                    mFilterDialog.showDialog(getActivity());
                }
            }
        });

        mSearchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {
            @Override
            public void onSearchTextChanged(String oldQuery, final String newText) {
                mClear.set(false);
                search(newText);
            }
        });

        mSearchView.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override
            public void onSuggestionClicked(SearchSuggestion searchSuggestion) {
                char[] test = searchSuggestion.getBody().toCharArray();
                mSearchView.setSearchText(searchSuggestion.getBody());
                mSearchView.clearSearchFocus();
                mClear.set(true);
                search(searchSuggestion.getBody());
            }

            @Override
            public void onSearchAction(String currentQuery) {
                mClear.set(true);
                search(currentQuery);
                mSearchView.clearSearchFocus();
            }
        });

        mFloatingActionBtn = view.findViewById(R.id.fab);

        mFloatingActionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), BrainstormActivity.class);
                // start the activity and set transitions
                getActivity().startActivity(i);
                getActivity().overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_top);
            }
        });
    }

    private void search(String newText) {
        ArrayList<StringSuggestion> suggestions = new ArrayList<>();

        suggestions.addAll(mIdeaService.searchStrings(newText));


        //get suggestions based on newQuery
        String SEARCH_URL = "https://suggestqueries.google.com/complete/search?client=firefox&q=";

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(SEARCH_URL + newText, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.d(TAG, "onSuccess");
                JSONArray jsonArray = json.jsonArray;
                try {
                    JSONArray googleSuggestions = jsonArray.getJSONArray(1);

                    int sugLen = Math.min(googleSuggestions.length() - suggestions.size(), 6 - suggestions.size());
                    for (int i = 0; i < sugLen; i ++) {
                        suggestions.add(new StringSuggestion(googleSuggestions.getString(i)));
                    }


                    if (!mClear.get()) {
                        //pass them on to the search view
                        mSearchView.swapSuggestions(suggestions);
                    } else {
                        mSearchView.clearSuggestions();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.d(TAG, "onFailure" + throwable);
            }
        });

        mAdapter.clear();
        mCurrentFilterRequest = REQUEST_SEARCH;
        mIdeas.addAll(mIdeaService.searchIdeas(newText));
        mAdapter.notifyDataSetChanged();
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

        // Creating the downvoteListener
        View.OnClickListener downvoteListener = new View.OnClickListener() {
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
        return downvoteListener;
    }

    // Filter dialog button onClick listeners
    // Top: Queries the top upvoted posts
    private View.OnClickListener TopOnClickListener() {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRvPosts.scrollToPosition(0);
                mIdeaService.queryPosts(null, REQUEST_TOP, true);
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
                mRvPosts.scrollToPosition(0);
                mIdeaService.queryPosts(null, REQUEST_RECENTS, true);
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
                mRvPosts.scrollToPosition(0);
                mIdeaService.queryPosts(null, REQUEST_OLDEST, true);
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

    // Update visuals for ann idea
    public void updateVisuals(Idea idea, int position) {
        mIdeas.set(position, idea);
        mAdapter.notifyItemChanged(position);
        mRvPosts.smoothScrollToPosition(position);
    }

    // Clear the SearchView upon refresh
    private void clearSearch() {
        mSearchView.clearSearchFocus();
        mSearchView.clearQuery();
    }
}