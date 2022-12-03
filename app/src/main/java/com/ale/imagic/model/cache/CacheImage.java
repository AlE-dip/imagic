package com.ale.imagic.model.cache;

import android.graphics.Bitmap;

import com.ale.imagic.model.ContentShare;

public class CacheImage {
    private long id;
    private String path;
    private Bitmap bitmap;

    public CacheImage(String path) {
        this.id = ContentShare.getMaxId();
        this.path = path;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
