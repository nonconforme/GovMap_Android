package com.govmap.view;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;

import com.govmap.R;

/**
 * Created by MediumMG on 14.09.2015.
 */
public class GovProgressDialog extends ProgressDialog {

    private Context mContext;

    public GovProgressDialog(Context context) {
        super(context);
        mContext = context;
    }

    public GovProgressDialog(Context context, int theme) {
        super(context, theme);
        mContext = context;
    }

    @Override
    public void show() {
        super.show();
        setContentView(R.layout.dialog_loading);
        setCancelable(false);
    }

    @Override
    public void onBackPressed() {
        dismiss();
        if (mContext instanceof Activity) {
            ((Activity) mContext).finish();
        }
    }
}
