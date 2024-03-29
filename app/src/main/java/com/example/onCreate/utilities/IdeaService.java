package com.example.onCreate.utilities;

import android.util.Log;

import com.example.onCreate.adapters.IdeaAdapter;
import com.example.onCreate.models.Idea;
import com.example.onCreate.models.StringSuggestion;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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

        pattern = fixString(pattern);

        // A list of ideas that contain the pattern string
        List<Idea> relevantIdeas = new ArrayList<Idea>();
        // Using the Boyer-Moore Fast String Searching Algorithm
        char[] pat = pattern.toCharArray();
        for (Idea idea : mRecentQuery) {
            char[] description = idea.getDescription().toCharArray();
            char[] title = idea.getTitle().toCharArray();
            char[] tags = stringListToCharArray(idea.getTags());

            // Add idea to list if pattern is contained within title/description
            int descriptionIndex = boyerMooreSearch(pat, description);
            int titleIndex = boyerMooreSearch(pat, title);
            int tagIndex = boyerMooreSearch(pat, tags);

            if (descriptionIndex != -1 || titleIndex != -1 || tagIndex != -1) {
                relevantIdeas.add(idea);
            }
        }
        return relevantIdeas;
    }

    public List<StringSuggestion> searchStrings(String pattern) {

        pattern = fixString(pattern);
        // A list of ideas that contain the pattern string
        List<StringSuggestion> relevantStrings = new ArrayList<>();
        HashSet<String> relevantHash = new HashSet<>();

        if (pattern.equals("")) {
            return relevantStrings;
        }

        // Using the Boyer-Moore Fast String Searching Algorithm
        char[] pat = pattern.toCharArray();
        for (Idea idea : mRecentQuery) {
            char[] description = idea.getDescription().toCharArray();
            char[] title = idea.getTitle().toCharArray();
            char[] tags = stringListToCharArray(idea.getTags());

            // Add idea to list if pattern is contained within title/description
            int descriptionIndex = boyerMooreSearch(pat, description);
            int titleIndex = boyerMooreSearch(pat, title);
            int tagIndex = boyerMooreSearch(pat, tags);

            if (descriptionIndex != -1) {
                String atIndex = StringAtIndex(description, descriptionIndex);
                if (relevantHash.size() < 7) {
                    relevantHash.add(atIndex);
                }
            }
            if (titleIndex != -1) {
                String atIndex = StringAtIndex(title, titleIndex);
                if (relevantHash.size() < 7) {
                    relevantHash.add(atIndex);
                }
            }
            if (tagIndex != -1) {
                String atIndex = StringAtIndex(tags, tagIndex);
                if (relevantHash.size() < 7) {
                    relevantHash.add(atIndex);
                }
            }
        }

        Iterator<String> it = relevantHash.iterator();
        while (it.hasNext()) {
            relevantStrings.add(new StringSuggestion(it.next()));
        }

        return relevantStrings;
    }

    private String fixString(String pattern) {
        char[] pat = pattern.toCharArray();
        StringBuilder builder = new StringBuilder(pat.length);
        for(char ch: pat)
        {
            if (ch != 0) {
                builder.append(ch);
            }
        }
        return builder.toString();

    }

    /** Boyer-Moore Fast String Searching Algorithm:
     *          Searches by the last character in the pattern
     *
     *  Returns true or false whether there is a pattern occurrence within a string text.
    **/

    private int boyerMooreSearch(char[] pattern, char[] text) {
        int textLen = text.length;
        int patLen = pattern.length;

        // Accounting for the empty string case
        if (patLen == 0) {
            return 0;
        }

        Map<Character, Integer> last = new HashMap<>();
        for (int i = 0; i < textLen; i++) {
            last.put(Character.toLowerCase(text[i]), -1);
        }
        for (int i = 0; i < patLen; i++) {
            last.put(Character.toLowerCase(pattern[i]), i);
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
                    return i;
                }
                // Iterates backwards through text, starting at the last letter
                i--; k--;
            } else {
                i += patLen - Math.min(k, 1 + last.get(Character.toLowerCase(text[i])));
                k = patLen - 1;
            }
        }
        return -1;
    }

    // Searches for a complete string around an index in a char array
    private String StringAtIndex(char[] text, int index) {
        int l = index;
        int r = index;

        while (l >= 0) {
            if (!Character.isAlphabetic(text[l])) {
                l++;
                break;
            } else if (l == 0) {
                // edge case, nonAlphabetic char is the at endpoints
                break;
            }
            l--;
        }
        while (r < text.length) {
            if (!Character.isAlphabetic(text[r])) {
                r--;
                break;
            }
            r++;
        }

        for (int i = l; i < r; i++) {
            text[i] = Character.toLowerCase(text[i]);
        }
        return new String(Arrays.copyOfRange(text, l, r + 1));
    }

    private char[] stringListToCharArray(ArrayList<String> tags) {
        String totalWords = "";

        for (int i = 0; i < tags.size(); i++) {
            totalWords += tags.get(i) + "";
        }

        return totalWords.toCharArray();
    }
}
