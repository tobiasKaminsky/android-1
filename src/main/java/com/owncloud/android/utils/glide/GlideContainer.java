package com.owncloud.android.utils.glide;

import com.bumptech.glide.load.Key;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.resources.files.ServerFileInterface;

public class GlideContainer {
    public ServerFileInterface file;
    public OwnCloudClient client;
    public Key key;
    public String type;
    public String url;
}
