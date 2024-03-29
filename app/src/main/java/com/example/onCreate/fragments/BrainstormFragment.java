package com.example.onCreate.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.onCreate.R;
import com.example.onCreate.activities.CanvasActivity;
import com.example.onCreate.activities.MainActivity;
import com.example.onCreate.dialogs.MediaSelectDialog;
import com.example.onCreate.dialogs.TagDialog;
import com.example.onCreate.models.Idea;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;


public class BrainstormFragment extends Fragment {

    private EditText mEtTitle;
    private EditText mEtDescription;
    private Button mBtnSubmit;
    private ConstraintLayout mScreenLayout;
    private LinearLayout mMediaLayout;
    private LinearLayout mTagButtonLayout;
    private LinearLayout mTagDisplayLayout;
    private ImageView mIvPostImage;
    private RadioGroup mSwitchPrivateGlobal;
    private Bitmap mSelectedImage;
    private MediaSelectDialog mMediaDialog;
    private TagDialog mTagDialog;
    private CardView mImageCard;
    private final static int PICK_PHOTO_CODE = 1046;
    private final static int CANVAS_CODE = 1253;
    private final static int MAX_DESCRIPTION_LENGTH = 280;
    private final static int MAX_TITLE_LENGTH = 35;
    private final static String TAG = "Brainstorming Fragment";

    public BrainstormFragment() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_brainstorm_new, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mEtTitle = view.findViewById(R.id.etTitle);
        mEtDescription = view.findViewById(R.id.etDescription);
        mBtnSubmit = view.findViewById(R.id.btnSubmit);
        mMediaLayout = view.findViewById(R.id.mediaLayout);
        mTagButtonLayout = view.findViewById(R.id.tagLayout);
        mTagDisplayLayout = view.findViewById(R.id.layoutTagDisplay);
        mIvPostImage = view.findViewById(R.id.ivPostImage);
        mSwitchPrivateGlobal = view.findViewById(R.id.switchPrivateGlobal);
        mScreenLayout = view.findViewById(R.id.ConstraintLayout);
        mImageCard = view.findViewById(R.id.cardImagePost);

        // Button to choose an image from the phone gallery
        mMediaLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMediaDialog = new MediaSelectDialog(galleryBtnOnClick(), canvasBtnOnClick());
                mMediaDialog.showDialog(getActivity());
            }
        });

        // Button to add tags
        mTagButtonLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTagDialog = new TagDialog(mTagDisplayLayout);
                mTagDialog.showDialog(getActivity());
            }
        });

        // Button to submit the idea post using input data
        mBtnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Description input and variants
                String description = mEtDescription.getText().toString();
                if (description.isEmpty()) {
                    Toast.makeText(getContext(), "Description cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (description.length() > MAX_DESCRIPTION_LENGTH) {
                    Toast.makeText(getContext(), "Sorry, your description is too long", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Title input and variants
                String title = mEtTitle.getText().toString();
                if (title.isEmpty()) {
                    Toast.makeText(getContext(), "title cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (title.length() > MAX_TITLE_LENGTH) {
                    Toast.makeText(getContext(), "Sorry, your title is too long", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Differentiating between private and global posts using Radio group/buttons
                int index = mSwitchPrivateGlobal.indexOfChild(view.findViewById(mSwitchPrivateGlobal.getCheckedRadioButtonId()));

                // Feed selection: index == 0 -> private ; index == 1 -> global
                boolean isPrivate = index == 0 ? true : false;

                ParseUser currentUser = ParseUser.getCurrentUser();

                // Publishes input data to database
                savePost(description, title, currentUser, mSelectedImage, isPrivate);
                // Goes to home screen after submission
                goMainActivity();
            }
        });
    }

    // Function to create and update an idea model, and post to Parse
    private void savePost(String description, String title, ParseUser currentUser, Bitmap photoFile, boolean isPrivate) {
        Idea idea = new Idea();
        idea.setDescription(description);
        idea.setUser(currentUser);
        idea.setVisibility(true);
        idea.setTitle(title);
        idea.setVisibility(isPrivate);

        // Setting starting upvotes if global
        if (!isPrivate) {
            // A user starts by initially upvoting their own post
            idea.add(Idea.KEY_ARRAY_UPVOTE, currentUser);
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
                    Log.e(TAG, "error while saving", e);
                    Toast.makeText(getContext(), "error while saving", Toast.LENGTH_SHORT).show();
                }
                Log.i(TAG, "Post save was successful");
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

    // Loads a bitmap from a uri
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

    // Setting dialog button click listeners for easy onActivityResult access
    public View.OnClickListener galleryBtnOnClick() {
        View.OnClickListener onClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMediaDialog.hideDialog();
                onPickPhoto(v);
            }
        };
        return onClick;
    }

    public View.OnClickListener canvasBtnOnClick() {
        View.OnClickListener onClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMediaDialog.hideDialog();
                goCanvasActivity();
            }
        };
        return onClick;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((data != null) && requestCode == PICK_PHOTO_CODE) {
            // Make the card view visible
            mImageCard.setVisibility(View.VISIBLE);

            Uri photoUri = data.getData();

            // Load the image located at photoUri into selectedImage
            mSelectedImage = loadFromUri(photoUri);

            // Load the selected image into a preview
            mIvPostImage.setImageBitmap(mSelectedImage);
        } else if ((data != null) && requestCode == CANVAS_CODE) {
            // Make the card view visible
            mImageCard.setVisibility(View.VISIBLE);

            byte[] byteArray = data.getByteArrayExtra("image");

            // Get a bitmap from the media's byteArray
            mSelectedImage = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

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

    // Intent to go canvas drawing
    private void goCanvasActivity() {
        Intent i = new Intent(getContext(), CanvasActivity.class);
        startActivityForResult(i, CANVAS_CODE);
    }
}