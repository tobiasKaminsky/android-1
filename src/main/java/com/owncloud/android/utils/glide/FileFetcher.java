package com.owncloud.android.utils.glide;

import android.support.annotation.NonNull;
import android.util.Log;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;
import com.owncloud.android.datamodel.OCFile;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class FileFetcher implements DataFetcher<InputStream> {
    private final static String TAG = FileFetcher.class.getSimpleName();
    private OCFile file;
    private InputStream data;

    public FileFetcher(OCFile file) {
        this.file = file;
    }

    @Override
    public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super InputStream> callback) {
        try {
            data = new FileInputStream(file.getStoragePath());
        } catch (FileNotFoundException e) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Failed to open file", e);
            }
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
