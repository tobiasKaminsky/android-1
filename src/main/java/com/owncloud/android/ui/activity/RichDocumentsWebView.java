package com.owncloud.android.ui.activity;

import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.owncloud.android.R;
import com.owncloud.android.lib.common.utils.Log_OC;

public class RichDocumentsWebView extends ExternalSiteWebView {

    private static final String TAG = RichDocumentsWebView.class.getSimpleName();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.collabora_menu, menu);

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webview.addJavascriptInterface(new AvatarBridge(), "AvatarHandler");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
    }

    private class AvatarBridge {
        @JavascriptInterface
        public void postmessage(String string) {
            Log_OC.d(TAG, "avatar: " + string);
        }
    }
}
