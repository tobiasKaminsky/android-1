package com.owncloud.android.ui.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.webkit.JavascriptInterface;

import com.owncloud.android.R;

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

        webview.addJavascriptInterface(new RichDocumentsMobileInterface(), "RichDocumentsMobileInterface");
    }

    private class RichDocumentsMobileInterface {
        @JavascriptInterface
        public void close() {
            runOnUiThread(new Runnable() {
                              @Override
                              public void run() {
                                  finish();
                              }
                          }
            );
        }
    }
}
