package com.owncloud.android.utils.glide;

import com.bumptech.glide.signature.ObjectKey;

import java.io.InputStream;

public class GlideAvatar {
    private ObjectKey key;
    private InputStream inputStream;

    public GlideAvatar(ObjectKey key, InputStream avatar) {
        this.key = key;
        this.inputStream = avatar;
    }

    public ObjectKey getKey() {
        return key;
    }

    public InputStream getInputStream() {
        return inputStream;
    }
}

