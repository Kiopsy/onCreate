package com.example.onCreate.models;

import android.util.Log;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Date;

// An idea object that is the foundation of a post in onCreate();
@ParseClassName("Idea")
public class Idea extends ParseObject {

    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_USER = "user";
    public static final String KEY_TITLE = "title";
    public static final String KEY_UPVOTES = "upvotes";
    public static final String KEY_DOWNVOTES = "downvotes";
    public static final String KEY_STARRED = "starred";
    public static final String KEY_VISIBILITY = "isPrivate";
    public static final String KEY_ARRAY_UPVOTE = "upvoteUsers";
    public static final String KEY_ARRAY_DOWNVOTE = "downvoteUsers";
    public static final String KEY_NET_VOTES = "netVotes";
    public static final String KEY_ARRAY_TAGS = "tags";

    public String getDescription() {
        return getString(KEY_DESCRIPTION);
    }

    public void setDescription(String description) {
        put(KEY_DESCRIPTION, description);
    }

    public ParseFile getImage() {
        return getParseFile(KEY_IMAGE);
    }

    public void setImage(ParseFile parseFile) {
        put(KEY_IMAGE, parseFile);
    }

    public ParseUser getUser() {
        return getParseUser(KEY_USER);
    }

    public void setUser(ParseUser user) {
        put(KEY_USER, user);
    }

    public String getTitle() {
        return getString(KEY_TITLE);
    }

    public void setTitle(String title) {
        put(KEY_TITLE, title);
    }

    public int getUpvotes() { return getInt(KEY_UPVOTES); }

    public void setUpvotes(int upvotes) { put(KEY_UPVOTES, upvotes); }

    public int getDownvotes() { return getInt(KEY_DOWNVOTES); }

    public void setDownvotes(int downvotes) { put(KEY_DOWNVOTES, downvotes); }

    public boolean getStarred() {
        return getBoolean(KEY_STARRED);
    }

    public void setStarred(boolean starred) {
        put(KEY_STARRED, starred);
    }

    public boolean getVisibility() {
        return getBoolean(KEY_VISIBILITY);
    }

    public void setVisibility(boolean isPrivate) {
        put(KEY_VISIBILITY, isPrivate);
    }

    public ArrayList<ParseUser> getUpvoteUsers() { return (ArrayList) get(KEY_ARRAY_UPVOTE); }

    public void setUpvoteUsers(ArrayList<ParseUser> users) { put(KEY_ARRAY_UPVOTE, users); }

    public ArrayList<ParseUser> getDownvoteUsers() { return (ArrayList) get(KEY_ARRAY_DOWNVOTE); }

    public void setDownvoteUsers(ArrayList<ParseUser> users) { put(KEY_ARRAY_DOWNVOTE, users); }

    public int getNetVotes() { return getInt(KEY_NET_VOTES); }

    public void setNetVotes(int netVotes) { put(KEY_NET_VOTES, netVotes); }

    public ArrayList<String> getTags() { return (ArrayList<String>) get(KEY_ARRAY_TAGS); }

    public void setTags(ArrayList<String> tags) { put(KEY_ARRAY_TAGS, tags); }

    public static String calculateTimeAgo(Date createdAt) {

        int SECOND_MILLIS = 1000;
        int MINUTE_MILLIS = 60 * SECOND_MILLIS;
        int HOUR_MILLIS = 60 * MINUTE_MILLIS;
        int DAY_MILLIS = 24 * HOUR_MILLIS;

        try {
            createdAt.getTime();
            long time = createdAt.getTime();
            long now = System.currentTimeMillis();

            final long diff = now - time;
            if (diff < MINUTE_MILLIS) {
                return "just now";
            } else if (diff < 2 * MINUTE_MILLIS) {
                return "a minute ago";
            } else if (diff < 50 * MINUTE_MILLIS) {
                return diff / MINUTE_MILLIS + "m";
            } else if (diff < 90 * MINUTE_MILLIS) {
                return "an hour ago";
            } else if (diff < 24 * HOUR_MILLIS) {
                return diff / HOUR_MILLIS + "h";
            } else if (diff < 48 * HOUR_MILLIS) {
                return "yesterday";
            } else {
                return diff / DAY_MILLIS + "d";
            }
        } catch (Exception e) {
            Log.i("Error:", "getRelativeTimeAgo failed", e);
            e.printStackTrace();
        }

        return "";
    }
}

