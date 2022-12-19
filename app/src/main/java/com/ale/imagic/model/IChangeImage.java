package com.ale.imagic.model;

import org.opencv.core.Mat;

public interface IChangeImage {
    public Mat Filter(Mat mat, ConfigFilter configFilter);
}
