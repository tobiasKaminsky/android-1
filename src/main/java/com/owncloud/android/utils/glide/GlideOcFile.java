package com.owncloud.android.utils.glide;

import com.owncloud.android.datamodel.OCFile;

public class GlideOcFile {
    private OCFile file;
    private GlideOCFileType type;
    private String path = "";

    public GlideOcFile(OCFile file, GlideOCFileType type) {
        this.file = file;
        this.type = type;
    }

    public GlideOcFile(OCFile file, GlideOCFileType type, String path) {
        this.file = file;
        this.type = type;
        this.path = path;
    }

    public OCFile getFile() {
        return file;
    }

    public GlideOCFileType getType() {
        return type;
    }

    public String getPath() {
        return path;
    }
}

