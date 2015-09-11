package com.govmap.activity;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import com.govmap.model.AddressComponent;
import com.govmap.model.DataObject;
import com.govmap.model.GeocodeResponse;
import com.govmap.model.Result;
import com.govmap.utils.GeocodeClient;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by MediumMG on 01.09.2015.
 */
public class MapActivity extends BaseActivity implements GoogleMap.OnMarkerDragListener, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    private DataObject mData, mTemporaryData;

    private Marker mMarker;
    private MapReceiver mReceiver;

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
            mMap.setOnMapLongClickListener(MapActivity.this);
            mMap.setOnMarkerDragListener(MapActivity.this);

            mMap.setInfoWindowAdapter(new CustomWindowInfoAdapter());

            animateMapToLocation();
        }
        else
            finish();
    }

    private void animateMapToLocation() {
        if (mMarker != null)
            mMarker.remove();

        LatLng latLng = new LatLng(mData.getLatitude(), mData.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
        mMap.animateCamera(cameraUpdate);

        mMarker = mMap.addMarker(new MarkerOptions().position(latLng));
        mMarker.showInfoWindow();
    }


    @Override
    protected void onResume() {
        super.onResume();

        mReceiver = new MapReceiver();
        IntentFilter intentFilter = new IntentFilter(MainApplication.ACTION_INNER_CADASTRE);
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        if (mReceiver != null)
            unregisterReceiver(mReceiver);
        super.onPause();
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

    @Override
    public void onMapLongClick(LatLng latLng) {
        mTemporaryData = new DataObject();
        mTemporaryData.setLatitude(latLng.latitude);
        mTemporaryData.setLongitude(latLng.longitude);
        String latlng = String.valueOf(latLng.latitude) + "," + String.valueOf(latLng.longitude);

        GeocodeClient.get().getGeocodeByLatLng(latlng, "iw", new GeocodeCallback());
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        mTemporaryData = new DataObject();
    }

    @Override
    public void onMarkerDrag(Marker marker) { }

    @Override
    public void onMarkerDragEnd(Marker marker) {

        mTemporaryData.setLatitude(marker.getPosition().latitude);
        mTemporaryData.setLongitude(marker.getPosition().longitude);
        String latlng = String.valueOf(marker.getPosition().latitude) + "," + String.valueOf(marker.getPosition().longitude);

        GeocodeClient.get().getGeocodeByLatLng(latlng, "iw", new GeocodeCallback());
    }

    class CustomWindowInfoAdapter implements GoogleMap.InfoWindowAdapter {
        private final View mMyMarkerView;
        private final TextView mAddress, mBlock, mSmooth;

        public CustomWindowInfoAdapter() {
            mMyMarkerView = getLayoutInflater().inflate(R.layout.window_info_maps, null);
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







    private class GeocodeCallback implements Callback<GeocodeResponse> {

        @Override
        public void success(GeocodeResponse geocodeResponse, Response response) {
            String city = "", street = "", home = "";

            for (Result result : geocodeResponse.results) {
                if (result.types.size() > 0 && "street_address".equals(result.types.get(0))) {
                    for(AddressComponent addressComponent :result.addressComponents) {
                        if (addressComponent.types.size() > 0 ) {
                            if ("street_number".equals(addressComponent.types.get(0))) {
                                String homeS = addressComponent.longName.trim();

                                ArrayList<Integer> numbers = new ArrayList<Integer>();
                                Pattern p = Pattern.compile("\\d+");
                                Matcher m = p.matcher(homeS);
                                while (m.find()) {
                                    numbers.add(Integer.parseInt(m.group()));
                                }
                                if (numbers.size() > 0)
                                    home = String.valueOf(numbers.get(0));
                            }
                            else
                            if ("route".equals(addressComponent.types.get(0))) {
                                street = addressComponent.longName
                                        .replace("Street", "")
                                        .replace("street", "")
                                        .replace("St", "")
                                        .replace("st", "")
                                        .trim();
                            }
                            else
                            if ("locality".equals(addressComponent.types.get(0))) {
                                city = addressComponent.longName.trim();
                            }
                        }
                    }
                }
            }

            if (!TextUtils.isEmpty(city) &&
                !TextUtils.isEmpty(street) &&
                !TextUtils.isEmpty(home) &&
                mTemporaryData != null) {

                String addressString = String.format(getString(R.string.req_for_cadastre),
                        city, home, street);
                Log.v(MainApplication.TAG, addressString);
                mTemporaryData.setAddress(addressString);

                ((MainApplication) getApplication()).startSearchWithAddress(addressString);
            }
            else {
                showNotFoundToast();
            }
        }

        @Override
        public void failure(RetrofitError error) {
            showNotFoundToast();
        }
    }

    private class MapReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (MainApplication.ACTION_INNER_CADASTRE.equals(intent.getAction())) {
                ((MainApplication) getApplication()).clearResults();

                String cadastre = intent.getStringExtra(MainApplication.EXTRA_DATA_CADASTRE);

                ArrayList<Integer> numbers = new ArrayList<Integer>();
                Pattern p = Pattern.compile("\\d+");
                Matcher m = p.matcher(cadastre);
                while (m.find()) {
                    numbers.add(Integer.parseInt(m.group()));
                }

                if (NO_RESULT_FOUND_HE.equals(cadastre) || numbers.size() != 2 || mTemporaryData == null) {
                    // no results found
                    showNotFoundToast();
                }
                else {
                    // Get cadastre numbers
                    mTemporaryData.setCadastre(numbers.get(0), numbers.get(1));

                    if (mTemporaryData != null) {
                        mData = mTemporaryData;
                        mTemporaryData = null;
                    }

                    animateMapToLocation();
                }
            }
        }
    }

    @Override
    protected void showNotFoundToast() {
        mTemporaryData = null;
        super.showNotFoundToast();
    }


}
