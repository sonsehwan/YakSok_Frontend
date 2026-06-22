package com.example.medication;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

public class AddressSearch extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_search);

        webView = findViewById(R.id.webView);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

        webView.addJavascriptInterface(new KakaoJavaScriptInterface(), "Android");

        webView.setWebViewClient(new WebViewClient());

        webView.loadUrl("https://github.com/sonsehwan/Kakao_address/blob/main/kakao_address.html");
    }

    private class KakaoJavaScriptInterface {
        @JavascriptInterface
        public void processAddress(final String address, final String zonecode){
            Intent intent = new Intent();
            intent.putExtra("slectedAddress", address);
            intent.putExtra("selectedZonecode", zonecode);

            setResult(RESULT_OK, intent);
            finish();
        }
    }
}