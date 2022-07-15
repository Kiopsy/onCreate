package com.example.onCreate.utilities;

import android.graphics.Bitmap;

import com.example.onCreate.models.Idea;

public class PostBitmap {

    private Bitmap mBitmap;
    private Idea mIdeaToPost;

    public PostBitmap(Idea idea) {
        mIdeaToPost = idea;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }
}
