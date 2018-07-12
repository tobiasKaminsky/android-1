package com.owncloud.android.utils.glide;

import android.support.annotation.NonNull;

import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.owncloud.android.datamodel.OCFile;

import java.io.InputStream;

public class OCFileModelLoaderFactory implements ModelLoaderFactory<OCFile, InputStream> {

    @NonNull
    @Override
    public ModelLoader<OCFile, InputStream> build(@NonNull MultiModelLoaderFactory unused) {
        return new OCFileModelLoader();
    }

    @Override
    public void teardown() {
        // Do nothing.
    }
}
