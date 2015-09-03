package com.govmap.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.govmap.GovWebView;
import com.govmap.MainApplication;
import com.govmap.R;

/**
 * Created by MediumMG on 01.09.2015.
 */
public class GeoNumberActivity extends BaseActivity implements View.OnClickListener, TextView.OnEditorActionListener {

    private static final int TIME_FIND = 500;
    private static final int TIME_INNERTEXT = 200;
    private static final int MAX_ATTEMPTS = 20;

    private EditText etGeoNum1, etGeoNum2;
    private Button btnSearch;

    private GovWebView wvGov;

    private int attemptCount = 0;
    private Handler mHandler = new Handler();
    private Runnable mRunnable = new MyRunnable();
    private GeoNumberReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geonumber);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        etGeoNum1 = (EditText) findViewById(R.id.etGeoNum1_AGN);
        etGeoNum2 = (EditText) findViewById(R.id.etGeoNum2_AGN);
        btnSearch = (Button) findViewById(R.id.btnSearch_AGN);

        wvGov = ((MainApplication) getApplication()).getWebView();

        etGeoNum2.setOnEditorActionListener(GeoNumberActivity.this);
        btnSearch.setOnClickListener(GeoNumberActivity.this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mReceiver = new GeoNumberReceiver();
        IntentFilter intentFilter = new IntentFilter(MainApplication.ACTION_INNER_ADDRESS);
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        mHandler.removeCallbacks(mRunnable);
        attemptCount = 0;
        if (mReceiver != null)
            unregisterReceiver(mReceiver);
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        if (checkData())
            callRequest();
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (checkData())
            callRequest();
        return true;
    }

    private void goToMap() {
        Intent intent = new Intent(GeoNumberActivity.this, MapActivity.class);
        startActivity(intent);
        finish();
    }

    private boolean checkData() {
        if (TextUtils.isEmpty(etGeoNum1.getText())) {
            etGeoNum1.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(etGeoNum2.getText())) {
            etGeoNum2.requestFocus();
            return false;
        }
        return true;
    }

    private void callRequest() {
        String geonum1 = String.valueOf(etGeoNum1.getText());
        String geonum2 = String.valueOf(etGeoNum2.getText());

        String cadastralString = String.format(getString(R.string.req_for_nubmer_format), geonum1, geonum2);
        Log.v(MainApplication.TAG, cadastralString);
        wvGov.loadUrl(String.format("javascript:(function() {document.getElementById('tbSearchWord').value = '%s';})();", cadastralString));
        wvGov.loadUrl("javascript:(function() {FS_Search();})();");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                wvGov.loadUrl("javascript:(function() {FSS_FindAddressForBlock();})();");
                mHandler.postDelayed(mRunnable, TIME_INNERTEXT);
            }
        }, TIME_FIND);

    }

    private class MyRunnable implements Runnable {
        @Override
        public void run() {
            ((MainApplication) getApplication()).checkInnerTextForAddress();
            attemptCount++;
            if (attemptCount < MAX_ATTEMPTS)
                mHandler.postDelayed(mRunnable, TIME_INNERTEXT);
        }
    }

    private class GeoNumberReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (MainApplication.ACTION_INNER_ADDRESS.equals(intent.getAction())) {
                ((MainApplication) getApplication()).clearResults();
                mHandler.removeCallbacks(mRunnable);
                attemptCount = 0;

                Log.v(MainApplication.TAG, "data: '"+ intent.getStringExtra("data")+"'");
                // TODO check

                goToMap();
            }
        }
    }

}
