package com.govmap.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.govmap.R;
import com.govmap.utils.AppPreferences;
import com.govmap.utils.AssetsUtil;
import com.govmap.utils.Const;

/**
 * Created by MediumMG on 18.10.2015.
 */
public class TCActivity extends BaseActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private TextView tvTextTC;
    private Button btnMainMenu;
    private CheckBox cbAgree;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tc);

        tvTextTC = (TextView) findViewById(R.id.activity_tc_text);
        tvTextTC.setText(AssetsUtil.readFileFromAssets(getAssets(), Const.TERMS_AND_CONDITIONS_FILE_NAME));

        cbAgree = (CheckBox) findViewById(R.id.activity_tc_agree);
        cbAgree.setChecked(false);
        cbAgree.setOnCheckedChangeListener(TCActivity.this);

        btnMainMenu = (Button) findViewById(R.id.activity_tc_main_menu);
        btnMainMenu.setEnabled(false);
        btnMainMenu.setOnClickListener(TCActivity.this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        btnMainMenu.setEnabled(isChecked);
    }

    @Override
    public void onClick(View v) {
        AppPreferences.setBoolean(TCActivity.this, AppPreferences.KEY_IS_TC_SHOWED, true);

        startActivity(new Intent(TCActivity.this, MainActivity.class));
        finish();
    }

}
