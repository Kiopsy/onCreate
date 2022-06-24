package com.example.onCreate.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.example.onCreate.R;
import com.example.onCreate.activities.MainActivity;
import com.example.onCreate.models.Idea;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;


public class Brainstorm extends Fragment {

    private EditText etTitle;
    private EditText etDescription;
    private Button btnSubmit;
    private ImageView ivMedia;
    private ImageView ivPostImage;
    private Switch switchPrivateGlobal;
    private Bitmap selectedImage;
    public final static int PICK_PHOTO_CODE = 1046;
    public final static String TAG = "Brainstorming Fragment";

    public Brainstorm () {
        // Required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_brainstorm, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etTitle = view.findViewById(R.id.etTitle);
        etDescription = view.findViewById(R.id.etDescription);
        btnSubmit = view.findViewById(R.id.btnSubmit);
        ivMedia = view.findViewById(R.id.ivMedia);
        ivPostImage = view.findViewById(R.id.ivPostImage);

        ivMedia.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onPickPhoto(v);
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String description = etDescription.getText().toString();
                String title = etTitle.getText().toString();
                if (description.isEmpty()) {
                    Toast.makeText(getContext(), "Description cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (title.isEmpty()) {
                    Toast.makeText(getContext(), "title cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                ParseUser currentUser = ParseUser.getCurrentUser();
                savePost(description, title, currentUser, selectedImage);
                goMainActivity();
            }
        });
    }

    private void savePost(String description, String title, ParseUser currentUser, Bitmap photoFile) {
        Idea idea = new Idea();
        idea.setDescription(description);
        idea.setUser(currentUser);
        idea.setTitle(title);
        if (photoFile != null) {
            idea.setImage(bitmapToParseFile(photoFile));
        }
        idea.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "error while saving", e);
                    Toast.makeText(getContext(), "error while saving", Toast.LENGTH_SHORT).show();
                }
                Log.i(TAG, "Post save was successful");
                etDescription.setText("");
                ivPostImage.setImageResource(0);
            }
        });
    }

    // Trigger gallery selection for a photo
    public void onPickPhoto(View view) {
        // Create intent for picking a photo from the gallery
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_PHOTO_CODE);
    }

    // converts a bitmap to a ParseFile
    public ParseFile bitmapToParseFile(Bitmap image) {
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG,100,byteArrayOutputStream);
        byte[] imageByte = byteArrayOutputStream.toByteArray();
        ParseFile parseFile = new ParseFile("image_file.png",imageByte);
        return parseFile;
    }

    public Bitmap loadFromUri(Uri photoUri) {
        Bitmap image = null;
        try {
            // check version of Android on device
            if(Build.VERSION.SDK_INT > 27){
                // on newer versions of Android, use the new decodeBitmap method
                ImageDecoder.Source source = ImageDecoder.createSource(getActivity().getContentResolver(), photoUri);
                image = ImageDecoder.decodeBitmap(source);
            } else {
                // support older versions of Android by using getBitmap
                image = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), photoUri);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((data != null) && requestCode == PICK_PHOTO_CODE) {
            Uri photoUri = data.getData();

            // Load the image located at photoUri into selectedImage
            selectedImage = loadFromUri(photoUri);

            // Load the selected image into a preview
            ivPostImage.setImageBitmap(selectedImage);
        }
    }

    // Intent to go to the homepage
    private void goMainActivity() {
        Intent i = new Intent(getContext(), MainActivity.class);
        startActivity(i);
        getActivity().finish();
    }
}