package com.owncloud.android.utils.glide;

import com.bumptech.glide.load.model.GlideUrl;

import java.net.URL;

public class GlideOcFile extends GlideUrl {
    public GlideOcFile(URL url) {
        super(url);
    }

    @Override
    public String getCacheKey() {
        return super.getCacheKey();
    }
}
