package com.example.onCreate.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;

import com.example.onCreate.R;
import com.parse.ParseFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class MediaSelectActivity extends AppCompatActivity {

    private Button mBtnGallery;
    private Button mBtnCanvas;
    private ArrayList<ParseFile> mMediaList;
    private static final String TAG = "MediaSelectActivity";
    private final static int PICK_PHOTO_CODE = 1046;
    private final static int CANVAS_CODE = 1253;
    private final static int MEDIA_SELECT_CODE = 2135;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_select);

        mBtnGallery = findViewById(R.id.btnGallery);
        mBtnCanvas = findViewById(R.id.btnCanvas);

        mBtnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPickPhoto(v);
            }
        });

        mBtnCanvas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goCanvasActivity();
            }
        });
        setActionBarIcon();
    }

    // Trigger gallery selection for a photo
    private void onPickPhoto(View view) {
        // Create intent for picking a photo from the gallery
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_PHOTO_CODE);
    }

    // Loads a bitmap from a photo's URI
    private Bitmap loadFromUri(Uri photoUri) {
        Bitmap image = null;
        try {
            // check version of Android on device
            if(Build.VERSION.SDK_INT > 27){
                // on newer versions of Android, use the new decodeBitmap method
                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), photoUri);
                image = ImageDecoder.decodeBitmap(source);
            } else {
                // support older versions of Android by using getBitmap
                image = MediaStore.Images.Media.getBitmap(getContentResolver(), photoUri);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    private byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream bStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bStream);
        return bStream.toByteArray();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((data != null) && requestCode == PICK_PHOTO_CODE) {
            // Converts a photo's URI to Bitmap for standardization
            byte[] byteArray = bitmapToByteArray(loadFromUri(data.getData()));
            data.putExtra("image", byteArray);

            // Send photo byteArray back to BrainstormFragment
            setResult(MEDIA_SELECT_CODE, data);
            finish();
        } else if ((data != null) && requestCode == CANVAS_CODE) {
            // Send drawing byteArray back to BrainstormFragment
            setResult(MEDIA_SELECT_CODE, data);
            finish();
        }
    }

    // Intent to go sign up
    private void goCanvasActivity() {
        Intent i = new Intent(this, CanvasActivity.class);
        startActivityForResult(i, CANVAS_CODE);
    }

    // Action bar for the screen that shows onCreate logo
    private void setActionBarIcon() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setIcon(R.drawable.logo);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
    }
}