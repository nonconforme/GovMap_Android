package com.govmap;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by MediumMG on 01.09.2015.
 */
public class GeoNumberActivity extends BaseActivity implements View.OnClickListener, TextView.OnEditorActionListener {

    private EditText etGeoNum1, etGeoNum2;
    private Button btnSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geonumber);

        getSupportActionBar().setDisplayShowHomeEnabled(true);

        etGeoNum1 = (EditText) findViewById(R.id.etGeoNum1_AGN);
        etGeoNum2 = (EditText) findViewById(R.id.etGeoNum2_AGN);
        btnSearch = (Button) findViewById(R.id.btnSearch_AGN);

        etGeoNum2.setOnEditorActionListener(GeoNumberActivity.this);
        btnSearch.setOnClickListener(GeoNumberActivity.this);
    }


    @Override
    public void onClick(View v) {
        if (checkData())
            callRequest();
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (checkData())
            callRequest();
        return true;
    }

    private boolean checkData() {
        if (TextUtils.isEmpty(etGeoNum1.getText())) {
            etGeoNum1.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(etGeoNum2.getText())) {
            etGeoNum2.requestFocus();
            return false;
        }
        return true;
    }

    private void callRequest() {
        String geonum1 = String.valueOf(etGeoNum1.getText());
        String geonum2 = String.valueOf(etGeoNum2.getText());
        //TODO: api request
        startActivity(new Intent(GeoNumberActivity.this, MapActivity.class));
    }
}
