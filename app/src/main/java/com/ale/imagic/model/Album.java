package com.ale.imagic.model;

import com.ale.imagic.model.cache.CacheImage;

import java.io.File;
import java.util.ArrayList;

public class Album {
    private int id;
    private String name;
    private ArrayList<CacheImage> cacheImages = new ArrayList<>();

    public Album(String name, File[] imageFiles) {
        this.name = name;
        for (int i = 0; i < imageFiles.length; i++) {
            CacheImage cacheImage = new CacheImage(imageFiles[i].getAbsolutePath());
            cacheImages.add(cacheImage);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ArrayList<CacheImage> getCacheImages() {
        return cacheImages;
    }

    public void setCacheImages(ArrayList<CacheImage> cacheImages) {
        this.cacheImages = cacheImages;
    }
}
