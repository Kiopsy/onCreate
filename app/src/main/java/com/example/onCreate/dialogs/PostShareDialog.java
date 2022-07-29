package com.example.onCreate.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.PixelCopy;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;

import com.example.onCreate.R;

import java.io.ByteArrayOutputStream;

import javax.security.auth.callback.Callback;

public class PostShareDialog extends DialogFragment {

    private static final String TAG = "ShareDialog";
    private Dialog mDialog;

    // Dialog Constructor
    public PostShareDialog() {};

    public void showDialog(Context context) {
        mDialog = new Dialog(context);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setCancelable(true);
        mDialog.setContentView(R.layout.dialog_share);

        // Instagram share button & onClickListener
        ImageView ivInstagram = (ImageView) mDialog.findViewById(R.id.ivInstagram);
        ivInstagram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity activity = (Activity) context;
                View view = activity.findViewById(android.R.id.content).getRootView();

                // Get Bitmap from post
                Bitmap ideaPost = getBitmapFromView(view, activity);

                // Get Url from Bitmap
                String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), ideaPost, "Title", null);

                // Start Instagram Story Intent
                Intent storiesIntent = new Intent("com.instagram.share.ADD_TO_STORY");
                storiesIntent.setDataAndType(Uri.parse(path),  "image/*");
                storiesIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                storiesIntent.setPackage("com.instagram.android");
                context.startActivity(storiesIntent);

                // finish the dialog
                mDialog.cancel();
            }
        });

        mDialog.show();
    }

    // Uses the PixelCopyApi to programmatically take a screenshot of the phone & post
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static Bitmap getBitmapFromView(View view, Activity activity) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);

        int[] locations = new int[2];
        view.getLocationInWindow(locations);
        Rect rect = new Rect(locations[0], locations[1], locations[0] + view.getWidth(), locations[1] + view.getHeight());

        PixelCopy.request(activity.getWindow(), rect, bitmap, copyResult -> {
            if (copyResult == PixelCopy.SUCCESS) {
                Log.i(TAG, "PixelCopy Success");
            } else {
                Log.e(TAG, "PixelCopy Failure");
            }
        }, new Handler(Looper.getMainLooper()));

        return bitmap;
    }
}
