package com.owncloud.android.utils.glide;

import android.support.annotation.NonNull;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;
import com.owncloud.android.lib.common.utils.Log_OC;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class FileFetcher implements DataFetcher<InputStream> {
    private final static String TAG = FileFetcher.class.getSimpleName();
    private String storagePath;
    private InputStream data;

    public FileFetcher(String storagePath) {
        this.storagePath = storagePath;
    }

    @Override
    public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super InputStream> callback) {
        try {
            data = new FileInputStream(storagePath);
        } catch (FileNotFoundException e) {
            Log_OC.d(TAG, "Failed to open file", e);
            callback.onLoadFailed(e);
            return;
        }
        callback.onDataReady(data);
    }

    @Override
    public void cleanup() {
        if (data != null) {
            try {
                data.close();
            } catch (IOException e) {
                // Ignored.
            }
        }
    }

    @Override
    public void cancel() {

    }

    @NonNull
    @Override
    public Class<InputStream> getDataClass() {
        return InputStream.class;
    }

    @NonNull
    @Override
    public DataSource getDataSource() {
        return DataSource.LOCAL;
    }
}
