package com.govmap;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * Created by Misha on 9/1/2015.
 */
public class MainActivity extends BaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btnFindAddressByGeoNumber_AM).setOnClickListener(this);
        findViewById(R.id.btnFindGeoNumberByAddress_AM).setOnClickListener(this);
        findViewById(R.id.btnFindGeoNumberByCurPos_AM).setOnClickListener(this);
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

    }
}
