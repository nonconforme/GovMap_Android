package com.govmap.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.govmap.MainApplication;

/**
 * Created by MediumMG on 03.09.2015.
 */
public class GovWebView extends WebView {

    private static String TAG = GovWebView.class.getSimpleName();

    public GovWebView(Context context) {
        super(context);
        init(context);
    }

    public GovWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GovWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public GovWebView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    public GovWebView(Context context, AttributeSet attrs, int defStyleAttr, boolean privateBrowsing) {
        super(context, attrs, defStyleAttr, privateBrowsing);
        init(context);
    }

    private void init(Context context) {
        if (isInEditMode())
            return;

        WebSettings settings = getSettings();

        setLongClickable(true);
        setScrollbarFadingEnabled(true);
        setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        setDrawingCacheEnabled(true);

        settings.setLoadsImagesAutomatically(true);
        settings.setSaveFormData(true);
        settings.setLoadWithOverviewMode(false);
        settings.setSupportMultipleWindows(false);
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        settings.setAppCacheEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setDomStorageEnabled(true);
        CookieManager.getInstance().setAcceptCookie(true);

        settings.setUseWideViewPort(true);
        settings.setJavaScriptEnabled(true);
        settings.setPluginState(WebSettings.PluginState.ON);

        settings.setAllowContentAccess(true);
        settings.setAllowFileAccess(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            settings.setMediaPlaybackRequiresUserGesture(false);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            settings.setAllowFileAccessFromFileURLs(true);
            settings.setAllowUniversalAccessFromFileURLs(true);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            CookieManager.getInstance().setAcceptThirdPartyCookies(GovWebView.this, true);
            CookieManager.getInstance().flush();
        }

        setWebChromeClient(new WebChromeClient());
    }

    @Override
    public void loadUrl(String url) {
        Log.v(MainApplication.TAG, "==========================================");
        Log.v(MainApplication.TAG, url + "");
        super.loadUrl(url);
    }
}
