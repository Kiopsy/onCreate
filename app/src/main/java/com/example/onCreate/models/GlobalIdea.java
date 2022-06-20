package com.example.onCreate.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("GlobalIdea")
public class GlobalIdea extends ParseObject {

    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_USER = "user";

    public String getDescription() {
        return getString(KEY_DESCRIPTION);
    }

    public void setDescription(String description) {
        put(KEY_DESCRIPTION, description);
    }
}

