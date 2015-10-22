package com.govmap.utils;

import android.content.res.AssetManager;
import android.text.TextUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by MediumMG on 22.10.2015.
 */
public class AssetsUtil {

    public static String readFileFromAssets(AssetManager manager, String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return null;
        }

        try {
            InputStream inputStream = manager.open(fileName);

            int size = inputStream.available();

            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();

            return new String(buffer);

        }
        catch (IOException e) {
            return null;
        }
    }
}
