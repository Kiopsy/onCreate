package com.example.onCreate.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.onCreate.R;
import com.example.onCreate.dialogs.MediaSelectDialog;
import com.example.onCreate.dialogs.TagDialog;
import com.example.onCreate.models.Idea;
import com.example.onCreate.utilities.CustomEditText;
import com.github.angads25.toggle.widget.LabeledSwitch;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class BrainstormActivity extends AppCompatActivity {

    private EditText mEtTitle;
    private EditText mEtDescription;
    private Button mBtnSubmit;
    private ConstraintLayout mScreenLayout;
    private LinearLayout mMediaLayout;
    private LinearLayout mTagButtonLayout;
    private LinearLayout mTagDisplayLayout;
    private ImageView mIvPostImage;
    private LabeledSwitch mSwitchPrivateGlobal;
    private Bitmap mSelectedImage;
    private MediaSelectDialog mMediaDialog;
    private TagDialog mTagDialog;
    private CardView mImageCard;
    private ImageView mClose;
    private final static int PICK_PHOTO_CODE = 1046;
    private final static int CANVAS_CODE = 1253;
    private final static int MAX_DESCRIPTION_LENGTH = 280;
    private final static int MAX_TITLE_LENGTH = 35;
    private final static String TAG = "Brainstorming Fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_brainstorm_new);

        mEtTitle = findViewById(R.id.title);
        mEtDescription = findViewById(R.id.description);
        mBtnSubmit = findViewById(R.id.btnSubmit);
        mMediaLayout = findViewById(R.id.mediaLayout);
        mTagButtonLayout = findViewById(R.id.tagLayout);
        mTagDisplayLayout = findViewById(R.id.layoutTagDisplay);
        mIvPostImage = findViewById(R.id.ivPostImage);
        mSwitchPrivateGlobal = findViewById(R.id.visibilitySwitch);
        mScreenLayout = findViewById(R.id.ConstraintLayout);
        mImageCard = findViewById(R.id.cardImagePost);
        mClose = findViewById(R.id.ivClose);

        // Button to choose an image from the phone gallery
        mMediaLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMediaDialog = new MediaSelectDialog(galleryBtnOnClick(), canvasBtnOnClick());
                mMediaDialog.showDialog(BrainstormActivity.this);
            }
        });

        // Button to add tags
        mTagButtonLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTagDialog = new TagDialog(mTagDisplayLayout);
                mTagDialog.showDialog(BrainstormActivity.this);
            }
        });

        // Button to submit the idea post using input data
        mBtnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Description input and variants
                String description = mEtDescription.getText().toString();
                if (description.isEmpty()) {
                    Toast.makeText(BrainstormActivity.this, "Description cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (description.length() > MAX_DESCRIPTION_LENGTH) {
                    Toast.makeText(BrainstormActivity.this, "Sorry, your description is too long", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Title input and variants
                String title = mEtTitle.getText().toString();
                if (title.isEmpty()) {
                    Toast.makeText(BrainstormActivity.this, "title cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (title.length() > MAX_TITLE_LENGTH) {
                    Toast.makeText(BrainstormActivity.this, "Sorry, your title is too long", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Differentiating between private and global posts using switches
                boolean isPrivate = mSwitchPrivateGlobal.isOn();

                ParseUser currentUser = ParseUser.getCurrentUser();

                // Get the string tags
                ArrayList<String> tags = mTagDialog == null ? new ArrayList<>() : mTagDialog.getTags();

                // Publishes input data to database
                savePost(description, title, currentUser, mSelectedImage, isPrivate, tags);
                // Goes to home screen after submission
                goMainActivity();
            }
        });

        // Button to close the post activity
        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        setActionBarIcon();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom);
    }

    // Function to create and update an idea model, and post to Parse
    private void savePost(String description, String title, ParseUser currentUser, Bitmap photoFile, boolean isPrivate, ArrayList<String> tags) {
        Idea idea = new Idea();
        idea.setDescription(description);
        idea.setUser(currentUser);
        idea.setVisibility(true);
        idea.setTitle(title);
        idea.setVisibility(isPrivate);
        idea.setTags(tags);

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
                    Toast.makeText(BrainstormActivity.this, "error while saving", Toast.LENGTH_SHORT).show();
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
                ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(), photoUri);
                image = ImageDecoder.decodeBitmap(source);
            } else {
                // support older versions of Android by using getBitmap
                image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
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
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }

    // Intent to go canvas drawing
    private void goCanvasActivity() {
        Intent i = new Intent(this, CanvasActivity.class);
        startActivityForResult(i, CANVAS_CODE);
    }

    // Action bar for the screen that shows onCreate logo
    private void setActionBarIcon() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }
}