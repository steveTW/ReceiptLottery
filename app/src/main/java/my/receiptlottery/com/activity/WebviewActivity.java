package my.receiptlottery.com.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import my.receiptlottery.com.R;

public class WebviewActivity extends Activity {

    private WebView mWebView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        String urlString = "";

        try{ /*get Intent data*/
            Intent intent = this.getIntent();

            urlString = intent.getStringExtra("url") != null ? intent.getStringExtra("url") : "";
        } catch(Exception e) {
            e.printStackTrace();
        }

        mWebView = (WebView) findViewById(R.id.webview);

        WebSettings websettings = mWebView.getSettings();
        websettings.setSupportZoom(true);  //啟用內置縮放功能
        websettings.setBuiltInZoomControls(true); //+/- 縮放功能
        websettings.setUseWideViewPort(true); //雙點擊顯示廣視角
        websettings.setLoadWithOverviewMode(true);//雙點擊顯示詳情
        websettings.setLoadsImagesAutomatically(true);

        mWebView.setWebViewClient(webviewClient);

        if (!urlString.equals(""))
            mWebView.loadUrl(urlString);
        else
            mWebView.loadUrl("http://invoice.etax.nat.gov.tw/");
    }

    private WebViewClient webviewClient = new WebViewClient() {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }
    };
}
