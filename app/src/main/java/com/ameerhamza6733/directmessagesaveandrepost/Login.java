package com.ameerhamza6733.directmessagesaveandrepost;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.ameerhamza6733.directmessagesaveandrepost.utils.Constants;
import com.ameerhamza6733.directmessagesaveandrepost.utils.CookieUtils;
import com.google.firebase.analytics.FirebaseAnalytics;


public final class Login extends AppCompatActivity implements  CompoundButton.OnCheckedChangeListener {
    private final WebChromeClient webChromeClient = new WebChromeClient();
    private String webViewUrl, defaultUserAgent;

    private WebView webView;
    private TextView tvLoadUrl;
    private ProgressBar progressBar;
    private boolean ready = false;
    private FirebaseAnalytics firebaseAnalytics=FirebaseAnalytics.getInstance(this);
    private Bundle bundle=new Bundle();
    private final WebViewClient webViewClient = new WebViewClient() {
        @Override
        public void onPageStarted(final WebView view, final String url, final Bitmap favicon) {

            webViewUrl = url;
            tvLoadUrl.setText(url);
            progressBar.setVisibility(View.VISIBLE);
            bundle.putString("url",url);
            firebaseAnalytics.logEvent("onPageStarted",bundle);
        }



        @Override
        public void onPageFinished(final WebView view, final String url) {
            webViewUrl = url;
            progressBar.setVisibility(View.INVISIBLE);

            firebaseAnalytics.logEvent("onPageFinished",bundle);
            final String mainCookie = CookieUtils.getCookie(url);
            if (TextUtils.isEmpty(mainCookie) || !mainCookie.contains("; ds_user_id=")) {
                ready = true;
                return;
            }
            if (mainCookie.contains("; ds_user_id=") && ready) {
                firebaseAnalytics.logEvent("cookieGotIt",bundle);
                returnCookieResult(mainCookie);
            }else {
                firebaseAnalytics.logEvent("failToGetCookie",bundle);
            }
        }
    };


    private void returnCookieResult(final String mainCookie) {
        final Intent intent = new Intent();
        intent.putExtra("cookie", mainCookie);
        Log.i("webview", "" + mainCookie);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        webView=findViewById(R.id.webview);
        tvLoadUrl=findViewById(R.id.tvUrl);
        progressBar=findViewById(R.id.progress_bar);
        initWebView();


    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
        final WebSettings webSettings = webView.getSettings();

        final String newUserAgent = isChecked
                ? "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.61 Safari/537.36"
                : defaultUserAgent;

        webSettings.setUserAgentString(newUserAgent);
        webSettings.setUseWideViewPort(isChecked);
        webSettings.setLoadWithOverviewMode(isChecked);
        webSettings.setSupportZoom(isChecked);
        webSettings.setBuiltInZoomControls(isChecked);

        webView.loadUrl("https://www.instagram.com/accounts/login/");
        tvLoadUrl.setText("https://www.instagram.com/accounts/login/");

    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView() {

            webView.setWebChromeClient(webChromeClient);
            webView.setWebViewClient(webViewClient);

            final WebSettings webSettings = webView.getSettings();
            if (webSettings != null) {
                if (defaultUserAgent == null) defaultUserAgent = webSettings.getUserAgentString();
                webSettings.setJavaScriptEnabled(true);
                webSettings.setDomStorageEnabled(true);
                webSettings.setSupportZoom(true);
                webSettings.setBuiltInZoomControls(true);
                webSettings.setDisplayZoomControls(false);
                webSettings.setLoadWithOverviewMode(true);
                webSettings.setUseWideViewPort(true);
                webSettings.setAllowFileAccessFromFileURLs(true);
                webSettings.setAllowUniversalAccessFromFileURLs(true);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                CookieManager.getInstance().removeAllCookies(null);
                CookieManager.getInstance().flush();
            } else {
                CookieSyncManager cookieSyncMngr = CookieSyncManager.createInstance(getApplicationContext());
                cookieSyncMngr.startSync();
                CookieManager cookieManager = CookieManager.getInstance();
                cookieManager.removeAllCookie();
                cookieManager.removeSessionCookie();
                cookieSyncMngr.stopSync();
                cookieSyncMngr.sync();
            }
            webView.loadUrl("https://www.instagram.com/accounts/login/");
        tvLoadUrl.setText("https://www.instagram.com/accounts/login/");

    }

    @Override
    protected void onPause() {
        if (webView != null) webView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) webView.onResume();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) webView.destroy();
        super.onDestroy();
    }
}