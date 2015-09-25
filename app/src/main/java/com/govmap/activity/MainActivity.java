package com.govmap.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.widget.SwitchCompat;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;

import com.govmap.MainApplication;
import com.govmap.R;
import com.govmap.model.DataObject;
import com.govmap.utils.DataSearchType;

import java.util.Locale;

/**
 * Created by Misha on 9/1/2015.
 */
public class MainActivity extends BaseActivity implements
        View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btnFindAddressByGeoNumber_AM).setOnClickListener(MainActivity.this);
        findViewById(R.id.btnFindGeoNumberByAddress_AM).setOnClickListener(MainActivity.this);
        findViewById(R.id.btnFindGeoNumberByCurPos_AM).setOnClickListener(MainActivity.this);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        ((SwitchCompat) menu.findItem(R.id.action_lang).getActionView().findViewById(R.id.action_lang_switch)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String lang = getBaseContext().getResources().getConfiguration().locale.getLanguage();

                Locale locale;
                if (lang.equals("iw") || lang.equals("he")) {
                    locale = new Locale("en", "US");
                } else {
                    locale = new Locale("iw", "IL");
                }
                Locale.setDefault(locale);

                Configuration config = new Configuration();
                config.locale = locale;

                Resources res = getBaseContext().getResources();
                DisplayMetrics displayMetrics = res.getDisplayMetrics();
                res.updateConfiguration(config, displayMetrics);

                Intent intent = new Intent(MainActivity.this, SplashActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_lang) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnFindAddressByGeoNumber_AM:
                findAddressByGeoNumber();
                break;
            case R.id.btnFindGeoNumberByAddress_AM:
                findGeoNumberByAddress();
                break;
            case R.id.btnFindGeoNumberByCurPos_AM:
                findGeoNumberByCurrentPosition();
                break;
        }
    }

    private void findAddressByGeoNumber() {
        startActivity(new Intent(MainActivity.this, GeoNumberActivity.class));
    }

    private void findGeoNumberByAddress() {
        startActivity(new Intent(MainActivity.this, SelectAddressActivity.class));
    }

    private void findGeoNumberByCurrentPosition() {

        if (isLocationServicesEnabled()) {
            DataObject mDataObject = new DataObject();

            Intent intent = new Intent(MainActivity.this, MapActivity.class);
            intent.putExtra(MainApplication.EXTRA_DATA_OBJECT, mDataObject);
            intent.putExtra(MainApplication.EXTRA_DATA_SEARCH_TYPE, DataSearchType.CURRENT_LOCATION.ordinal());
            startActivity(intent);
        }
        else {
            Intent locationMode = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(locationMode);
        }
    }

}
