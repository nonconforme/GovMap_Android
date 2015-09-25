package com.govmap;

import android.app.Application;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.govmap.activity.BaseActivity;
import com.govmap.view.GovWebView;

/**
 * Created by MediumMG on 03.09.2015.
 */
public class MainApplication extends Application {

    public static String TAG = MainApplication.class.getSimpleName();

    // Site
    private final static String GOV_URL = "http://www.govmap.gov.il/";

    // Intents
    public static final String ACTION_LOAD_ERROR = "com.govmap.load_error";
    public static final String ACTION_LOAD_PROGRESS = "com.govmap.load_progress";
    public static final String ACTION_FINISH_SPLASH = "com.govmap.finish_splash";
    public static final String ACTION_INNER_ADDRESS = "com.govmap.inner_address";
    public static final String ACTION_INNER_CADASTRE = "com.govmap.inner_cadastre";

    // Extra keys
    public static final String EXTRA_DATA_LOAD_PROGRESS = "data_load_progress";

    public static final String EXTRA_DATA_CADASTRE = "data_cadastre";

    public static final String EXTRA_DATA_ADDRESS = "data_address";

    public static final String EXTRA_DATA_OBJECT = "data_object";
    public static final String EXTRA_DATA_SEARCH_TYPE = "data_type";


    // Search
    private static final int TIME_FIND = 1000;
    private static final int TIME_INNERTEXT_FOR_ADDRESS = 1000;
    private static final int TIME_INNERTEXT_FOR_CADASTRE = 3000;
    private static final int MAX_ATTEMPTS_ADDRESS = 10;
    private static final int MAX_ATTEMPTS_CADASTRE = 4;
    private int attemptCount = 0;

    private Handler mHandler = new Handler();
    private Handler mJSHandler = new Handler();

    private Runnable mContentForAddressRunnable = new ContentForAddressRunnable();
    private Runnable mContentForCadastreRunnable = new ContentForCadastreRunnable();
    private Runnable mJSHandlerForAddressRunnable = new JSHandlerForAddressRunnable();
    private Runnable mJSHandlerForCadastreRunnable = new JSHandlerForCadastreRunnable();

    private GovWebView mWebView;

    private GovJavaScriptInterface mInterface = new GovJavaScriptInterface();

    @Override
    public void onCreate() {
        super.onCreate();

        mWebView = new GovWebView(this);
        mWebView.setWebChromeClient(new GovWebChromeClient());
        mWebView.setWebViewClient(new GovWebClient());
        mWebView.addJavascriptInterface(mInterface, "INTERFACE");
    }

    public GovWebView getWebView() {
        return mWebView;
    }

    public void loadGovSite() {
        mWebView.loadUrl(GOV_URL);
    }

    public void clearResults(){
        mWebView.loadUrl("javascript:(function() {document.getElementById('tdFSTableResultsFromLink').innerText = '';})();");
        mWebView.loadUrl("javascript:(function() {document.getElementById('divTableResultsFromLink').innerText = '';})();");
    }

    public void startSearchWihCadastre(String cadastre) {
        mWebView.loadUrl(String.format("javascript:(function() {document.getElementById('tbSearchWord').value = '%s';})();", cadastre));
        mWebView.loadUrl("javascript:(function() {FS_Search();})();");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl("javascript:(function() {FSS_FindAddressForBlock();})();");
                attemptCount = 0;
                mHandler.postDelayed(mContentForAddressRunnable, TIME_INNERTEXT_FOR_ADDRESS);
            }
        }, TIME_FIND);
    }

    public void startSearchWithAddress(String address) {
        mWebView.loadUrl(String.format("javascript:(function() {document.getElementById('tbSearchWord').value = '%s';})();", address));
        mWebView.loadUrl("javascript:(function() {FS_Search();})();");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl("javascript:(function() {FSS_FindBlockForAddress();})();");
                attemptCount = 0;
                mHandler.postDelayed(mContentForCadastreRunnable, TIME_INNERTEXT_FOR_CADASTRE);
            }
        }, TIME_FIND);
    }

    private class ContentForAddressRunnable implements Runnable {
        @Override
        public void run() {
            mJSHandler.postDelayed(mJSHandlerForAddressRunnable, MAX_ATTEMPTS_ADDRESS * TIME_INNERTEXT_FOR_ADDRESS);
            mWebView.loadUrl("javascript:window.INTERFACE.processContentForAddress(document.getElementById('tdFSTableResultsFromLink').innerText);");
        }
    }

    private class JSHandlerForAddressRunnable implements Runnable {
        @Override
        public void run() {
            mInterface.processContentForAddress(BaseActivity.NO_RESULT_FOUND_HE);
        }
    }

    private class ContentForCadastreRunnable implements Runnable {
        @Override
        public void run() {
            mJSHandler.postDelayed(mJSHandlerForCadastreRunnable, MAX_ATTEMPTS_CADASTRE * TIME_INNERTEXT_FOR_CADASTRE);
            mWebView.loadUrl("javascript:window.INTERFACE.processContentForCadastre(document.getElementById('divTableResultsFromLink').innerText);");
        }
    }

    private class JSHandlerForCadastreRunnable implements Runnable {
        @Override
        public void run() {
            mInterface.processContentForCadastre(BaseActivity.NO_RESULT_FOUND_HE);
        }
    }

    private class GovWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            Intent intent = new Intent(ACTION_LOAD_PROGRESS);
            intent.putExtra(EXTRA_DATA_LOAD_PROGRESS, newProgress);
            sendBroadcast(intent);
            super.onProgressChanged(view, newProgress);
        }
    }

    private class GovWebClient extends WebViewClient {

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            if (errorCode == ERROR_CONNECT)
                sendBroadcast(new Intent(ACTION_LOAD_ERROR));
            super.onReceivedError(view, errorCode, description, failingUrl);
        }

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
        public void processContentForAddress(String aContent) {
            mJSHandler.removeCallbacks(mJSHandlerForAddressRunnable);
            String content = aContent.trim();

            Log.v(MainApplication.TAG, "content address: '" + content + "'");

            if (!TextUtils.isEmpty(content)) {
                mHandler.removeCallbacks(mContentForAddressRunnable);
                attemptCount = 0;
                Intent intent = new Intent(ACTION_INNER_ADDRESS);
                intent.putExtra(EXTRA_DATA_ADDRESS, content);
                sendBroadcast(intent);
            }
            else {
                attemptCount++;
                if (attemptCount < MAX_ATTEMPTS_ADDRESS)
                    mHandler.postDelayed(mContentForAddressRunnable, TIME_INNERTEXT_FOR_ADDRESS);
                else
                    processContentForAddress(BaseActivity.NO_RESULT_FOUND_HE);
            }
        }

        @JavascriptInterface
        public void processContentForCadastre(String aContent) {
            mJSHandler.removeCallbacks(mJSHandlerForCadastreRunnable);

            String content = aContent.trim();

            Log.v(MainApplication.TAG, "content block: '" + content + "'");

            if (!TextUtils.isEmpty(content)) {
                mHandler.removeCallbacks(mContentForCadastreRunnable);
                attemptCount = 0;
                Intent intent = new Intent(ACTION_INNER_CADASTRE);
                intent.putExtra(EXTRA_DATA_CADASTRE, content);
                sendBroadcast(intent);
            }
            else {
                attemptCount++;
                if (attemptCount < MAX_ATTEMPTS_CADASTRE)
                    mHandler.postDelayed(mContentForCadastreRunnable, TIME_INNERTEXT_FOR_CADASTRE);
                else
                    processContentForCadastre(BaseActivity.NO_RESULT_FOUND_HE);
            }
        }
    }







}
