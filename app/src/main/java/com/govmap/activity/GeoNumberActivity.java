package com.govmap.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.govmap.MainApplication;
import com.govmap.R;
import com.govmap.model.DataObject;
import com.govmap.utils.CustomTextWatcher;
import com.govmap.utils.DataSearchType;

/**
 * Created by MediumMG on 01.09.2015.
 */
public class GeoNumberActivity extends BaseActivity implements View.OnClickListener, TextView.OnEditorActionListener {

    private EditText etBlock, etSmooth;
    private Button btnSearch;

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
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(getResources().getString(R.string.title_lot_parcel));
    }

    @Override
    protected void onPause() {
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
        DataObject mDataObject = new DataObject();

        String block = String.valueOf(etBlock.getText());
        String smooth = String.valueOf(etSmooth.getText());

        mDataObject.setCadastre(Integer.valueOf(block), Integer.valueOf(smooth));

        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(etSmooth.getWindowToken(), 0);

        Intent intent = new Intent(GeoNumberActivity.this, MapActivity.class);
        intent.putExtra(MainApplication.EXTRA_DATA_OBJECT, mDataObject);
        intent.putExtra(MainApplication.EXTRA_DATA_SEARCH_TYPE, DataSearchType.CADASTRE.ordinal());
        startActivity(intent);
    }


}
