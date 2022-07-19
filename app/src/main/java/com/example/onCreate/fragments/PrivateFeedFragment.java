package com.example.onCreate.fragments;

import static com.example.onCreate.utilities.IdeaService.REQUEST_ENDLESS_SCROLL;
import static com.example.onCreate.utilities.IdeaService.REQUEST_OLDEST;
import static com.example.onCreate.utilities.IdeaService.REQUEST_RECENTS;
import static com.example.onCreate.utilities.IdeaService.REQUEST_SEARCH;
import static com.example.onCreate.utilities.IdeaService.REQUEST_STARRED;

import android.app.SearchManager;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.widget.SearchView.SearchAutoComplete;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import com.example.onCreate.R;
import com.example.onCreate.adapters.IdeaAdapter;
import com.example.onCreate.dialogs.FilterDialog;
import com.example.onCreate.models.Idea;
import com.example.onCreate.utilities.EndlessRecyclerViewScrollListener;
import com.example.onCreate.utilities.IdeaService;

import java.util.ArrayList;
import java.util.List;

public class PrivateFeedFragment extends Fragment {

    private static final String TAG = "PrivateFeedFragment";
    private EndlessRecyclerViewScrollListener mScrollListener;
    private SwipeRefreshLayout mSwipeContainer;
    private RecyclerView mRvPosts;
    private IdeaAdapter mAdapter;
    private List<Idea> mIdeas;
    private IdeaService mIdeaService;
    private TextView mTvFilter;
    private SearchView mSearchView;
    private FilterDialog mFilterDialog;
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

        // Search functionalities
        mSearchView = view.findViewById(R.id.searchView);
        mIdeaService.queryPosts(null, REQUEST_SEARCH, false);

        ArrayList<String> countries = new ArrayList<>();

        countries.add("France");
        countries.add("Italy");

        SearchManager manager = (SearchManager) getActivity().getSystemService(getContext().SEARCH_SERVICE);


        mSearchView.setSearchableInfo(manager.getSearchableInfo(getActivity().getComponentName()));

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
                mIdeaService.queryPosts(null, REQUEST_OLDEST, true);
                mFilterDialog.hideDialog();
                mCurrentFilterRequest = REQUEST_OLDEST;
                clearSearch();
            }
        };
        return listener;
    }

    // Clear the SearchView upon refresh
    private void clearSearch() {
        mSearchView.setQuery("", false);
        mSearchView.setIconified(true);;
    }
}