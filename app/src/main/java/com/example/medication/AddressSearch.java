package com.example.medication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AddressSearch extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_search);

        webView = findViewById(R.id.webView);

        // webView 대신 최상단 루트 뷰(android.R.id.content)에 Insets를 적용해야 완벽하게 상단바에서 밀려납니다.
        View rootView = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

        webView.addJavascriptInterface(new KakaoJavaScriptInterface(), "Android");

        webView.setWebViewClient(new WebViewClient());

        webView.loadUrl("https://sonsehwan.github.io/Kakao_address/kakao_address.html");
    }

    private class KakaoJavaScriptInterface {
        @JavascriptInterface
        public void processAddress(final String address, final String zonecode){
            Intent intent = new Intent();
            intent.putExtra("address_result", address);
            intent.putExtra("selectedZonecode", zonecode);

            setResult(RESULT_OK, intent);
            finish();
        }
    }
}