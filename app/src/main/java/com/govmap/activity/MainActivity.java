package com.govmap.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
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
        View.OnClickListener,
        CompoundButton.OnCheckedChangeListener {

    private SwitchCompat mSwitchCompat;

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
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(getResources().getString(R.string.title_main));
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mSwitchCompat = (SwitchCompat) menu.findItem(R.id.action_lang).getActionView().findViewById(R.id.action_lang_switch);
        mSwitchCompat.setText(getResources().getString(R.string.action_switch_lang));
        mSwitchCompat.setChecked(false);
        mSwitchCompat.setOnCheckedChangeListener(MainActivity.this);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_lang: return true;
            case R.id.action_about: {
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
                return true;
            }

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

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked)
            showChangeLocale();
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
            new AlertDialog.Builder(MainActivity.this)
                    .setMessage(R.string.message_no_gps)
                    .setPositiveButton(R.string.text_go_to_setting, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent locationMode = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(locationMode);
                        }
                    })
                    .setNegativeButton(R.string.text_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setCancelable(false)
                    .create().show();
        }
    }

    private void showChangeLocale() {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(R.string.message_change_locale)
                .setPositiveButton(R.string.text_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
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

                        SharedPreferences.Editor editor = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE).edit();
                        editor.putString(KEY_LANG, locale.getLanguage());
                        editor.putString(KEY_COUNTRY, locale.getCountry());
                        editor.commit();

                        dialog.dismiss();
                        finish();
                    }
                })
                .setNegativeButton(R.string.text_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        mSwitchCompat.setChecked(!mSwitchCompat.isChecked());
                    }
                })
                .setCancelable(false)
                .create().show();
    }

}
