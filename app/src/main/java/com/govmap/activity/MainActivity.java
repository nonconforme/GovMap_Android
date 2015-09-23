package com.govmap.activity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import com.govmap.MainApplication;
import com.govmap.R;
import com.govmap.model.DataObject;
import com.govmap.utils.DataSearchType;

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
