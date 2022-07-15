package com.example.onCreate.utilities;

import android.util.Log;

import com.example.onCreate.adapters.IdeaAdapter;
import com.example.onCreate.models.Idea;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to connect Idea models with Parse.
 */
public class IdeaService {

    private static final String TAG = "IdeaService";
    private List<Idea> mRecentQuery = new ArrayList<>();
    private boolean mIsPrivated;
    private IdeaAdapter mAdapter;
    private List<Idea> mIdeas;

    // Query post request codes:
    public static final int REQUEST_RECENTS = 1;
    public static final int REQUEST_ENDLESS_SCROLL = 2;
    public static final int REQUEST_SEARCH = 3;
    public static final int REQUEST_STARRED  = 4;
    public static final int REQUEST_OLDEST = 5;
    public static final int REQUEST_TOP  = 6;
    private int mCurrentFilterRequest = REQUEST_RECENTS;

    public IdeaService (IdeaAdapter adapter, List<Idea> ideas, boolean isPrivated) {
        this.mIsPrivated = isPrivated;
        this.mAdapter = adapter;
        this.mIdeas = ideas;
    }

    /**
     * Queries idea posts from Parse.
     *
     * @param lastIdea Should have the last idea post seen in the Endless
     *                 Scrolling case. Should be null otherwise
     *
     * @param requestCode Should have the type of request code to query the
     *                    specific results accordingly.
     */
    public void queryPosts(Idea lastIdea, int requestCode, boolean isDialog) {
        // specify what type of data we want to query - Post.class
        ParseQuery<Idea> query = ParseQuery.getQuery(Idea.class);
        // include data referred by user key
        query.include(Idea.KEY_USER);
        // find only private posts
        query.whereEqualTo("isPrivate", mIsPrivated);
        if (mIsPrivated) {
            // find only the current user's posts
            query.whereEqualTo("user", ParseUser.getCurrentUser());
        }
        // Search request should not have a limit
        if(requestCode != REQUEST_SEARCH) {
            // limit query to latest 20 items
            query.setLimit(20);
        }
        // Clear adapter if not endless scrolling
        if (requestCode != REQUEST_ENDLESS_SCROLL) {
            mAdapter.clear();
            // Clear current idea list
            mRecentQuery.clear();
            // Sets the request code as the current filter request
            mCurrentFilterRequest = requestCode;
        }

        // Accounting for different query cases
        switch (requestCode) {
            case REQUEST_RECENTS:
            case REQUEST_SEARCH:
                // order posts by creation date (newest first)
                query.addDescendingOrder("createdAt");
                break;
            case REQUEST_ENDLESS_SCROLL:
                Date date = lastIdea.getCreatedAt();
                int upvotes = lastIdea.getUpvotes();
                // Must account for current filter state when performing endless scroll
                switch (mCurrentFilterRequest) {
                    case REQUEST_RECENTS:
                    case REQUEST_SEARCH:
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
                    case REQUEST_TOP:
                        if (date != null) {
                            query.whereLessThan("upvotes", upvotes);
                        }
                        query.addDescendingOrder("upvotes");
                        break;
                }
                break;

            case REQUEST_STARRED:
                // order posts by upvotes (highest upvoted first)
                query.whereEqualTo("starred", true);
                query.addDescendingOrder("createdAt");
                break;

            case REQUEST_TOP:
                // order posts by upvotes (highest upvoted first)
                query.addDescendingOrder("upvotes");
                break;

            case REQUEST_OLDEST:
                // order posts by creation date (oldest first)
                query.addAscendingOrder("createdAt");
                break;
        }

        // Only search in background for feed retrieving
        if (isDialog) {
            try {
                mRecentQuery.addAll(query.find());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            mIdeas.addAll(mRecentQuery);
            mAdapter.notifyDataSetChanged();
        } else{
            // start an asynchronous call for posts for the feed
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
                    mRecentQuery.addAll(feed);
                    mIdeas.addAll(feed);
                    mAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    // Gets the idea list from the most recent query.
    public List<Idea> getRecentQuery() {
        return mRecentQuery;
    }

    // Searches most recent queries to see if it contains a specified pattern
    public List<Idea> searchIdeas(String pattern) {
        // A list of ideas that contain the pattern string
        List<Idea> relevantIdeas = new ArrayList<Idea>();
        // Using the Boyer-Moore Fast String Searching Algorithm
        char[] pat = pattern.toCharArray();
        for (Idea idea : mRecentQuery) {
            char[] description = idea.getDescription().toCharArray();
            char[] title = idea.getTitle().toCharArray();

            // Add idea to list if pattern is contained within title/description
            if (boyerMooreSearch(pat, description) || boyerMooreSearch(pat, title)) {
                relevantIdeas.add(idea);
            }
        }
        return relevantIdeas;
    }

    /** Boyer-Moore Fast String Searching Algorithm:
     *          Searches by the last character in the pattern
     *
     *  Returns true or false whether there is a pattern occurrence within a string text.
    **/
    private boolean boyerMooreSearch(char[] pattern, char[] text) {
        int textLen = text.length;
        int patLen = pattern.length;

        // Accounting for the empty string case
        if (patLen == 0) {
            return true;
        }


        Map<Character, Integer> last = new HashMap<>();
        for (int i = 0; i < textLen; i++) {
            last.put(text[i], -1);
        }
        for (int i = 0; i < patLen; i++) {
            last.put(pattern[i], i);
        }

        // Text & Pattern Pointer
        // Note: both start at the end of the pattern text
        int i = patLen - 1;
        int k = patLen - 1;

        // Searches through entire text
        while (i < textLen) {
            // Compares characters, ignoring case
            if (Character.toLowerCase(text[i]) == Character.toLowerCase(pattern[k])) {
                // If the pointer has reached the start of the pattern string, a match is found
                if (k == 0) {
                    return true;
                }
                // Iterates backwards through text, starting at the last letter
                i--; k--;
            } else {
                i += patLen - Math.min(k, 1 + last.get(text[i]));
                k = patLen - 1;
            }
        }
        return false;
    }
}
