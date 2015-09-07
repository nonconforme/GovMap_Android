package com.govmap.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.govmap.MainApplication;
import com.govmap.R;
import com.govmap.model.DataObject;
import com.govmap.model.GeocodeResponse;
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
public class SelectAddressActivity extends BaseActivity implements View.OnClickListener, TextView.OnEditorActionListener {

    private EditText etStreet, etHome, etCity;

    private DataObject mDataObject;

    private SelectAddressReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_address);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        etStreet = (EditText) findViewById(R.id.etStreet_ASA);
        etHome = (EditText) findViewById(R.id.etHome_ASA);
        etCity = (EditText) findViewById(R.id.etCity_ASA);

        findViewById(R.id.btnFind_ASA).setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mReceiver = new SelectAddressReceiver();
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
    public void onClick(View view) {
        findGeoNumber();
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH)
            findGeoNumber();
        return true;
    }

    private void goToMap() {
        Log.v(MainApplication.TAG, mDataObject.toString());

        Intent intent = new Intent(SelectAddressActivity.this, MapActivity.class);
        intent.putExtra(MainApplication.EXTRA_DATA_OBJECT, mDataObject);
        startActivity(intent);
        finish();
    }

    private void findGeoNumber() {
        if (checkData())
            callRequest();
    }


    private boolean checkData() {
        if (TextUtils.isEmpty(etCity.getText())) {
            etCity.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(etStreet.getText())) {
            etStreet.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(etHome.getText())) {
            etHome.requestFocus();
            return false;
        }
        return true;
    }

    private void callRequest() {
        mDataObject = new DataObject();

        String city = etCity.getText().toString();
        String street = etStreet.getText().toString();
        String home = etStreet.getText().toString();

        String addressString = String.format(getString(R.string.req_for_cadastre),
                city, home, street);
        Log.v(MainApplication.TAG, addressString);
        mDataObject.setAddress(addressString);

        ((MainApplication) getApplication()).startSearchWithAddress(addressString);
    }


    private class SelectAddressReceiver extends BroadcastReceiver {

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

                    GeocodeClient.get().getGeocodeByAddress(mDataObject.getAddress().replace(" ", "+"), "he", new GeocodeCallback()) ;
                }
            }
        }
    }

    private class GeocodeCallback implements Callback<GeocodeResponse> {

        @Override
        public void success(GeocodeResponse geocodeResponse, Response response) {
            Log.v(MainApplication.TAG, geocodeResponse.toString());
            if (geocodeResponse.results.size() > 0) {

                mDataObject.setLatitude(geocodeResponse.results.get(0).geometry.location.lat);
                mDataObject.setLongitude(geocodeResponse.results.get(0).geometry.location.lng);

                goToMap();
            }
            else
                showNotFoundToast();
        }

        @Override
        public void failure(RetrofitError error) {
            showNotFoundToast();
        }
    }
}
