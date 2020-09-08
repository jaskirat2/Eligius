package in.techaddicts.eligius;

import androidx.appcompat.app.AppCompatActivity;


import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class FacebookActivity extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facebook);

        webView = (WebView) findViewById(R.id.id_webview3);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("https://www.facebook.com/jaskirats25d");

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