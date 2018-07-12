package com.owncloud.android.utils.glide;

import android.support.annotation.NonNull;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.ModelLoader;
import com.owncloud.android.datamodel.OCFile;

import java.io.InputStream;

public class OCFileModelLoader implements ModelLoader<OCFile, InputStream> {
    private static final String TAG = OCFileModelLoader.class.getSimpleName();

    @Override
    public boolean handles(@NonNull OCFile model) {
        return true;
    }

    @Override
    public LoadData<InputStream> buildLoadData(@NonNull OCFile model, int width, int height, @NonNull Options options) {
        return new LoadData<>(GlideKey.resizedImage(model), new FileFetcher(model));
    }
}
