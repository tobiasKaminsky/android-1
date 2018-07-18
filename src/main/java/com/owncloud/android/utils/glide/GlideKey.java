package com.owncloud.android.utils.glide;

import com.bumptech.glide.signature.ObjectKey;
import com.owncloud.android.datamodel.OCFile;
import com.owncloud.android.lib.resources.files.TrashbinFile;

import java.io.File;

public class GlideKey {
    private static final String THUMBNAIL = "THUMBNAIL_";
    private static final String RESIZED_IMAGE = "RESIZED_IMAGE_";
    private static final String AVATAR = "AVATAR";

    public static ObjectKey serverThumbnail(OCFile file) {
        return new ObjectKey(THUMBNAIL + file.getEtagOnServer()); // TODO if null, show placeholder
    }

    public static ObjectKey resizedImage(OCFile file) {
        return new ObjectKey(RESIZED_IMAGE + file.getEtagOnServer());
    }

    public static ObjectKey localFile(File file) {
        return new ObjectKey(file.hashCode());
    }

    public static ObjectKey url(String url) {
        return new ObjectKey(url);
    }

    public static ObjectKey trashbinThumbnail(TrashbinFile file) {
        return new ObjectKey(THUMBNAIL + file.getRemoteId());
    }

    public static ObjectKey activityThumbnail(OCFile file) {
        return new ObjectKey(THUMBNAIL + file.getRemoteId()); // TODO if null, show placeholder
    }

//    public static ObjectKey avatar() {
//        return new ObjectKey("a_" + userId + "_" + serverName + "_" + eTag);
//    }
}
