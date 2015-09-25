package com.govmap.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.govmap.MainApplication;
import com.govmap.R;
import com.govmap.view.GovWebView;

/**
 * Created by MediumMG on 01.09.2015.
 */
public abstract class BaseActivity extends AppCompatActivity {

    public static final String NO_RESULT_FOUND_HE = "לא נמצאו תוצאות מתאימות";

    protected GovWebView wvGov;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            restoreData(savedInstanceState);
        }
        super.onCreate(savedInstanceState);

        wvGov = ((MainApplication) getApplication()).getWebView();
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkConnection();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        GovWebView webView = ((MainApplication) getApplication()).getWebView();
        if (webView != null)
            webView.saveState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)
            onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    private void restoreData(Bundle data) {
        GovWebView webView = ((MainApplication) getApplication()).getWebView();
        if (webView == null) {
            webView = new GovWebView(getApplication());
        }
        webView.restoreState(data);
    }

    protected void checkConnection() {
        if (!isNetworkConnectionEnabled()) {
            new AlertDialog.Builder(BaseActivity.this)
                    .setMessage(R.string.message_no_internet_connection)
                    .setPositiveButton(R.string.text_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            checkConnection();
                        }
                    })
                    .setNegativeButton(R.string.text_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            BaseActivity.this.finish();
                        }
                    })
                    .setCancelable(false)
                    .create().show();
        }
    }

    protected boolean isNetworkConnectionEnabled() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    protected boolean isLocationServicesEnabled() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }




}
