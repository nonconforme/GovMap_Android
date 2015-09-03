package com.govmap;

import android.app.Application;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by MediumMG on 03.09.2015.
 */
public class MainApplication extends Application {

    public static String TAG = MainApplication.class.getSimpleName();

    private final static String GOV_URL = "http://www.govmap.gov.il/";

    public static final String ACTION_FINISH_SPLASH = "com.govmap.finish_splash";
    public static final String ACTION_INNER_ADDRESS = "com.govmap.inner_address";

    private GovWebView mWebView;

    @Override
    public void onCreate() {
        super.onCreate();

        mWebView = new GovWebView(this);
        mWebView.setWebViewClient(new GovWebClient());
        mWebView.addJavascriptInterface(new GovJavaScriptInterface(), "INTERFACE");
    }

    public GovWebView getWebView() {
        return mWebView;
    }

    public void loadGovSite() {
        mWebView.loadUrl(GOV_URL);
    }

    public void clearResults(){
        mWebView.loadUrl("javascript:(function() {document.getElementById('divTableResultsFromLink').innerText = '';})();");
        mWebView.loadUrl("javascript:(function() {document.getElementById('tdFSTableResultsFromLink').innerText = '';})();");
    }

    private class GovWebClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            Log.v(TAG, "onPageFinished: "+ url);
            if (GOV_URL.equals(url)) {
                sendBroadcast(new Intent(ACTION_FINISH_SPLASH));
            }
            super.onPageFinished(view, url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return false;
        }
    }

    private class GovJavaScriptInterface {

        @JavascriptInterface
        public void processContent(String aContent)
        {
            final String content = aContent.trim();

            if (!TextUtils.isEmpty(content)) {
                Intent intent = new Intent(ACTION_INNER_ADDRESS);
                intent.putExtra("data", content);
                sendBroadcast(intent);


            }
        }
    }




    public void checkInnerTextForAddress() {
        mWebView.loadUrl("javascript:window.INTERFACE.processContent(document.getElementById('tdFSTableResultsFromLink').innerText);");
    }

}
