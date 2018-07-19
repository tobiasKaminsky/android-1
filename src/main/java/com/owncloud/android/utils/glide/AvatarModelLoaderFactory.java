package com.owncloud.android.utils.glide;

import android.support.annotation.NonNull;

import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;

import java.io.InputStream;

public class AvatarModelLoaderFactory implements ModelLoaderFactory<GlideAvatar, InputStream> {

    @NonNull
    @Override
    public ModelLoader<GlideAvatar, InputStream> build(@NonNull MultiModelLoaderFactory unused) {
        return new AvatarLoader();
    }

    @Override
    public void teardown() {
        // Do nothing.
    }
}
