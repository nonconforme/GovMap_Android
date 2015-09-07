package com.govmap.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
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
 * Created by Misha on 9/1/2015.
 */
public class MainActivity extends BaseActivity implements
        View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener{

    public static int LOCATION_INTERVAL = 20000;
    public static int LOCATION_FASTEST_INTERVAL = LOCATION_INTERVAL / 2;

    private GoogleApiClient mGoogleApiClient;
    private boolean mNeedToSendRequest = false;

    private DataObject mDataObject;

    private MainReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_icon);

        findViewById(R.id.btnFindAddressByGeoNumber_AM).setOnClickListener(MainActivity.this);
        findViewById(R.id.btnFindGeoNumberByAddress_AM).setOnClickListener(MainActivity.this);
        findViewById(R.id.btnFindGeoNumberByCurPos_AM).setOnClickListener(MainActivity.this);

        mGoogleApiClient = new GoogleApiClient.Builder(MainActivity.this)
                .addConnectionCallbacks(MainActivity.this)
                .addOnConnectionFailedListener(MainActivity.this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mReceiver = new MainReceiver();
        IntentFilter intentFilter = new IntentFilter(MainApplication.ACTION_INNER_CADASTRE);
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

    private void goToMap() {
        Log.v(MainApplication.TAG, mDataObject.toString());

        Intent intent = new Intent(MainActivity.this, MapActivity.class);
        intent.putExtra(MainApplication.EXTRA_DATA_OBJECT, mDataObject);
        startActivity(intent);
        finish();
    }

    private void findGeoNumberByCurrentPosition() {
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
        else {
            mGoogleApiClient.connect();
            mNeedToSendRequest = true;
        }
    }

    protected void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(LOCATION_INTERVAL)
                .setFastestInterval(LOCATION_FASTEST_INTERVAL);

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, locationRequest, MainActivity.this);

        mNeedToSendRequest = false;
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, MainActivity.this);
    }

    @Override
    public void onLocationChanged(Location location) {
        stopLocationUpdates();

        mDataObject = new DataObject();
        mDataObject.setLatitude(location.getLatitude());
        mDataObject.setLongitude(location.getLongitude());

        String latlng = String.valueOf(location.getLatitude()) + "," + String.valueOf(location.getLongitude());

        GeocodeClient.get().getGeocodeByLatLng(latlng, "he", new GeocodeCallback());
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
        showNotFoundToast();
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
                                home = addressComponent.longName.trim();
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
                Log.v(MainApplication.TAG, addressString);
                mDataObject.setAddress(addressString);

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

    private class MainReceiver extends BroadcastReceiver {

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
                    showNotFoundToast();
                }
                else {
                    // Get cadastre numbers
                    mDataObject.setCadastre(numbers.get(0), numbers.get(1));

                    goToMap();
                }
            }
        }
    }

}
