package com.example.onCreate.utilities;

import android.util.Log;

import com.example.onCreate.models.Idea;
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
    private List<Idea> mRecentQuery;
    private boolean mIsPrivated;

    // Query post request codes:
    public static final int REQUEST_RECENTS = 1;
    public static final int REQUEST_ENDLESS_SCROLL = 2;
    public static final int REQUEST_STARRED  = 3;
    public static final int REQUEST_OLDEST = 4;
    public static final int REQUEST_SEARCH = 5;
    public static final int REQUEST_TOP  = 6;
    private int mCurrentFilterRequest = REQUEST_RECENTS;

    public IdeaService (boolean isPrivated) {
        this.mIsPrivated = isPrivated;
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
    public void queryPosts(Idea lastIdea, int requestCode) {
        // specify what type of data we want to query - Post.class
        ParseQuery<Idea> query = ParseQuery.getQuery(Idea.class);
        // include data referred by user key
        query.include(Idea.KEY_USER);
        if (mIsPrivated) {
            // find only private posts
            query.whereEqualTo("isPrivate", true);
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
//            adapter.clear();
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

        try {
            mRecentQuery = query.find();
        } catch (ParseException e) {
            Log.e(TAG, "Issue with getting posts", e);
        }
    }

    // Gets the idea list from the most recent query.
    public List<Idea> getRecentQuery() {
        return mRecentQuery;
    }

    // Gets all the strings from idea titles and descriptions
    public List<String> getQueryStrings() {
        List<String> allQueryStrings = new ArrayList<String>();

        for (Idea idea : mRecentQuery) {
            // Add title and description from the recent query into string list
            allQueryStrings.add(idea.getTitle());
            allQueryStrings.add(idea.getDescription());
        }
        return allQueryStrings;
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

    // Boyer-Moore Fast String Searching Algorithm:
    // https://www.cs.utexas.edu/users/moore/best-ideas/string-searching/
    private boolean boyerMooreSearch(char[] pattern, char[] text) {
        int n = text.length;
        int m = pattern.length;

        if (m == 0) {
            return true;
        }
        Map<Character, Integer> last = new HashMap<>();
        for (int i = 0; i < n; i++) {
            last.put(text[i], -1);
        }
        for (int i = 0; i < m; i++) {
            last.put(pattern[i], i);
        }
        int i = m - 1;
        int k = m - 1;
        while (i < n) {
            if (text[i] == pattern[k]) {
                if (k == 0) {
                    return true;
                }
                i--; k--;
            }
            else
            {
                i += m - Math.min(k, 1 + last.get(text[i]));
                k = m - 1;
            }
        }
        return false;
    }
}
