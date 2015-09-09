package com.govmap.utils;

import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.govmap.R;

/**
 * Created by Misha on 9/9/2015.
 */
public class CustomTextWatcher implements TextWatcher{
    private EditText editText;

    public CustomTextWatcher(EditText editText) {
        this.editText = editText;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if(s.length() != 0) editText.setBackgroundResource(R.drawable.select_address_edit_text_bckg);
    }
}
