package com.ale.imagic.model.cache;

import android.graphics.Bitmap;

import com.ale.imagic.model.ConfigFilter;
import com.ale.imagic.model.ContentShare;
import com.ale.imagic.model.IChangeImage;

public class CacheFilter {
    private long id;
    private String name;
    private Bitmap bitmap;
    private ConfigFilter configFilter;
    private IChangeImage changeImage;


    public CacheFilter(String name, ConfigFilter configFilter, IChangeImage changeImage) {
        this.id = ContentShare.getMaxId();
        this.name = name;
        this.configFilter = configFilter;
        this.changeImage = changeImage;
    }

    public CacheFilter() {
        this.id = 0;
        this.name = "";
        this.configFilter = null;
        this.changeImage = null;
    }

    public void setCache(CacheFilter cacheFilter) {
        id = cacheFilter.getId();
        name = cacheFilter.getName();
        configFilter = cacheFilter.getConfigFilter();
        changeImage = cacheFilter.getChangeImage();
    }

    public ConfigFilter getConfigFilter() {
        return configFilter;
    }

    public void setConfigFilter(ConfigFilter configFilter) {
        this.configFilter = configFilter;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public IChangeImage getChangeImage() {
        return changeImage;
    }

    public void setChangeImage(IChangeImage changeImage) {
        this.changeImage = changeImage;
    }
}
