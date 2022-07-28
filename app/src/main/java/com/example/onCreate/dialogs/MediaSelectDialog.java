package com.example.onCreate.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import androidx.fragment.app.DialogFragment;

import com.example.onCreate.R;

public class MediaSelectDialog extends DialogFragment {

    private View.OnClickListener mGalleryOnClick;
    private View.OnClickListener mCanvasOnClick;
    private Dialog mDialog;

    public MediaSelectDialog(View.OnClickListener g, View.OnClickListener c) {
        this.mGalleryOnClick = g;
        this.mCanvasOnClick = c;
    }

    public void showDialog(Activity activity) {
        mDialog = new Dialog(activity);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setCancelable(true);
        mDialog.setContentView(R.layout.dialog_media_select);

        Button btnGallery = (Button) mDialog.findViewById(R.id.btnGallery);
        Button btnCanvas = (Button) mDialog.findViewById(R.id.btnCanvas);

        btnGallery.setOnClickListener(mGalleryOnClick);
        btnCanvas.setOnClickListener(mCanvasOnClick);

        mDialog.show();
        mDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        mDialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    public void hideDialog() {
        mDialog.cancel();
    }
}
