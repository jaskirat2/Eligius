package in.techaddicts.eligius;

import androidx.appcompat.app.AppCompatActivity;


import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class InstagramActivity extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instagram);

        webView = (WebView) findViewById(R.id.id_webview2);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("https://instagram.com/jaskirat_02");

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
    }

    @Override
    public void onBackPressed(){
        if(webView.canGoBack()){
            webView.goBack();
        }
        else{
            super.onBackPressed();
        }
    }
}