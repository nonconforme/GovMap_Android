package com.govmap;

import android.os.Bundle;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;

/**
 * Created by MediumMG on 01.09.2015.
 */
public class MapActivity extends BaseActivity {

    GoogleMap mMap;

    @Override
    public void  onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.activity_map_fragment)).getMap();

        if (mMap != null) {

        }
        else
            finish();
    }
}
