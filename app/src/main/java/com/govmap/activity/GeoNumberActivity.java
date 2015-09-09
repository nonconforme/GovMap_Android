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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.govmap.MainApplication;
import com.govmap.R;
import com.govmap.model.DataObject;
import com.govmap.model.GeocodeResponse;
import com.govmap.utils.CustomTextWatcher;
import com.govmap.utils.GeocodeClient;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by MediumMG on 01.09.2015.
 */
public class GeoNumberActivity extends BaseActivity implements View.OnClickListener, TextView.OnEditorActionListener {

    private EditText etBlock, etSmooth;
    private Button btnSearch;

    private GeoNumberReceiver mReceiver;

    private DataObject mDataObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geonumber);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        etBlock = (EditText) findViewById(R.id.etBlock_AGN);
        etSmooth = (EditText) findViewById(R.id.etSmooth_AGN);
        btnSearch = (Button) findViewById(R.id.btnSearch_AGN);

        etSmooth.setOnEditorActionListener(GeoNumberActivity.this);
        btnSearch.setOnClickListener(GeoNumberActivity.this);

        etSmooth.addTextChangedListener(new CustomTextWatcher(etSmooth));
        etBlock.addTextChangedListener(new CustomTextWatcher(etBlock));
    }

    @Override
    protected void onResume() {
        super.onResume();

        mReceiver = new GeoNumberReceiver();
        IntentFilter intentFilter = new IntentFilter(MainApplication.ACTION_INNER_ADDRESS);
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        if (mReceiver != null)
            unregisterReceiver(mReceiver);
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        findAddress();
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH)
            findAddress();
        return true;
    }

    private void goToMap() {
        Log.v(MainApplication.TAG, mDataObject.toString());

        Intent intent = new Intent(GeoNumberActivity.this, MapActivity.class);
        intent.putExtra(MainApplication.EXTRA_DATA_OBJECT, mDataObject);
        startActivity(intent);
        finish();
    }

    private void findAddress() {
        if (checkData())
            callRequest();
    }

    private boolean checkData() {
        if (TextUtils.isEmpty(etBlock.getText())) {
            etBlock.requestFocus();
            etBlock.setBackgroundResource(R.drawable.select_address_edit_text_bckg_highligt);
            return false;
        }
        if (TextUtils.isEmpty(etSmooth.getText())) {
            etSmooth.requestFocus();
            etSmooth.setBackgroundResource(R.drawable.select_address_edit_text_bckg_highligt);
            return false;
        }
        return true;
    }

    private void callRequest() {
        mDataObject = new DataObject();

        String block = String.valueOf(etBlock.getText());
        String smooth = String.valueOf(etSmooth.getText());

        String cadastralString = String.format(getString(R.string.req_for_nubmer_format1), block, smooth);

        Log.v(MainApplication.TAG, cadastralString);
        mDataObject.setCadastre(Integer.valueOf(block), Integer.valueOf(smooth));

        ((MainApplication) getApplication()).startSearchWihCadastre(cadastralString);
    }


    private class GeoNumberReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (MainApplication.ACTION_INNER_ADDRESS.equals(intent.getAction())) {
                ((MainApplication) getApplication()).clearResults();

                String address = intent.getStringExtra(MainApplication.EXTRA_DATA_ADDRESS);

                if (NO_RESULT_FOUND_HE.equals(address)) {
                    // no results found
                    showNotFoundToast();
                }
                else {
                    // Get coordinates;
                    mDataObject.setAddress(address);

                    GeocodeClient.get().getGeocodeByAddress(address.replace(" ", "+"), "he", new GeocodeCallback()) ;
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
