package com.owncloud.android.utils.glide;

import android.support.annotation.NonNull;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.ModelLoader;
import com.owncloud.android.datamodel.OCFile;

import java.io.InputStream;

public class OCFileModelLoader implements ModelLoader<GlideOcFile, InputStream> {
    private static final String TAG = OCFileModelLoader.class.getSimpleName();

    @Override
    public boolean handles(@NonNull GlideOcFile model) {
        return true;
    }

    @Override
    public LoadData<InputStream> buildLoadData(@NonNull GlideOcFile model, int width, int height, @NonNull Options options) {
        OCFile file = model.getFile();

        if (GlideOCFileType.thumbnail.equals(model.getType())) {
            String path;
            if (model.getFile().getStoragePath().isEmpty()) {
                path = model.getPath();
            } else {
                path = model.getFile().getStoragePath();
            }

            return new LoadData<>(GlideKey.serverThumbnail(file), new FileFetcher(path));
        } else {
            return new LoadData<>(GlideKey.resizedImage(file), new FileFetcher(file.getStoragePath()));
        }
    }
}
