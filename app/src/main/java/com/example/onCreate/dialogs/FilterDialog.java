package com.example.onCreate.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import androidx.fragment.app.DialogFragment;

import com.example.onCreate.R;

public class FilterDialog extends DialogFragment {

    private View.OnClickListener mTopOnClick;
    private View.OnClickListener mStarredOnClick;
    private View.OnClickListener mRecentOnClick;
    private View.OnClickListener mOldestOnClick;
    private boolean mIsPrivate;
    private Dialog mDialog;

    public FilterDialog(boolean isPrivate) {
        this.mIsPrivate = isPrivate;
    }

    public void showDialog(Activity activity) {
        mDialog = new Dialog(activity);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setCancelable(true);
        mDialog.setContentView(R.layout.dialog_filter);

        Button btnTop = (Button) mDialog.findViewById(R.id.btnTop);
        Button btnStarred = (Button) mDialog.findViewById(R.id.btnStarred);
        Button btnRecent = (Button) mDialog.findViewById(R.id.btnRecent);
        Button btnOldest = (Button) mDialog.findViewById(R.id.btnOldest);

        // Change filter functionality based on global vs private feeds
        if (mIsPrivate) {
            btnStarred.setVisibility(View.VISIBLE);
            btnTop.setVisibility(View.GONE);
        } else {
            btnStarred.setVisibility(View.GONE);
            btnTop.setVisibility(View.VISIBLE);
        }

        btnTop.setOnClickListener(mTopOnClick);
        btnStarred.setOnClickListener(mStarredOnClick);
        btnRecent.setOnClickListener(mRecentOnClick);
        btnOldest.setOnClickListener(mOldestOnClick);

        mDialog.show();
    }

    public void hideDialog() {
        mDialog.cancel();
    }

    public void setTopOnClick(View.OnClickListener TopOnClick) {
        this.mTopOnClick = TopOnClick;
    }

    public void setStarredOnClick(View.OnClickListener StarredOnClick) {
        this.mStarredOnClick = StarredOnClick;
    }

    public void setRecentOnClick(View.OnClickListener RecentOnClick) {
        this.mRecentOnClick = RecentOnClick;
    }

    public void setOldestOnClick(View.OnClickListener OldestOnClick) {
        this.mOldestOnClick = OldestOnClick;
    }
}
