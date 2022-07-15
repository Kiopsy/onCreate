package com.example.onCreate.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.onCreate.R;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.share.model.ShareContent;
import com.facebook.share.model.ShareMediaContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.widget.ShareDialog;

import java.util.Arrays;

public class PostShareDialog extends DialogFragment {

    private final String TAG = "ShareDialog";
    private View.OnClickListener mFacebookClick;
    private Dialog mDialog;
    private CallbackManager mCallbackManager;
    private LoginButton mLoginButton;
    private ShareDialog mShareFacebookDialog;

    public PostShareDialog(View.OnClickListener f) {
        this.mFacebookClick = f;
    }

    public void showDialog(Context context) {
        mDialog = new Dialog(context);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setCancelable(true);
        mDialog.setContentView(R.layout.dialog_share);

        ImageView ivFacebook = (ImageView) mDialog.findViewById(R.id.ivFacebook);
        BitmapDrawable drawable = (BitmapDrawable) ivFacebook.getDrawable();
        Bitmap bitmap = drawable.getBitmap();

        Activity activity = (Activity) context;

        mCallbackManager = CallbackManager.Factory.create();
        mLoginButton = mDialog.findViewById(R.id.login_button);

        mLoginButton.setPermissions(Arrays.asList("public_profile"));
        mLoginButton.registerCallback(mCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.d(TAG, "Login Success");
                    }

                    @Override
                    public void onCancel() {
                        Log.d(TAG, "Login Cancel");
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Log.d(TAG, "Login Error" + exception);
                    }
                });

        mDialog.show();
    }

    public void hideDialog() {
        mDialog.cancel();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }
//
//        SharePhoto post = new SharePhoto.Builder()
//                .setBitmap(bitmap)
//                .build();
//
//        ShareContent shareContent = new ShareMediaContent.Builder()
//                .addMedium(post)
//                .build();
//        hideDialog();
//        mShareFacebookDialog = new ShareDialog(activity);
//        mShareFacebookDialog.show(shareContent, ShareDialog.Mode.AUTOMATIC);
}
