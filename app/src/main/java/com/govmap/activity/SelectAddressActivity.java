package com.govmap.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.govmap.R;
import com.govmap.activity.BaseActivity;

/**
 * Created by Misha on 9/1/2015.
 */
public class SelectAddressActivity extends BaseActivity implements View.OnClickListener {
    private EditText mStreetEt, mHomeEt, mCityEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_address);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mStreetEt = (EditText) findViewById(R.id.etStreet_ASA);
        mHomeEt = (EditText) findViewById(R.id.etHome_ASA);
        mCityEt = (EditText) findViewById(R.id.etCity_ASA);

        findViewById(R.id.btnFind_ASA).setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        findGeoNumber();
    }

    private void findGeoNumber() {

    }
}
