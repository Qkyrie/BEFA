package com.trust1t.android;

import android.util.Log;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.webkit.WebViewClient;
import android.widget.Toast;
import android.webkit.WebView;

import com.trust1t.android.config.Configuration;
import com.trust1t.android.js.JavaScriptHandler;

public class WebViewActivity extends Activity {

    /** -------- VARIABLES --------- **/
    private WebView myWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        //Load the webview
        myWebView = (WebView)this.findViewById(R.id.myWebView);
        myWebView.setWebViewClient(new WebViewClient());
        myWebView.getSettings().setJavaScriptEnabled(true);
        //Add our javascripthandler to the webview
        myWebView.addJavascriptInterface(new JavaScriptHandler(this), "MyHandler");
        //Load our test url
        myWebView.loadUrl("http://192.168.1.100:8080/AndroidContainerWebviewTest");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.web_view, menu);
        return true;
    }

    /** -------- JAVASCRIPT HANDLING METHODS -------- **/
    public void javascriptCallFinished(int val){
        Toast.makeText(this, "Callback got val: " + val, 5).show();
    }

    public void changeText(String someText){
        Log.v(Configuration.EID_WEBVIEW_CONTAINER_LOG_FILTER, "changeText is called");
    }
    
}
