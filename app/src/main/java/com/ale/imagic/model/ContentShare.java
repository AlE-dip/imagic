package com.ale.imagic.model;

import android.graphics.Bitmap;

import org.opencv.core.Mat;

public class ContentShare {
    private static long maxId = 0;

    public static synchronized long getMaxId() {
        maxId++;
        return maxId;
    }

    //image after apply filter

}
