package com.govmap;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btnFindAddressByGeoNumber_AM).setOnClickListener(this);
        findViewById(R.id.btnFindGeoNumberByAddress_AM).setOnClickListener(this);
        findViewById(R.id.btnFindGeoNumberByCurPos_AM).setOnClickListener(this);

        mGoogleApiClient = new GoogleApiClient.Builder(MainActivity.this)
                .addConnectionCallbacks(MainActivity.this)
                .addOnConnectionFailedListener(MainActivity.this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
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

    }

    private void findGeoNumberByAddress() {
        startActivity(new Intent(this, SelectAddressActivity.class));
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
                mGoogleApiClient, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        stopLocationUpdates();
        Toast.makeText(MainActivity.this, location.toString(), Toast.LENGTH_SHORT).show();
        //TODO api request
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
        //TODO show error
    }


}
