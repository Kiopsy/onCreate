package com.example.onCreate.fragments;

import static com.example.onCreate.utilities.IdeaService.REQUEST_ENDLESS_SCROLL;
import static com.example.onCreate.utilities.IdeaService.REQUEST_OLDEST;
import static com.example.onCreate.utilities.IdeaService.REQUEST_RECENTS;
import static com.example.onCreate.utilities.IdeaService.REQUEST_SEARCH;
import static com.example.onCreate.utilities.IdeaService.REQUEST_STARRED;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
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

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.Headers;

public class PrivateFeedFragment extends Fragment {

    private static final String TAG = "PrivateFeedFragment";
    private EndlessRecyclerViewScrollListener mScrollListener;
    private SwipeRefreshLayout mSwipeContainer;
    private RecyclerView mRvPosts;
    private IdeaAdapter mAdapter;
    private List<Idea> mIdeas;
    private IdeaService mIdeaService;
    private FloatingSearchView mSearchView;
    private FilterDialog mFilterDialog;
    private ExtendedFloatingActionButton mFloatingActionBtn;
    private int mCurrentFilterRequest = REQUEST_RECENTS;
    private AtomicBoolean mClear = new AtomicBoolean();

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

        // Init the idea Parse manager
        mIdeaService = new IdeaService(mAdapter, mIdeas, true);

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
                clearSearch();
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
                    mFilterDialog = new FilterDialog(true);

                    // Setting dialog onClick listeners
                    mFilterDialog.setRecentOnClick(RecentOnClickListener());
                    mFilterDialog.setStarredOnClick(StarredOnClickListener());
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

        mSearchView.setOnClearSearchActionListener(new FloatingSearchView.OnClearSearchActionListener() {
            @Override
            public void onClearSearchClicked() {
                mClear.set(true);
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

    // Function to search for a specific text and to add suggestions in SearchView
    private void search(String newText) {
        ArrayList<StringSuggestion> suggestions = new ArrayList<>();

        // Add suggestions based on ideas currently in database
        suggestions.addAll(mIdeaService.searchStrings(newText));

        //get suggestions based on newQuery
        String SEARCH_URL = "https://suggestqueries.google.com/complete/search?client=firefox&q=";

        // Connect with Google Search API
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(SEARCH_URL + newText, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.d(TAG, "onSuccess");
                JSONArray jsonArray = json.jsonArray;
                try {
                    JSONArray googleSuggestions = jsonArray.getJSONArray(1);

                    // Change the length of the suggestion list based on current suggestion size from database
                    int sugLen = Math.min(googleSuggestions.length() - suggestions.size(), 6 - suggestions.size());

                    // Add remaining suggestions from Google Search API
                    for (int i = 0; i < sugLen; i ++) {
                        suggestions.add(new StringSuggestion(googleSuggestions.getString(i)));
                    }

                    // Boolean to distinguish between when to clear search ideas
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
                mRvPosts.scrollToPosition(0);
                mIdeaService.queryPosts(null, REQUEST_STARRED, true);
                mFilterDialog.hideDialog();
                mCurrentFilterRequest = REQUEST_STARRED;
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

    // method to convert text to a bitmap for FloatingActionButton
    public static Bitmap textAsBitmap(String text, float textSize, int textColor) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(textSize);
        paint.setColor(textColor);
        paint.setTextAlign(Paint.Align.LEFT);
        float baseline = -paint.ascent(); // ascent() is negative
        int width = (int) (paint.measureText(text) + 0.0f); // round
        int height = (int) (baseline + paint.descent() + 0.0f);
        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(image);
        canvas.drawText(text, 0, baseline, paint);
        return image;
    }

    // Update visuals for ann idea
    public void updateVisuals(Idea idea, int position) {
        mIdeas.set(position, idea);
        mAdapter.notifyItemChanged(position);
    }

    // Clear the SearchView upon refresh
    private void clearSearch() {
        mSearchView.clearQuery();
        mSearchView.clearSearchFocus();
    }
}