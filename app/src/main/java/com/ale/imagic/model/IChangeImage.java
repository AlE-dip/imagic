package com.ale.imagic.model;

import org.opencv.core.Mat;

public interface IChangeImage {
    public boolean Filter(Mat mat, Mat dst, ConfigFilter configFilter);
}
