package com.example.onCreate.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import androidx.fragment.app.DialogFragment;

import com.example.onCreate.R;

import java.io.ByteArrayOutputStream;

public class PostShareDialog extends DialogFragment {

    private final String TAG = "ShareDialog";
    private View.OnClickListener mFacebookClick;
    private Dialog mDialog;

    public PostShareDialog() {};

    public void showDialog(Context context) {
        mDialog = new Dialog(context);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setCancelable(true);
        mDialog.setContentView(R.layout.dialog_share);

        ImageView ivFacebook = (ImageView) mDialog.findViewById(R.id.ivInstagram);

        ivFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String application = "com.instagram.android";

                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                BitmapDrawable g = (BitmapDrawable) ivFacebook.getDrawable();
                g.getBitmap().compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), g.getBitmap(), "Title", null);

                Intent storiesIntent = new Intent("com.instagram.share.ADD_TO_STORY");
                storiesIntent.setDataAndType(Uri.parse(path),  "image/*");
                storiesIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                storiesIntent.setPackage("com.instagram.android");
//                getActivity().grantUriPermission(
//                        "com.instagram.png.android", Uri.parse(path), Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(storiesIntent);
                hideDialog();
            }
        });

        mDialog.show();
    }



    public void hideDialog() {
        mDialog.cancel();
    }
}
