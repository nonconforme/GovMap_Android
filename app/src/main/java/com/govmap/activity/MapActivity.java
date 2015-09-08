package com.govmap.activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.govmap.MainApplication;
import com.govmap.R;
import com.govmap.model.DataObject;

/**
 * Created by MediumMG on 01.09.2015.
 */
public class MapActivity extends BaseActivity {

    private GoogleMap mMap;
    private DataObject mData;

    @Override
    public void  onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mData = getIntent().getParcelableExtra(MainApplication.EXTRA_DATA_OBJECT);

        mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.activity_map_fragment)).getMap();

        if (mMap != null && mData != null) {
            mMap.getUiSettings().setAllGesturesEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.getUiSettings().setIndoorLevelPickerEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);

            mMap.setMyLocationEnabled(true);

            animateMapToLocation(mData.getLatitude(), mData.getLongitude());
        }
        else
            finish();
    }

    private void animateMapToLocation(double latitude, double longitude) {
        LatLng latLng = new LatLng(latitude, longitude);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
        mMap.animateCamera(cameraUpdate);

        mMap.setInfoWindowAdapter(new CustomWindowInfoAdapter());

        mMap.addMarker(new MarkerOptions().position(latLng)).showInfoWindow();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            try {
                String url = String.format("waze://?ll=%s,%s&navigate=yes", String.valueOf(mData.getLatitude()), String.valueOf(mData.getLongitude()));
                Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse(url) );
                startActivity(intent);
            }
            catch (ActivityNotFoundException ex) {
                Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse( "market://details?id=com.waze" ) );
                startActivity(intent);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class CustomWindowInfoAdapter implements GoogleMap.InfoWindowAdapter {
        private final View mMyMarkerView;
        private final TextView mAddress, mBlock, mSmooth;

        public CustomWindowInfoAdapter() {
            mMyMarkerView = getLayoutInflater()
                    .inflate(R.layout.window_info_maps, null);
            mAddress = (TextView) mMyMarkerView.findViewById(R.id.tv_address);
            mBlock = (TextView) mMyMarkerView.findViewById(R.id.tv_block);
            mSmooth = (TextView) mMyMarkerView.findViewById(R.id.tv_smooth);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            mAddress.setText(mData.getAddress());
            mBlock.setText(getString(R.string.text_block) + mData.getBlock());
            mSmooth.setText(getString(R.string.text_smooth) + mData.getSmooth());
            return mMyMarkerView;
        }
    }

}
