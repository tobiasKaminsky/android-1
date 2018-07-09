package com.owncloud.android.utils.glide;

import android.support.annotation.NonNull;

import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;

import java.io.InputStream;

public class GlideContainerModelLoaderFactory implements ModelLoaderFactory<GlideContainer, InputStream> {

    @NonNull
    @Override
    public ModelLoader<GlideContainer, InputStream> build(@NonNull MultiModelLoaderFactory unused) {
        return new GlideContainerStreamLoader();
    }

    @Override
    public void teardown() {
        // Do nothing.
    }
}
