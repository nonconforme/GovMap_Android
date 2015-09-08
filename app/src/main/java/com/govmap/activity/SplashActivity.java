package com.govmap.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;

import com.govmap.MainApplication;
import com.govmap.R;


public class SplashActivity extends BaseActivity {

    private SplashReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
    }

    @Override
    protected void onResume() {
        super.onResume();



        mReceiver = new SplashReceiver();
        IntentFilter intentFilter = new IntentFilter(MainApplication.ACTION_FINISH_SPLASH);
        registerReceiver(mReceiver, intentFilter);
//        ((MainApplication) getApplication()).loadGovSite();



    }

    @Override
    protected void onPause() {
        if (mReceiver != null)
            unregisterReceiver(mReceiver);
        super.onPause();
    }

    private class SplashReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (MainApplication.ACTION_FINISH_SPLASH.equals(intent.getAction())) {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }
        }
    }

}
