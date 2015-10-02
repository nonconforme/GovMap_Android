package com.govmap;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
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
public class MainApplication extends Application implements Application.ActivityLifecycleCallbacks {

    public static String TAG = MainApplication.class.getSimpleName();

    public static String STYLE_NOT_VISIBLE = "none";

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
    private static final int TIME_FIND = 2000;

    private static final int TIME_INNERTEXT_FOR_ADDRESS = 1000;
    private static final int MAX_ATTEMPTS_ADDRESS = 9;

    private static final int TIME_INNERTEXT_FOR_CADASTRE = 3000;
    private static final int MAX_ATTEMPTS_CADASTRE = 3;

    private int attemptCount = 0;

    private Handler mStartHandler = new Handler();
    private Handler mHandlerAfterFindTime = new Handler();
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


    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) { }

    @Override
    public void onActivityStarted(Activity activity) { }

    @Override
    public void onActivityResumed(Activity activity) { }

    @Override
    public void onActivityPaused(Activity activity) { }

    @Override
    public void onActivityStopped(Activity activity) { }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) { }

    @Override
    public void onActivityDestroyed(Activity activity) { }










    public GovWebView getWebView() {
        return mWebView;
    }

    public void loadGovSite() {
        mWebView.loadUrl(GOV_URL);
    }

    private void clearResults(){
        new Handler(MainApplication.this.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl("javascript:(function() {document.getElementById('tdFSTableResultsFromLink').innerText = '';})();");
                mWebView.loadUrl("javascript:(function() {document.getElementById('divTableResultsFromLink').innerText = '';})();");
            }
        });
    }

    public void clearHandlers() {
        mStartHandler.removeCallbacksAndMessages(null);
        mHandlerAfterFindTime.removeCallbacksAndMessages(null);
        mJSHandler.removeCallbacksAndMessages(null);
        clearResults();
    }

    public void startSearchWihCadastre(String cadastre) {
        clearResults();
        mWebView.loadUrl(String.format("javascript:(function() {document.getElementById('tbSearchWord').value = '%s';})();", cadastre));
        mWebView.loadUrl("javascript:(function() {FS_Search();})();");

        mStartHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl("javascript:(function() {FSS_FindAddressForBlock();})();");
                attemptCount = 0;
                mHandlerAfterFindTime.postDelayed(mContentForAddressRunnable, TIME_INNERTEXT_FOR_ADDRESS);
            }
        }, TIME_FIND);
    }

    public void startSearchWithAddress(String address) {
        clearResults();
        mWebView.loadUrl(String.format("javascript:(function() {document.getElementById('tbSearchWord').value = '%s';})();", address));
        mWebView.loadUrl("javascript:(function() {FS_Search();})();");

        mStartHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mJSHandler.postDelayed(mJSHandlerForCadastreRunnable, (MAX_ATTEMPTS_CADASTRE + 1) * TIME_INNERTEXT_FOR_CADASTRE);
                mWebView.loadUrl("javascript:window.INTERFACE.processStyles('1', document.getElementById('trFSFindGushUrl').getAttribute('style'), document.getElementById('trFSSearchParams').getAttribute('style'));");
//                mWebView.loadUrl("javascript:window.INTERFACE.processStyles('2', document.getElementById('trFSFindGushUrl').style, document.getElementById('trFSSearchParams').style);");
//                mWebView.loadUrl("javascript:window.INTERFACE.processStyles('3', document.getElementById('trFSFindGushUrl').style, document.getElementById('trFSSearchParams').getAttribute('style'));");
//                mWebView.loadUrl("javascript:window.INTERFACE.processStyles('4', document.getElementById('trFSFindGushUrl').getAttribute('style'), document.getElementById('trFSSearchParams').style);");

//                mWebView.loadUrl("javascript:(function() {FSS_FindBlockForAddress();})();");
//                attemptCount = 0;
//                mHandlerAfterFindTime.postDelayed(mContentForCadastreRunnable, TIME_INNERTEXT_FOR_CADASTRE);
            }
        }, TIME_FIND);

    }

    private class ContentForAddressRunnable implements Runnable {
        @Override
        public void run() {
            mJSHandler.postDelayed(mJSHandlerForAddressRunnable, (MAX_ATTEMPTS_ADDRESS + 1) * TIME_INNERTEXT_FOR_ADDRESS);
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
            Log.v(MainApplication.TAG, errorCode + " "+description);
            if (errorCode == ERROR_CONNECT ||
                errorCode == ERROR_HOST_LOOKUP)
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
                mHandlerAfterFindTime.removeCallbacks(mContentForAddressRunnable);
                attemptCount = 0;
                clearResults();
                Intent intent = new Intent(ACTION_INNER_ADDRESS);
                intent.putExtra(EXTRA_DATA_ADDRESS, content);
                sendBroadcast(intent);
            }
            else {
                attemptCount++;
                if (attemptCount < MAX_ATTEMPTS_ADDRESS)
                    mHandlerAfterFindTime.postDelayed(mContentForAddressRunnable, TIME_INNERTEXT_FOR_ADDRESS);
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
                mHandlerAfterFindTime.removeCallbacks(mContentForCadastreRunnable);
                attemptCount = 0;
                new Handler(MainApplication.this.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        mWebView.loadUrl("javascript:(function() {FSS_ShowNewSearch(true);})();");
                    }
                });
                clearResults();
                Intent intent = new Intent(ACTION_INNER_CADASTRE);
                intent.putExtra(EXTRA_DATA_CADASTRE, content);
                sendBroadcast(intent);
            }
            else {
                attemptCount++;
                if (attemptCount < MAX_ATTEMPTS_CADASTRE)
                    mHandlerAfterFindTime.postDelayed(mContentForCadastreRunnable, TIME_INNERTEXT_FOR_CADASTRE);
                else
                    processContentForCadastre(BaseActivity.NO_RESULT_FOUND_HE);
            }
        }

        @JavascriptInterface
        public void processStyles(String value, String searchButtonStyle, String suggestionsTableStyle) {
            String[] parsedStringsB = searchButtonStyle.split(" ");
            String searchButtonStyleValue = parsedStringsB[parsedStringsB.length - 1].split(";")[0];

            String[] parsedStringsT = suggestionsTableStyle.split(" ");
            String suggestionsTableStyleValue = parsedStringsT[parsedStringsT.length - 1].split(";")[0];

            Log.v(MainApplication.TAG, value + " content SearchButtonStyle: '" + searchButtonStyle + "'+ value: '" +searchButtonStyleValue+"'");
            Log.v(MainApplication.TAG, value + " content SuggestionsTableStyle: '" + suggestionsTableStyle + "'+ value: '" +suggestionsTableStyleValue+"'");

            if (searchButtonStyleValue.equals(STYLE_NOT_VISIBLE) ||
                !suggestionsTableStyleValue.equals(STYLE_NOT_VISIBLE)) {
                Log.v(MainApplication.TAG, "No_results");
                processContentForCadastre(BaseActivity.NO_RESULT_FOUND_HE);
            }
            else {
                new Handler(MainApplication.this.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        mWebView.loadUrl("javascript:(function() {FSS_FindBlockForAddress();})();");
                        attemptCount = 0;
                        mHandlerAfterFindTime.postDelayed(mContentForCadastreRunnable, TIME_INNERTEXT_FOR_CADASTRE);
                    }
                });
            }
        }
    }







}
