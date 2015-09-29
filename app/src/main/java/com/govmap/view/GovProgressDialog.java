package com.govmap.view;

import android.app.ProgressDialog;
import android.content.Context;

import com.govmap.R;

/**
 * Created by MediumMG on 14.09.2015.
 */
public class GovProgressDialog extends ProgressDialog {

    public GovProgressDialog(Context context) {
        super(context);
    }


    public GovProgressDialog(Context context, int theme) {
        super(context, theme);
    }

    @Override
    public void show() {
        super.show();
        setContentView(R.layout.dialog_loading);
        setCancelable(false);
    }
}
