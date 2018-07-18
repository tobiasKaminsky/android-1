package com.owncloud.android.utils.glide;

import android.accounts.Account;
import android.content.Context;

import com.bumptech.glide.signature.ObjectKey;
import com.owncloud.android.datamodel.ArbitraryDataProvider;
import com.owncloud.android.datamodel.OCFile;
import com.owncloud.android.lib.resources.files.TrashbinFile;

import java.io.File;

public class GlideKey {
    private static final String AVATAR = "AVATAR";
    private static final String THUMBNAIL = "THUMBNAIL_";
    private static final String RESIZED_IMAGE = "RESIZED_IMAGE_";

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

    public static ObjectKey avatar(Account account, String userId, Context context) {
        ArbitraryDataProvider arbitraryDataProvider = new ArbitraryDataProvider(context.getContentResolver());

        String serverName = account.name.substring(account.name.lastIndexOf('@') + 1, account.name.length());
        String eTag = arbitraryDataProvider.getValue(userId + "@" + serverName, GlideKey.AVATAR);

        return new ObjectKey("a_" + userId + "_" + serverName + "_" + eTag);
    }
}
