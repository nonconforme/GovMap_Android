package com.govmap;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;


public class SplashActivity extends BaseActivity {

    private static final int DELAY_MILLISECONDS = 2000;

    private Handler mHandler = new Handler();
    private Runnable mRunnable = new NextActivityRunnable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHandler.postDelayed(mRunnable, DELAY_MILLISECONDS);
    }

    @Override
    protected void onPause() {
        mHandler.removeCallbacks(mRunnable);
        super.onPause();
    }

    private class NextActivityRunnable implements Runnable {
        @Override
        public void run() {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        }
    }

}
