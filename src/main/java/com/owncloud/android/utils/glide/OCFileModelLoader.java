package com.owncloud.android.utils.glide;

import android.support.annotation.NonNull;
import android.util.Log;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.FileLoader;
import com.bumptech.glide.signature.ObjectKey;
import com.owncloud.android.datamodel.OCFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class OCFileModelLoader extends FileLoader<OCFile> {
    private static final String TAG = OCFileModelLoader.class.getSimpleName();
    private FileOpener fileOpener;

    public OCFileModelLoader(FileOpener<OCFile> fileOpener) {
        super(fileOpener);

        this.fileOpener = fileOpener;
    }

    @Override
    public boolean handles(@NonNull File model) {
        return true;
    }

    @Override
    public LoadData<OCFile> buildLoadData(@NonNull File model, int width, int height, @NonNull Options options) {
        return new LoadData<OCFile>(new ObjectKey(model), new FileFetcher<OCFile>(model, fileOpener));
    }

    private static final class FileFetcher<OCFile> implements DataFetcher<com.owncloud.android.datamodel.OCFile> {
        private final File file;
        private final FileOpener<com.owncloud.android.datamodel.OCFile> opener;
        private com.owncloud.android.datamodel.OCFile data;

        FileFetcher(File file, FileOpener<com.owncloud.android.datamodel.OCFile> opener) {
            this.file = file;
            this.opener = opener;
        }

        @Override
        public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super com.owncloud.android.datamodel.OCFile> callback) {
            try {
                data = opener.open(file);
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
                    opener.close(data);
                } catch (IOException e) {
                    // Ignored.
                }
            }
        }

        @Override
        public void cancel() {
            // Do nothing.
        }

        @NonNull
        @Override
        public Class<com.owncloud.android.datamodel.OCFile> getDataClass() {
            return opener.getDataClass();
        }

        @NonNull
        @Override
        public DataSource getDataSource() {
            return DataSource.LOCAL;
        }
    }
}
