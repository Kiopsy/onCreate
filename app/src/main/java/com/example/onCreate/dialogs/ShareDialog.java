package com.example.onCreate.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

import androidx.fragment.app.DialogFragment;

import com.example.onCreate.R;

public class ShareDialog extends DialogFragment {

    private View.OnClickListener mFacebookClick;
    private Dialog mDialog;

    public ShareDialog(View.OnClickListener f) {
        this.mFacebookClick = f;
    }

    public void showDialog(Context context) {
        mDialog = new Dialog(context);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setCancelable(true);
        mDialog.setContentView(R.layout.dialog_share);

        ImageView ivFacebook = (ImageView) mDialog.findViewById(R.id.ivFacebook);
        //ivFacebook.setOnClickListener(mFacebookClick);

        mDialog.show();
    }

    public void hideDialog() {
        mDialog.cancel();
    }
}
