package com.govmap.activity;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
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
import com.govmap.view.GovWebView;

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
    private TextView mNormalTab, mSatelliteTab, mGovmapTab;
    private GovWebView mGovmap;

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

        mGovmap = (GovWebView)findViewById(R.id.activity_map_govmap);
        mGovmap.setWebChromeClient(new WebChromeClient());
        mGovmap.setWebViewClient(new GovWebClient());
        mGovmap.setVisibility(View.GONE);
        mGovmap.loadUrl(MainApplication.GOV_URL);

        mNormalTab = (TextView) findViewById(R.id.activity_map_type_normal);
        mSatelliteTab = (TextView) findViewById(R.id.activity_map_type_satellite);
        mGovmapTab = (TextView) findViewById(R.id.activity_map_type_govmap);

        mProgressDialog = new GovProgressDialog(MapActivity.this);

        mGoogleApiClient = new GoogleApiClient.Builder(MapActivity.this)
                .addConnectionCallbacks(MapActivity.this)
                .addOnConnectionFailedListener(MapActivity.this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();

        mNormalTab.setOnClickListener(MapActivity.this);
        mSatelliteTab.setOnClickListener(MapActivity.this);
        mGovmapTab.setOnClickListener(MapActivity.this);

        if (mMap != null && mData != null && mSearchType != null) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

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

        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(getResources().getString(R.string.title_map));

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
    protected void onDestroy() {
        mGoogleApiClient.disconnect();
        ((MainApplication) getApplication()).clearHandlers();
        super.onDestroy();
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
                if (mData ==  null)
                    return true;

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
    public void onConnectionFailed(ConnectionResult result) {
        if (mProgressDialog != null && mProgressDialog.isShowing())
            mProgressDialog.dismiss();

        if (mResolvingError) {
            // Already attempting to resolve an error.
            Toast.makeText(MapActivity.this, "", Toast.LENGTH_LONG).show();
            return;
        }
        else if (result.hasResolution()) {
            try {
                mResolvingError = true;
                result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                mGoogleApiClient.connect();
                if (mProgressDialog != null && !mProgressDialog.isShowing())
                    mProgressDialog.show();
            }
        } else {
            // Show dialog using GoogleApiAvailability.getErrorDialog()
            showErrorDialog(result.getErrorCode());
            mResolvingError = true;
        }

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
                mNormalTab.setBackgroundResource(R.color.blue_dark);
                mNormalTab.setTextColor(getResources().getColor(R.color.white));

                mSatelliteTab.setBackgroundResource(R.color.white);
                mSatelliteTab.setTextColor(getResources().getColor(R.color.blue_dark));

                mGovmapTab.setBackgroundResource(R.color.white);
                mGovmapTab.setTextColor(getResources().getColor(R.color.blue_dark));

                mGovmap.setVisibility(View.GONE);
                if (mMap.getMapType() != GoogleMap.MAP_TYPE_NORMAL)
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            }
            case R.id.activity_map_type_satellite: {
                mSatelliteTab.setBackgroundResource(R.color.blue_dark);
                mSatelliteTab.setTextColor(getResources().getColor(R.color.white));

                mNormalTab.setBackgroundResource(R.color.white);
                mNormalTab.setTextColor(getResources().getColor(R.color.blue_dark));

                mGovmapTab.setBackgroundResource(R.color.white);
                mGovmapTab.setTextColor(getResources().getColor(R.color.blue_dark));

                mGovmap.setVisibility(View.GONE);
                if (mMap.getMapType() != GoogleMap.MAP_TYPE_SATELLITE)
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            }
            case R.id.activity_map_type_govmap: {
                mGovmapTab.setBackgroundResource(R.color.blue_dark);
                mGovmapTab.setTextColor(getResources().getColor(R.color.white));

                mNormalTab.setBackgroundResource(R.color.white);
                mNormalTab.setTextColor(getResources().getColor(R.color.blue_dark));

                mSatelliteTab.setBackgroundResource(R.color.white);
                mSatelliteTab.setTextColor(getResources().getColor(R.color.blue_dark));

                mGovmap.reload();
                mGovmap.setVisibility(View.VISIBLE);
            }

        }
    }


    class CustomWindowInfoAdapter implements GoogleMap.InfoWindowAdapter {
        private final View mMyMarkerView;
        private final TextView mAddress, mBlockSmooth;

        public CustomWindowInfoAdapter() {
            mMyMarkerView = getLayoutInflater().inflate(R.layout.window_info_maps, null);
            mAddress = (TextView) mMyMarkerView.findViewById(R.id.tv_address);
            mBlockSmooth = (TextView) mMyMarkerView.findViewById(R.id.tv_block_smooth);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            if (TextUtils.isEmpty(mData.getShowedAddress())) {
                mAddress.setText(getString(R.string.text_address_not_found));
            }
            else {
                mAddress.setText(mData.getShowedAddress());
            }

            if (mData.getLot() < 0  &&  mData.getParcel() < 0) {
                mBlockSmooth.setText(getString(R.string.text_cadastre_not_found));
            }
            else {
                String cadastralString = getString(R.string.text_lot)
                                        + " "
                                        + String.valueOf(mData.getLot())
                                        + " "
                                        + getString(R.string.text_parcel)
                                        + " "
                                        + String.valueOf(mData.getParcel());

                mBlockSmooth.setText(cadastralString);
            }
            return mMyMarkerView;
        }
    }

    private String clearAddress(String address) {
        return address.replace("רחוב: ","").replace("בית: ","").replace("עיר: ", "").trim();
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
                sendGetCoordinatesRequest(mData.getSearchAddress());
                break;
            }
            case COORDINATES: {
                sendGetAddressRequest(mData.getLatitude(), mData.getLongitude());
                break;
            }
            case CADASTRE: {
                String cadastralString = String.format(getString(R.string.req_for_nubmer_format),
                        String.valueOf(mData.getLot()),
                        String.valueOf(mData.getParcel()));
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
        address = clearAddress(address);
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




    private class GovWebClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            if (mGovmap.getVisibility() == View.VISIBLE) {
                if (mProgressDialog != null && !mProgressDialog.isShowing())
                    mProgressDialog.show();
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (mGovmap.getVisibility() == View.VISIBLE) {
                if (mProgressDialog != null && mProgressDialog.isShowing())
                    mProgressDialog.dismiss();
            }

            super.onPageFinished(view, url);
            if (!TextUtils.isEmpty(mData.getSearchAddress()))
                mGovmap.loadUrl(String.format("javascript:(function() {document.getElementById('tbSearchWord').value = '%s';})();", mData.getSearchAddress()));
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return false;
        }
    }


    private class MapReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (MainApplication.ACTION_INNER_CADASTRE.equals(intent.getAction())) {

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

                String addressResult = intent.getStringExtra(MainApplication.EXTRA_DATA_ADDRESS);

                if (NO_RESULT_FOUND_HE.equals(addressResult)) {
                    // no results found
                    notGovMapReponse();
                }
                else {
                    // Parse result from govmap
                    String showedAddresses = "";
                    String[] addresses = addressResult.replace("\t", "").split("\n");
                    for (int i = 0; i < addresses.length; i++) {
                        String[] values = addresses[i].split(",");

                        String city = "", street = "", home = "";
                        for (int j = 0; j < values.length; j++) {
                            if (values[j].contains("עיר:"))
                                city = values[j].replace("רחוב:","").replace("בית:","").replace("עיר:", "").trim();
                            if (values[j].contains("רחוב:"))
                                street = values[j].replace("רחוב:","").replace("בית:","").replace("עיר:", "").trim();
                            if (values[j].contains("בית:"))
                                home = values[j].replace("רחוב:","").replace("בית:","").replace("עיר:", "").trim();
                        }
                        addresses[i] = String.format(getString(R.string.req_for_cadastre), city, home, street);
                        showedAddresses += (i == addresses.length - 1) ?  addresses[i] : addresses[i]+"\n";
                    }
                    showedAddresses.trim();

                    mData.setSearchAddress(addresses[0]);
                    mData.setShowedAddress(showedAddresses);
                    sendGetCoordinatesRequest(mData.getSearchAddress());
                }
            }
        }
    }

    private class GetAddressCallback implements Callback<GeocodeResponse> {

        @Override
        public void success(GeocodeResponse geocodeResponse, Response response) {
            if (MapActivity.this.isFinishing()) {
                Log.v(MainApplication.TAG, "isFinishing");
                return;
            }

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

                String addressSearchString = String.format(getString(R.string.req_for_cadastre), city, home, street);
                String addressShowedString = String.format(getString(R.string.req_for_cadastre), city, home, street);
                mData.setSearchAddress(addressSearchString);
                mData.setShowedAddress(addressShowedString);

                switch (mSearchType) {
                    case ADDRESS: {
                        animateMapToLocation();
                        break;
                    }
                    case COORDINATES: {
                        ((MainApplication) getApplication()).startSearchWithAddress(addressSearchString);
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
            if (MapActivity.this.isFinishing()) {
                Log.v(MainApplication.TAG, "isFinishing");
                return;
            }

            animateMapToLocation();
        }
    }

    private class GetCoordinatesCallback implements Callback<GeocodeResponse> {

        @Override
        public void success(GeocodeResponse geocodeResponse, Response response) {
            if (geocodeResponse.results.size() > 0) {
                if (MapActivity.this.isFinishing()) {
                    Log.v(MainApplication.TAG, "isFinishing");
                    return;
                }

                mData.setLatitude(geocodeResponse.results.get(0).geometry.location.lat);
                mData.setLongitude(geocodeResponse.results.get(0).geometry.location.lng);

                switch (mSearchType) {
                    case ADDRESS: {
                        ((MainApplication) getApplication()).startSearchWithAddress(mData.getSearchAddress());
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
            if (MapActivity.this.isFinishing()) {
                Log.v(MainApplication.TAG, "isFinishing");
                return;
            }

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
                        finish();
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
                        finish();
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
                        finish();
                    }
                })
                .setCancelable(false)
                .create().show();
    }

    //region handle connection failures
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    private static final String DIALOG_ERROR = "dialog_error";
    private boolean mResolvingError = false;

    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getSupportFragmentManager(), "errordialog");
    }

    /* Called from ErrorDialogFragment when the dialog is dismissed. */
    public void onDialogDismissed() {
        mResolvingError = false;
    }

    /* A fragment to display an error dialog */
    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() { }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GoogleApiAvailability.getInstance().getErrorDialog(
                    this.getActivity(), errorCode, REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((MapActivity) getActivity()).onDialogDismissed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_RESOLVE_ERROR) {
            mResolvingError = false;
            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (!mGoogleApiClient.isConnecting() &&
                        !mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect();
                }
            }
        }
    }
    //endregion
}

