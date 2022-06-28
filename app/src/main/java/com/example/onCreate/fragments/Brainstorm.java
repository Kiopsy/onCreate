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
import android.widget.RadioGroup;
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

    private EditText mEtTitle;
    private EditText mEtDescription;
    private Button mBtnSubmit;
    private ImageView mIvMedia;
    private ImageView mIvPostImage;
    private RadioGroup mSwitchPrivateGlobal;
    private Bitmap mSelectedImage;
    public final static int PICK_PHOTO_CODE = 1046;
    public final static String mTAG = "Brainstorming Fragment";

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

        mEtTitle = view.findViewById(R.id.etTitle);
        mEtDescription = view.findViewById(R.id.etDescription);
        mBtnSubmit = view.findViewById(R.id.btnSubmit);
        mIvMedia = view.findViewById(R.id.ivMedia);
        mIvPostImage = view.findViewById(R.id.ivPostImage);
        mSwitchPrivateGlobal = view.findViewById(R.id.switchPrivateGlobal);

        mIvMedia.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onPickPhoto(v);
            }
        });

        mBtnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Getting text from description & title
                String description = mEtDescription.getText().toString();
                String title = mEtTitle.getText().toString();
                if (description.isEmpty()) {
                    Toast.makeText(getContext(), "Description cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (title.isEmpty()) {
                    Toast.makeText(getContext(), "title cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Differentiating between private and global posts using Radio group/buttons
                int index = mSwitchPrivateGlobal.indexOfChild(view.findViewById(mSwitchPrivateGlobal.getCheckedRadioButtonId()));

                // index == 0 : private
                // index == 1 : global
                boolean isPrivate = index == 0 ? true : false;

                ParseUser currentUser = ParseUser.getCurrentUser();
                savePost(description, title, currentUser, mSelectedImage, isPrivate);
                goMainActivity();
            }
        });
    }

    private void savePost(String description, String title, ParseUser currentUser, Bitmap photoFile, boolean isPrivate) {
        Idea idea = new Idea();
        idea.setDescription(description);
        idea.setUser(currentUser);
        idea.setVisibility(true);
        idea.setTitle(title);
        idea.setVisibility(isPrivate);
        // Setting upvotes if global
        if (!isPrivate) {
            // A user starts by initially upvoting their own post
            idea.add("upvoteUsers", currentUser);
            idea.setUpvotes(1);
            idea.setDownvotes(0);
        }

        if (photoFile != null) {
            idea.setImage(bitmapToParseFile(photoFile));
        }
        idea.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(mTAG, "error while saving", e);
                    Toast.makeText(getContext(), "error while saving", Toast.LENGTH_SHORT).show();
                }
                Log.i(mTAG, "Post save was successful");
                mEtDescription.setText("");
                mIvPostImage.setImageResource(0);
            }
        });
    }

    // Trigger gallery selection for a photo
    private void onPickPhoto(View view) {
        // Create intent for picking a photo from the gallery
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_PHOTO_CODE);
    }

    // converts a bitmap to a ParseFile
    private ParseFile bitmapToParseFile(Bitmap image) {
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG,100,byteArrayOutputStream);
        byte[] imageByte = byteArrayOutputStream.toByteArray();
        ParseFile parseFile = new ParseFile("image_file.png",imageByte);
        return parseFile;
    }

    private Bitmap loadFromUri(Uri photoUri) {
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
            mSelectedImage = loadFromUri(photoUri);

            // Load the selected image into a preview
            mIvPostImage.setImageBitmap(mSelectedImage);
        }
    }

    // Intent to go to the homepage
    private void goMainActivity() {
        Intent i = new Intent(getContext(), MainActivity.class);
        startActivity(i);
        getActivity().finish();
    }
}