package com.ale.imagic.convertor;

import android.graphics.Bitmap;
import android.util.Size;
import android.widget.ImageView;

import com.ale.imagic.model.cache.CacheImage;

public class Handle {
    public static void loadCacheImage(CacheImage cacheImage, Size size, OnHandleLoadImage onHandleLoadImage) {
        if (cacheImage.getBitmap() == null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Bitmap bitmap = null;
                    if(size != null){
                        bitmap = Convert.resizeBitmap(cacheImage.getPath(), size.getWidth(), size.getHeight());
                    } else {
                        bitmap = Convert.readImage(cacheImage.getPath());
                    }
                    cacheImage.setBitmap(bitmap);
                    onHandleLoadImage.onFinish(bitmap);
                }
            }).start();
        } else {
            onHandleLoadImage.onFinish(cacheImage.getBitmap());
        }
    }

    public interface OnHandleLoadImage {
        public void onFinish(Bitmap bitmap);
    }
}
