package com.example.onCreate.models;


import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("PrivateIdea")
public class PrivateIdea extends ParseObject {

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