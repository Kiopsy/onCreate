package com.example.onCreate.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
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
import android.widget.Toast;

import com.example.onCreate.R;
import com.google.android.material.textfield.TextInputLayout;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private ImageView mIvProfilePicture;
    private TextInputLayout mEtUsername;
    private TextInputLayout mEtPassword;
    private TextInputLayout mEtEmail;
    private TextInputLayout mEtName;
    private TextInputLayout mEtJobDescription;
    private TextInputLayout mEtGeneralDescription;
    private Button mBtnProfPic;
    private Button mBtnSignup;
    private Bitmap selectedImage;
    public final static int mPICK_PHOTO_CODE = 1046;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mIvProfilePicture = findViewById(R.id.ivProfilePicture);
        mEtUsername = findViewById(R.id.etUsername);
        mEtPassword = findViewById(R.id.etPassword);
        mEtEmail = findViewById(R.id.etEmail);
        mEtName = findViewById(R.id.etName);
        mEtJobDescription = findViewById(R.id.etJobDescription);
        mEtGeneralDescription = findViewById(R.id.etGeneralDescription);
        mBtnProfPic = findViewById(R.id.btnProfImage);
        mBtnSignup = findViewById(R.id.btnSignup);

        // Set profile picture onClick listener
        mBtnProfPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPickPhoto(v);
            }
        });

        // Set signup button onClick Listener
        mBtnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick signup button");
                String username = mEtUsername.getEditText().getText().toString();
                String password = mEtPassword.getEditText().getText().toString();
                String email = mEtEmail.getEditText().getText().toString();
                String name = mEtName.getEditText().getText().toString();
                String jobDescription = mEtJobDescription.getEditText().getText().toString();
                String generalDescription = mEtGeneralDescription.getEditText().getText().toString();
                signupUser(username, password, email, name, jobDescription, generalDescription);
            }
        });

        // Set up logo in action bar
        setActionBarIcon();
    }

    // Create a new Parse User
    private void signupUser(String username, String password, String email, String name, String jobDescription, String generalDescription) {
        Log.i(TAG, "Attempting to create user: " + username);

        // Create the ParseUser & set core properties
        ParseUser user = new ParseUser();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.put("name", name);
        user.put("jobDescription", jobDescription);
        user.put("generalDescription", generalDescription);
        ParseFile photoFile = bitmapToParseFile(selectedImage);
        // Ensure profile photo is saved before signing up
        photoFile.saveInBackground(new SaveCallback() {
            public void done(ParseException e) {
                // Check if signup was successful
                if (e != null) {
                    Log.e(TAG, "Issue saving profile picture", e);
                    Toast.makeText(RegisterActivity.this, "Issue with profile photo!", Toast.LENGTH_SHORT).show();
                    return;
                }
                user.put("profileImage", photoFile);
                // finish signing up user
                signupUser(user);
                Toast.makeText(RegisterActivity.this, "success", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Finish signing up Parse User
    private void signupUser(ParseUser user) {
        Log.i(TAG, "Attempting to signup user: " + user.getUsername());

        // Invoke signUpInBackground
        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                // Check if signup was successful
                if (e != null) {
                    Log.e(TAG, "Issue with signing up an account", e);
                    Toast.makeText(RegisterActivity.this, "Issue with signup!", Toast.LENGTH_SHORT).show();
                    return;
                }
                goMainActivity();
                Toast.makeText(RegisterActivity.this, "success", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Trigger gallery selection for a photo
    public void onPickPhoto(View view) {
        // Create intent for picking a photo from the gallery
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, mPICK_PHOTO_CODE);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((data != null) && requestCode == mPICK_PHOTO_CODE) {
            Uri photoUri = data.getData();

            // Load the image located at photoUri into selectedImage
            selectedImage = loadFromUri(photoUri);

            // Load the selected image into a preview
            mIvProfilePicture.setImageBitmap(selectedImage);
        }
    }

    // Intent to go to the homepage
    private void goMainActivity() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }

    // Setting up action bar & onCreate logo
    private void setActionBarIcon() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setIcon(R.drawable.logo);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
    }
}