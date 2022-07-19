package com.example.onCreate.utilities;

import android.util.Log;

import java.util.ArrayList;
import com.codepath.asynchttpclient.AsyncHttpClient;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Headers;

// Client to connect with Google's autocomplete text search
public class AutoCompleteClient {

    public static final String SEARCH_URL = "http://suggestqueries.google.com/complete/search?client=firefox&q=";
    public static final String TAG = "AutoCompleteClient";

    /**
     * Function that uses Google Search API to return a list of strings similar to input
     * for autocomplete features.
     *
     * @param search:  Initial string to query for.
     *
     * @return A list of similar strings to input for autocomplete usage.
     */
    public static ArrayList<String> autocomplete(String search) {
        // List of autocomplete suggestions
        ArrayList<String> suggestions = new ArrayList<>();

        // API client
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(SEARCH_URL + search, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.d(TAG, "onSuccess");
                JSONObject jsonObject = json.jsonObject;
                try {
                    JSONArray results = jsonObject.getJSONArray("results");
//                    Log.i(TAG, "Results: " + results.toString());
//                    movies.addAll(Movie.fromJsonArray(results));
//                    movieAdapter.notifyDataSetChanged();

                } catch (JSONException e){
                    Log.e(TAG, "Hit json exception", e);
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.d(TAG, "onFailure");
            }
        });
        return suggestions;
    }
}
