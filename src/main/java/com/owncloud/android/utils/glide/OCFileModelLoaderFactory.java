package com.owncloud.android.utils.glide;

import android.support.annotation.NonNull;

import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;

import java.io.InputStream;

public class OCFileModelLoaderFactory implements ModelLoaderFactory<GlideOcFile, InputStream> {

    @NonNull
    @Override
    public ModelLoader<GlideOcFile, InputStream> build(@NonNull MultiModelLoaderFactory unused) {
        return new OCFileModelLoader();
    }

    @Override
    public void teardown() {
        // Do nothing.
    }
}
