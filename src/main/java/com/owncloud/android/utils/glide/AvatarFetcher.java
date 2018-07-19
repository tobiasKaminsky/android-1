package com.owncloud.android.utils.glide;

import android.support.annotation.NonNull;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;

import java.io.InputStream;

public class AvatarFetcher implements DataFetcher<InputStream> {
    private final static String TAG = AvatarFetcher.class.getSimpleName();
    private GlideAvatar avatar;

    public AvatarFetcher(GlideAvatar avatar) {
        this.avatar = avatar;
    }

    @Override
    public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super InputStream> callback) {
        callback.onDataReady(avatar.getInputStream());
    }

    @Override
    public void cleanup() {

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
        return DataSource.REMOTE;
    }
}
