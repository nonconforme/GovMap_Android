package com.govmap;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by MediumMG on 01.09.2015.
 */
public abstract class BaseActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            restoreLocale(savedInstanceState);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    private void restoreLocale(Bundle data) {

    }

}
