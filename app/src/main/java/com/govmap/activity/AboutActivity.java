package com.govmap.activity;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.govmap.R;
import com.govmap.utils.AssetsUtil;
import com.govmap.utils.Const;

/**
 * Created by MediumMG on 22.10.2015.
 */
public class AboutActivity extends BaseActivity {

    private TextView tvVersion, tvTextTC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tvVersion = (TextView) findViewById(R.id.activity_about_version);
        tvTextTC = (TextView) findViewById(R.id.activity_about_tc_text);

        tvTextTC.setText(AssetsUtil.readFileFromAssets(getAssets(), Const.TERMS_AND_CONDITIONS_FILE_NAME));

        PackageManager pm = getPackageManager();
        try {
            String appVersion = pm.getPackageInfo(getPackageName(), 0).versionName;
            String appVersionText = getString(R.string.text_version) + appVersion;
            tvVersion.setText(appVersionText);
        } catch (PackageManager.NameNotFoundException e) {
            tvVersion.setVisibility(View.GONE);
        }
    }
}
