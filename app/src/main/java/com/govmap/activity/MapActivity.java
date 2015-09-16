package com.govmap.activity;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
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
import com.govmap.utils.DataSearchType;
import com.govmap.utils.GeocodeClient;
import com.govmap.view.GovProgressDialog;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by MediumMG on 01.09.2015.
 */
public class MapActivity extends BaseActivity implements
        View.OnClickListener,
        GoogleMap.OnMarkerDragListener,
        GoogleMap.OnMapLongClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    public static int LOCATION_INTERVAL = 20000;
    public static int LOCATION_FASTEST_INTERVAL = LOCATION_INTERVAL / 2;

    private GoogleApiClient mGoogleApiClient;
    private boolean mNeedToSendRequest = false;

    private GovProgressDialog mProgressDialog;

    private GoogleMap mMap;
    private TextView mNormal, mSatellite;

    private DataObject mData;
    private DataSearchType mSearchType;

    private Marker mMarker;
    private MapReceiver mReceiver;

    @Override
    public void  onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mData = getIntent().getParcelableExtra(MainApplication.EXTRA_DATA_OBJECT);
        mSearchType = DataSearchType.getEnum(getIntent().getIntExtra(MainApplication.EXTRA_DATA_SEARCH_TYPE, -1));

        mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.activity_map_fragment)).getMap();
        mNormal = (TextView) findViewById(R.id.activity_map_type_normal);
        mSatellite = (TextView) findViewById(R.id.activity_map_type_satellite);

        mProgressDialog = new GovProgressDialog(MapActivity.this);

        mGoogleApiClient = new GoogleApiClient.Builder(MapActivity.this)
                .addConnectionCallbacks(MapActivity.this)
                .addOnConnectionFailedListener(MapActivity.this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();

        mNormal.setOnClickListener(MapActivity.this);
        mSatellite.setOnClickListener(MapActivity.this);

        if (mMap != null && mData != null && mSearchType != null) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

            mMap.setOnMapLongClickListener(MapActivity.this);
            mMap.setOnMarkerDragListener(MapActivity.this);

            mMap.setInfoWindowAdapter(new CustomWindowInfoAdapter());

            Log.v(MainApplication.TAG, "get data: " + mData.toString());
            startSearch();
        }
        else
            finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mReceiver = new MapReceiver();
        IntentFilter intentFilter = new IntentFilter(MainApplication.ACTION_INNER_CADASTRE);
        intentFilter.addAction(MainApplication.ACTION_INNER_ADDRESS);
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        stopLocationUpdates();
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
        mSearchType = DataSearchType.COORDINATES;
        mData = new DataObject();
        mData.setLatitude(latLng.latitude);
        mData.setLongitude(latLng.longitude);
        startSearch();
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        marker.hideInfoWindow();
    }

    @Override
    public void onMarkerDrag(Marker marker) { }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        mSearchType = DataSearchType.COORDINATES;
        mData = new DataObject();
        mData.setLatitude(marker.getPosition().latitude);
        mData.setLongitude(marker.getPosition().longitude);
        startSearch();
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (mNeedToSendRequest)
            startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) { }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
//        notFoundAddress();
//        animateMapToLocation();
    }

    @Override
    public void onLocationChanged(Location location) {
        stopLocationUpdates();

        mData.setLatitude(location.getLatitude());
        mData.setLongitude(location.getLongitude());
        mSearchType = DataSearchType.COORDINATES;
        startSearch();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_map_type_normal: {
                mNormal.setBackgroundResource(R.color.blue_dark);
                mNormal.setTextColor(getResources().getColor(R.color.white));
                mSatellite.setBackgroundResource(R.color.white);
                mSatellite.setTextColor(getResources().getColor(R.color.blue_dark));
                if (mMap.getMapType() != GoogleMap.MAP_TYPE_NORMAL)
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            }
            case R.id.activity_map_type_satellite: {
                mSatellite.setBackgroundResource(R.color.blue_dark);
                mSatellite.setTextColor(getResources().getColor(R.color.white));
                mNormal.setBackgroundResource(R.color.white);
                mNormal.setTextColor(getResources().getColor(R.color.blue_dark));
                if (mMap.getMapType() != GoogleMap.MAP_TYPE_SATELLITE)
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            }
        }
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
            if (TextUtils.isEmpty(mData.getAddress())) {
                mAddress.setText(getString(R.string.text_address_not_found));
            }
            else {
                mAddress.setText(mData.getAddress());
            }

            if (mData.getBlock() < 0  &&  mData.getSmooth() < 0) {
                mBlock.setText(getString(R.string.text_cadastre_not_found));
                mSmooth.setVisibility(View.GONE);
            }
            else {
                mSmooth.setVisibility(View.VISIBLE);
                mBlock.setText(getString(R.string.text_block) + mData.getBlock());
                mSmooth.setText(getString(R.string.text_smooth) + mData.getSmooth());
            }
            return mMyMarkerView;
        }
    }




    private void animateMapToLocation() {
        Log.v(MainApplication.TAG, "show data: " + mData.toString());
        if (mProgressDialog != null && mProgressDialog.isShowing())
            mProgressDialog.dismiss();

        if (mData.getLatitude() == Double.MAX_VALUE ||
            mData.getLongitude() == Double.MAX_VALUE) {
            notFoundAddress();
            return;
        }
        if (mMarker != null)
            mMarker.remove();

        LatLng latLng = new LatLng(mData.getLatitude(), mData.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
        mMap.animateCamera(cameraUpdate);

        mMarker = mMap.addMarker(new MarkerOptions().position(latLng).draggable(true));

        mMarker.showInfoWindow();
    }

    private void startSearch() {
        switch (mSearchType) {
            case ADDRESS: {
                sendGetCoordinatesRequest(mData.getAddress());
                break;
            }
            case COORDINATES: {
                sendGetAddressRequest(mData.getLatitude(), mData.getLongitude());
                break;
            }
            case CADASTRE: {
                String cadastralString = String.format(getString(R.string.req_for_nubmer_format1),
                        String.valueOf(mData.getBlock()),
                        String.valueOf(mData.getSmooth()));
                ((MainApplication) getApplication()).startSearchWihCadastre(cadastralString);
                break;
            }
            case CURRENT_LOCATION: {
                if (mGoogleApiClient.isConnected()) {
                    startLocationUpdates();
                }
                else {
                    mGoogleApiClient.connect();
                    mNeedToSendRequest = true;
                }
                break;
            }
        }
        if (mProgressDialog != null && !mProgressDialog.isShowing())
            mProgressDialog.show();
    }

    private void sendGetAddressRequest(double latitude, double longitude) {
        String latlng = String.valueOf(latitude) + "," + String.valueOf(longitude);
        GeocodeClient.get().getGeocodeByLatLng(latlng, "iw", new GetAddressCallback());
    }

    private void sendGetCoordinatesRequest(String address) {
        GeocodeClient.get().getGeocodeByAddress(address.replace(" ", "+"), "iw", new GetCoordinatesCallback()) ;
    }

    protected void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(LOCATION_INTERVAL)
                .setFastestInterval(LOCATION_FASTEST_INTERVAL);

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, locationRequest, MapActivity.this);

        mNeedToSendRequest = false;
    }

    protected void stopLocationUpdates() {
        if (mGoogleApiClient.isConnected())
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, MapActivity.this);
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

                if (NO_RESULT_FOUND_HE.equals(cadastre) || numbers.size() != 2) {
                    // no results found
                }
                else {
                    // Get cadastre numbers
                    mData.setCadastre(numbers.get(0), numbers.get(1));

                    switch (mSearchType) {
                        case ADDRESS: {
                            break;
                        }
                        case COORDINATES: {
                            break;
                        }
                        case CADASTRE: {
                            break;
                        }
                    }
                }
                animateMapToLocation();
            }

            else
            if  (MainApplication.ACTION_INNER_ADDRESS.equals(intent.getAction())) {
                ((MainApplication) getApplication()).clearResults();

                String address = intent.getStringExtra(MainApplication.EXTRA_DATA_ADDRESS);

                if (NO_RESULT_FOUND_HE.equals(address)) {
                    // no results found
                    notGovMapReponse();
                }
                else {
                    // Get coordinates;
                    mData.setAddress(address);
                    sendGetCoordinatesRequest(mData.getAddress());
                }
            }
        }
    }

    private class GetAddressCallback implements Callback<GeocodeResponse> {

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
                !TextUtils.isEmpty(home)) {

                String addressString = String.format(getString(R.string.req_for_cadastre),
                        city, home, street);
                mData.setAddress(addressString);

                switch (mSearchType) {
                    case ADDRESS: {
                        animateMapToLocation();
                        break;
                    }
                    case COORDINATES: {
                        ((MainApplication) getApplication()).startSearchWithAddress(addressString);
                        break;
                    }
                    case CADASTRE: {
                        animateMapToLocation();
                        break;
                    }
                }
            }
            else {
                animateMapToLocation();
            }
        }

        @Override
        public void failure(RetrofitError error) {
            animateMapToLocation();
        }
    }

    private class GetCoordinatesCallback implements Callback<GeocodeResponse> {

        @Override
        public void success(GeocodeResponse geocodeResponse, Response response) {
            if (geocodeResponse.results.size() > 0) {

                mData.setLatitude(geocodeResponse.results.get(0).geometry.location.lat);
                mData.setLongitude(geocodeResponse.results.get(0).geometry.location.lng);

                switch (mSearchType) {
                    case ADDRESS: {
                        ((MainApplication) getApplication()).startSearchWithAddress(mData.getAddress());
                        break;
                    }
                    case COORDINATES:  {
                        animateMapToLocation();
                        break;
                    }
                    case CADASTRE: {
                        animateMapToLocation();
                        break;
                    }
                }
            }
            else
                animateMapToLocation();
        }

        @Override
        public void failure(RetrofitError error) {
            animateMapToLocation();
        }
    }

    private void notFoundAddress() {
        if (mProgressDialog != null && mProgressDialog.isShowing())
            mProgressDialog.dismiss();
        new AlertDialog.Builder(MapActivity.this)
                .setMessage(R.string.message_address_not_found)
                .setPositiveButton(R.string.text_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        startSearch();
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

    private void notFoundCadastre() {
        if (mProgressDialog != null && mProgressDialog.isShowing())
            mProgressDialog.dismiss();
        new AlertDialog.Builder(MapActivity.this)
                .setMessage(R.string.message_cadastre_not_found)
                .setPositiveButton(R.string.text_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        startSearch();
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

    private void notGovMapReponse() {
        if (mProgressDialog != null && mProgressDialog.isShowing())
            mProgressDialog.dismiss();
        new AlertDialog.Builder(MapActivity.this)
                .setMessage(R.string.message_connect_error)
                .setPositiveButton(R.string.text_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        startSearch();
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
