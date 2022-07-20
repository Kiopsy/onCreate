package com.example.onCreate.models;

import android.annotation.SuppressLint;
import android.os.Parcel;

import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;

@SuppressLint("ParcelCreator")
public class StringSuggestion implements SearchSuggestion {

    private String mString;

    public StringSuggestion(String suggestion) {
        this.mString = suggestion;
    }

    public StringSuggestion(Parcel source) {
        this.mString = source.readString();
    }

    @Override
    public String getBody() {
        return mString;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mString);
    }
}

