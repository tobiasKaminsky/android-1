package com.owncloud.android.ui.activity;

import android.accounts.Account;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.owncloud.android.R;
import com.owncloud.android.authentication.AccountUtils;
import com.owncloud.android.datamodel.FileDataStorageManager;
import com.owncloud.android.datamodel.OCFile;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.operations.RichDocumentsCreateAssetOperation;

public class RichDocumentsWebView extends ExternalSiteWebView {

    private static final String TAG = RichDocumentsWebView.class.getSimpleName();

    private static final int FILECHOOSER_RESULTCODE = 2888;

    private ValueCallback<Uri> mUploadMessage;
    private Uri mCapturedImageURI = null;
    private ProgressBar progressBar;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.collabora_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_send_share_file) {
            openShareDialog();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    //    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // webview.evaluateJavascript("window.RichDocumentsMobileInterface.insertGraphic('http://)", null);
//        String url = "";
//        webview.evaluateJavascript("OCA.RichDocuments.documentsMain.postAsset('Coast.jpg', '" + url + "');",
//                new ValueCallback<String>() {
//                    @Override
//                    public void onReceiveValue(String value) {
//                        Log_OC.d(TAG, value);
//                    }
//                });
//
//        return true;
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        webViewLayout = R.layout.richdocuments_webview;
        super.onCreate(savedInstanceState);

        progressBar = findViewById(R.id.progressBar2);

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
    }

    private void openFileChooser() {
        Intent action = new Intent(this, FilePickerActivity.class);
//        startActivityForResult(action, FileDisplayActivity.REQUEST_CODE__MOVE_FILES);
        startActivityForResult(action, 123);
    }

    private void openShareDialog() {
        FileDataStorageManager fileDataStorageManager = new FileDataStorageManager(getAccount(), getContentResolver());
        OCFile file = fileDataStorageManager.getFileByPath("/Photos/Coast.jpg");
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

        OCFile file = data.getParcelableExtra(FolderPickerActivity.EXTRA_FILES);

        new Thread(() -> {
            Account account = AccountUtils.getCurrentOwnCloudAccount(this);
            RichDocumentsCreateAssetOperation operation = new RichDocumentsCreateAssetOperation(file.getRemotePath());
            RemoteOperationResult result = operation.execute(account, this);

            if (result.isSuccess()) {
                String asset = (String) result.getData().get(0);

                runOnUiThread(() -> webview.evaluateJavascript("OCA.RichDocuments.documentsMain.postAsset('" + file.getFileName() + "', '" + asset + "');", null));
            } else {
                // todo
            }
        }).start();

        super.onActivityResult(requestCode, resultCode, data);
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


//        @JavascriptInterface
//        public void openLocalFileChooser() {
//            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//            intent.setType("*/*");
//            intent.addCategory(Intent.CATEGORY_OPENABLE);
//
//            try {
//                startActivityForResult(
//                        Intent.createChooser(intent, "Select a File to Upload"),
//                        123);
//            } catch (android.content.ActivityNotFoundException ex) {
//                // Potentially direct the user to the Market with a Dialog
//                Toast.makeText(getApplicationContext(), "Please install a File Manager.",
//                        Toast.LENGTH_SHORT).show();
//            }
//        }

        @JavascriptInterface
        public void insertGraphic() {
            openFileChooser();
        }
    }
}
