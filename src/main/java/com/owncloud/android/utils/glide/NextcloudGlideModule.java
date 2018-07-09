package com.owncloud.android.utils.glide;

import android.content.Context;
import android.graphics.drawable.PictureDrawable;
import android.support.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;
import com.caverock.androidsvg.SVG;
import com.owncloud.android.utils.svg.SvgDecoder;
import com.owncloud.android.utils.svg.SvgDrawableTranscoder;

import java.io.InputStream;

/**
 * Module for generating api.
 */
@GlideModule
public class NextcloudGlideModule extends AppGlideModule {
    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        registry.prepend(GlideContainer.class, InputStream.class, new GlideContainerModelLoaderFactory());
        registry.prepend(String.class, InputStream.class, new StringModelLoaderFactory());
        registry.register(SVG.class, PictureDrawable.class, new SvgDrawableTranscoder())
                .append(InputStream.class, SVG.class, new SvgDecoder());
    }

    // Disable manifest parsing to avoid adding similar modules twice.
    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }
}
