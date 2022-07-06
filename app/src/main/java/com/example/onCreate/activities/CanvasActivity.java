package com.example.onCreate.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;

import androidx.appcompat.app.ActionBar;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.example.onCreate.R;
import com.example.onCreate.utilities.DrawingView;
import com.example.onCreate.utilities.StrokeSelectorDialog;
import com.github.dhaval2404.colorpicker.MaterialColorPickerDialog;
import com.github.dhaval2404.colorpicker.listener.ColorListener;
import com.github.dhaval2404.colorpicker.model.ColorShape;
import com.github.dhaval2404.colorpicker.model.ColorSwatch;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

// Code used from: https://github.com/martinbaciga/android-drawing-canvas
public class CanvasActivity extends AppCompatActivity {
    private DrawingView mDrawingView;
    private ImageView mFillBackgroundImageView;
    private ImageView mColorImageView;
    private ImageView mStrokeImageView;
    private ImageView mUndoImageView;
    private ImageView mRedoImageView;
    private int mCurrentBackgroundColor;
    private int mCurrentColor;
    private int mCurrentStroke;
    private final static int MEDIA_SELECT_CODE = 2135;
    private static final int MAX_STROKE_WIDTH = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_canvas);

        mDrawingView = findViewById(R.id.main_drawing_view);
        mFillBackgroundImageView = findViewById(R.id.main_fill_iv);
        mColorImageView = findViewById(R.id.main_color_iv);
        mStrokeImageView = findViewById(R.id.main_stroke_iv);
        mUndoImageView = findViewById(R.id.main_undo_iv);
        mRedoImageView = findViewById(R.id.main_redo_iv);

        mFillBackgroundImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startFillBackgroundDialog();
            }
        });

        mColorImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startColorPickerDialog();
            }
        });

        mStrokeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startStrokeSelectorDialog();
            }
        });

        mUndoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawingView.undo();
            }
        });

        mRedoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawingView.redo();
            }
        });

        setActionBarIcon();
        initDrawingView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_canvas, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clear:
                new AlertDialog.Builder(this)
                        .setTitle("Clear canvas")
                        .setMessage("Are you sure you want to clear the canvas?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mDrawingView.clearCanvas();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                break;
            case R.id.action_send:
                Bitmap drawing = mDrawingView.getBitmap();

//                ByteArrayOutputStream bStream = new ByteArrayOutputStream();
//                drawing.compress(Bitmap.CompressFormat.PNG, 100, bStream);
//                byte[] byteArray = bStream.toByteArray();

                Intent data = new Intent();
//                data.putExtra("image", byteArray);
                data.setData(getImageUri(this, drawing));
                setResult(MEDIA_SELECT_CODE, data);
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
    private void initDrawingView() {
        mCurrentBackgroundColor = ContextCompat.getColor(this, android.R.color.white);
        mCurrentColor = ContextCompat.getColor(this, android.R.color.black);
        mCurrentStroke = 10;
        mDrawingView.setBackgroundColor(mCurrentBackgroundColor);
        mDrawingView.setPaintColor(mCurrentColor);
        mDrawingView.setPaintStrokeWidth(mCurrentStroke);
    }

    private void startFillBackgroundDialog() {
        new MaterialColorPickerDialog
                .Builder(this)
                .setTitle("Pick Theme")
                .setColorShape(ColorShape.SQAURE)
                .setColorSwatch(ColorSwatch._300)
                .setDefaultColor(R.color.black)
                .setColorListener(new ColorListener() {
                    @Override
                    public void onColorSelected(int color, @NotNull String colorHex) {
                        // Handle Color Selection
                        mCurrentBackgroundColor = color;
                        mDrawingView.setBackgroundColor(mCurrentBackgroundColor);
                    }
                })
                .show();
    }

    private void startColorPickerDialog() {
        new MaterialColorPickerDialog
                .Builder(this)
                .setTitle("Pick Theme")
                .setColorShape(ColorShape.SQAURE)
                .setColorSwatch(ColorSwatch._300)
                .setDefaultColor(R.color.black)
                .setColorListener(new ColorListener() {
                    @Override
                    public void onColorSelected(int color, @NotNull String colorHex) {
                        // Handle Color Selection
                        mCurrentColor = color;
                        mDrawingView.setPaintColor(mCurrentColor);
                    }
                })
                .show();
    }

    private void startStrokeSelectorDialog() {
        StrokeSelectorDialog dialog = StrokeSelectorDialog.newInstance(mCurrentStroke, MAX_STROKE_WIDTH);
        dialog.setOnStrokeSelectedListener(new StrokeSelectorDialog.OnStrokeSelectedListener() {
            @Override
            public void onStrokeSelected(int stroke) {
                mCurrentStroke = stroke;
                mDrawingView.setPaintStrokeWidth(mCurrentStroke);
            }
        });
        dialog.show(getSupportFragmentManager(), "StrokeSelectorDialog");
    }

    // Action bar and logo setup
    private void setActionBarIcon() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setIcon(R.drawable.logo);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
    }
    
//    private void requestPermissionsAndSaveBitmap()
//    {
//        if (PermissionManager.checkWriteStoragePermissions(this))
//        {
//            Uri uri = FileManager.saveBitmap(this, mDrawingView.getBitmap());
//            //startShareDialog(uri);
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
//    {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        switch (requestCode)
//        {
//            case PermissionManager.REQUEST_WRITE_STORAGE:
//            {
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
//                {
//                    Uri uri = FileManager.saveBitmap(this, mDrawingView.getBitmap());
//                    //startShareDialog(uri);
//                } else
//                {
//                    Toast.makeText(this, "The app was not allowed to write to your storage. Hence, it cannot function properly. Please consider granting it this permission", Toast.LENGTH_LONG).show();
//                }
//            }
//        }
//    }
}