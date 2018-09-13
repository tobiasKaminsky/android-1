package com.owncloud.android.ui.activity;

import android.accounts.Account;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.owncloud.android.R;
import com.owncloud.android.authentication.AccountUtils;
import com.owncloud.android.datamodel.OCFile;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.operations.RichDocumentsCreateAssetOperation;

public class RichDocumentsWebView extends ExternalSiteWebView {

    private static final String TAG = RichDocumentsWebView.class.getSimpleName();

    private static final int REQUEST_REMOTE_FILE = 100;
    public static final int REQUEST_LOCAL_FILE = 101;

    private ProgressBar progressBar;
    private OCFile file;

    public ValueCallback<Uri[]> uploadMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        showToolbar = false;
        webViewLayout = R.layout.richdocuments_webview;
        super.onCreate(savedInstanceState);

        progressBar = findViewById(R.id.progressBar2);

        file = getIntent().getParcelableExtra(EXTRA_FILE);

        webview.addJavascriptInterface(new RichDocumentsMobileInterface(), "RichDocumentsMobileInterface");

        webview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                webview.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                webview.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);

                super.onPageFinished(view, url);
            }
        });

        webview.setWebChromeClient(new WebChromeClient() {
            RichDocumentsWebView activity = RichDocumentsWebView.this;

            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback,
                                             FileChooserParams fileChooserParams) {
                // make sure there is no existing message
                if (uploadMessage != null) {
                    uploadMessage.onReceiveValue(null);
                    uploadMessage = null;
                }

                activity.uploadMessage = filePathCallback;

                Intent intent = fileChooserParams.createIntent();
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                try {
                    activity.startActivityForResult(intent, REQUEST_LOCAL_FILE);
                } catch (ActivityNotFoundException e) {
                    uploadMessage = null;
                    Toast.makeText(getBaseContext(), "Cannot open file chooser", Toast.LENGTH_LONG).show();
                    return false;
                }

                return true;
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    private void openFileChooser() {
        Intent action = new Intent(this, FilePickerActivity.class);
        startActivityForResult(action, REQUEST_REMOTE_FILE);
    }

    private void openShareDialog() {
        Intent intent = new Intent(this, ShareActivity.class);
        intent.putExtra(FileActivity.EXTRA_FILE, file);
        intent.putExtra(FileActivity.EXTRA_ACCOUNT, getAccount());
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (RESULT_OK != resultCode) {
            // TODO 
            return;
        }

        switch (requestCode) {
            case REQUEST_LOCAL_FILE:
                handleLocalFile(data, resultCode);
                break;

            case REQUEST_REMOTE_FILE:
                handleRemoteFile(data);
                break;

            default:
                // unexpected, do nothing
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void handleLocalFile(Intent data, int resultCode) {
        if (uploadMessage == null) {
            return;
        }

        uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
        uploadMessage = null;
    }

    private void handleRemoteFile(Intent data) {
        OCFile file = data.getParcelableExtra(FolderPickerActivity.EXTRA_FILES);

        new Thread(() -> {
            Account account = AccountUtils.getCurrentOwnCloudAccount(this);
            RichDocumentsCreateAssetOperation operation = new RichDocumentsCreateAssetOperation(file.getRemotePath());
            RemoteOperationResult result = operation.execute(account, this);

            if (result.isSuccess()) {
                String asset = (String) result.getData().get(0);

                runOnUiThread(() -> webview.evaluateJavascript("OCA.RichDocuments.documentsMain.postAsset('" +
                        file.getFileName() + "', '" + asset + "');", null));
            } else {
                // todo
            }
        }).start();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(EXTRA_URL, url);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        url = savedInstanceState.getString(EXTRA_URL);
        super.onRestoreInstanceState(savedInstanceState);
    }

    private class RichDocumentsMobileInterface {
        @JavascriptInterface
        public void close() {
            runOnUiThread(RichDocumentsWebView.this::finish);
        }

        @JavascriptInterface
        public void insertGraphic() {
//            openFileChooser();

            openShareDialog();
        }
    }
}
