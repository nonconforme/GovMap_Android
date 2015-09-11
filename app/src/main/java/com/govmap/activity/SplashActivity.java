package com.govmap.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ProgressBar;

import com.govmap.MainApplication;
import com.govmap.R;


public class SplashActivity extends BaseActivity {

    private SplashReceiver mReceiver;
    private ProgressBar pbProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        findViewById(R.id.splash_logo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }
        });

        pbProgress = (ProgressBar) findViewById(R.id.splash_progress);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (mReceiver != null)
            unregisterReceiver(mReceiver);
        super.onPause();
    }

    @Override
    protected void checkConnection() {
        if (!isNetworkConnectionEnabled()) {
            new AlertDialog.Builder(SplashActivity.this)
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
                            SplashActivity.this.finish();
                        }
                    })
                    .setCancelable(false)
                    .create().show();
        }
        else {
            pbProgress.setProgress(0);

            mReceiver = new SplashReceiver();
            IntentFilter intentFilter = new IntentFilter(MainApplication.ACTION_FINISH_SPLASH);
            intentFilter.addAction(MainApplication.ACTION_LOAD_PROGRESS);
            registerReceiver(mReceiver, intentFilter);
            ((MainApplication) getApplication()).loadGovSite();
        }
    }

    private class SplashReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (MainApplication.ACTION_FINISH_SPLASH.equals(intent.getAction())) {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }
            else
            if (MainApplication.ACTION_LOAD_PROGRESS.equals(intent.getAction())) {
                pbProgress.setProgress(intent.getIntExtra(MainApplication.EXTRA_DATA_LAOD_PROGRESS, 0));
            }
        }
    }

}
