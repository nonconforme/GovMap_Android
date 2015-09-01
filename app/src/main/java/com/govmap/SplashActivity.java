package com.govmap;

import android.os.Bundle;
import android.util.Log;

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Log.v("SplashActivity", Keys.getCertificateSHA1Fingerprint(this));
        Log.v("SplashActivity", Keys.getHashKey(this));
    }

}
