package com.example.downloadfile;

import android.net.Uri;

import java.io.File;

public class LyricsFile {
    private final long _id;
    private final Uri uri;
    private final String name;

    public LyricsFile(long _id, Uri uri, String name) {
        this._id = _id;
        this.uri = uri;
        this.name = name;
    }

    public long getId() { return _id; }

    public Uri getUri() { return uri; }

    public String getPath() {
        File file = new File(this.uri.getPath());
        final String[] split = file.getAbsolutePath().split(":");
        String filePath = split[0];
        return filePath;
    }

    public String getName() {
        return name;
    }
}
