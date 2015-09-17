package com.govmap.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.govmap.MainApplication;
import com.govmap.R;
import com.govmap.model.DataObject;
import com.govmap.utils.CustomTextWatcher;
import com.govmap.utils.DataSearchType;

/**
 * Created by Misha on 9/1/2015.
 */
public class SelectAddressActivity extends BaseActivity implements View.OnClickListener, TextView.OnEditorActionListener {

    private EditText etStreet, etHome, etCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_address);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        etStreet = (EditText) findViewById(R.id.etStreet_ASA);
        etHome = (EditText) findViewById(R.id.etHome_ASA);
        etCity = (EditText) findViewById(R.id.etCity_ASA);

        etStreet.addTextChangedListener(new CustomTextWatcher(etStreet));
        etHome.addTextChangedListener(new CustomTextWatcher(etHome));
        etCity.addTextChangedListener(new CustomTextWatcher(etCity));

        etHome.setOnEditorActionListener(SelectAddressActivity.this);

        findViewById(R.id.btnFind_ASA).setOnClickListener(this);

//        etCity.setText("הרצל");
//        etStreet.setText("ראשון לציון");
//        etHome.setText("74");
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
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

    private void findGeoNumber() {
        if (checkData())
            callRequest();
    }


    private boolean checkData() {
        if (TextUtils.isEmpty(etCity.getText())) {
            etCity.requestFocus();
            etCity.setBackgroundResource(R.drawable.select_address_edit_text_bckg_highligt);
            return false;
        }
        if (TextUtils.isEmpty(etStreet.getText())) {
            etStreet.requestFocus();
            etStreet.setBackgroundResource(R.drawable.select_address_edit_text_bckg_highligt);
            return false;
        }
        if (TextUtils.isEmpty(etHome.getText())) {
            etHome.requestFocus();
            etHome.setBackgroundResource(R.drawable.select_address_edit_text_bckg_highligt);
            return false;
        }
        return true;
    }

    private void callRequest() {
        DataObject mDataObject = new DataObject();

        String city = etCity.getText().toString();
        String street = etStreet.getText().toString();
        String home = etHome.getText().toString();

        String addressString = String.format(getString(R.string.req_for_cadastre), city, home, street);

        mDataObject.setAddress(addressString);

        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(etHome.getWindowToken(), 0);

        Intent intent = new Intent(SelectAddressActivity.this, MapActivity.class);
        intent.putExtra(MainApplication.EXTRA_DATA_OBJECT, mDataObject);
        intent.putExtra(MainApplication.EXTRA_DATA_SEARCH_TYPE, DataSearchType.ADDRESS.ordinal());
        startActivity(intent);
    }

}
