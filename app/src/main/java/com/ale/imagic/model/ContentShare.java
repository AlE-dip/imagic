package com.ale.imagic.model;

public class ContentShare {
    private static long maxId = 0;

    public static synchronized long getMaxId() {
        maxId++;
        return maxId;
    }
}
