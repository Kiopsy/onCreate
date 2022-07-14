package com.example.onCreate.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
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
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import java.util.Arrays;

public class ShareDialog extends DialogFragment {

    private final String TAG = "ShareDialog";
    private View.OnClickListener mFacebookClick;
    private Dialog mDialog;
    private CallbackManager mCallbackManager;
    private LoginButton mLoginButton;

    public ShareDialog(View.OnClickListener f) {
        this.mFacebookClick = f;
    }

    public void showDialog(Context context) {
        mDialog = new Dialog(context);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setCancelable(true);
        mDialog.setContentView(R.layout.dialog_share);

        ImageView ivFacebook = (ImageView) mDialog.findViewById(R.id.ivFacebook);

        mLoginButton = mDialog.findViewById(R.id.login_button);
        mCallbackManager = CallbackManager.Factory.create();

        //mLoginButton.setPermissions(Arrays.asList("user"));

        ivFacebook.setOnClickListener(new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(@NonNull FacebookException e) {
                Log.e(TAG, "Login error" + e);
            }
        });

        mDialog.show();
    }

    public void hideDialog() {
        mDialog.cancel();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }
}
