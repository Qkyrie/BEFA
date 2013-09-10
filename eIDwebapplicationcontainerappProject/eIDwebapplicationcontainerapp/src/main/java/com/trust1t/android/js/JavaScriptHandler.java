package com.trust1t.android.js;

import com.trust1t.android.WebViewActivity;

/**
 * Created by KwintenP on 2/09/13.
 */
public class JavaScriptHandler {

    WebViewActivity parentActivity;

    public JavaScriptHandler(WebViewActivity activity) {
        parentActivity = activity;
    }

    public void callToAndroidApp(int val){
        parentActivity.changeText("test" + val);
    }

}