package com.ale.imagic.convertor;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class Filter {

    //f1 = 0, f2 = 1, f3 = 2
    public static void deleteColor(Mat src, int BGR) {
        //Xoa mau
        //B=0;G=1;R=2 ung vs BGR
        for (int i = 0; i < src.rows(); i++) {
            for (int j = 0; j < src.cols(); j++) {
                double point[] = src.get(i, j);
                point[BGR] = 0;
                src.put(i, j, point);
            }
        }
    }

    public static Mat tableColor() {
        Mat M;
        M = Mat.zeros(1000, 2000, CvType.CV_8UC3);
        int[] color = new int[]{0, 0, 0};
        int h = 0;
        for (int i = 0; i < M.rows(); i++) {
            for (int j = 0; j < M.cols(); j++) {
                h = j / 10;
                for (int k = 0; k < 3; k++) {
                    color[k] = h % 4;
                    h /= 4;
                    if (h == 0) {
                        break;
                    }
                }
                double[] point = M.get(i, j);
                point[0] = color[0] * 64 + 32;
                point[1] = color[1] * 64 + 32;
                point[2] = color[2] * 64 + 32;
                M.put(i, j, point);

                color[0] = 0;
                color[1] = 0;
                color[2] = 0;
            }
        }
        return M;
    }

    //f4 = 100
    public static void imageBinary(Mat img, int threshold) {
        for (int i = 0; i < img.rows(); i++) {
            for (int j = 0; j < img.cols(); j++) {
                if ((img.get(i, j)[0] + img.get(i, j)[1] + img.get(i, j)[2]) / 3 < threshold) {

                    double[] point = img.get(i, j);
                    point[0] = 0;
                    point[1] = 0;
                    point[2] = 0;
                    img.put(i, j, point);
                } else {
                    double[] point = img.get(i, j);
                    point[0] = 255;
                    point[1] = 255;
                    point[2] = 255;
                    img.put(i, j, point);
                }
            }
        }
    }

    //f5
    public void imageContours(Mat img) {
        imageBinary(img, 80);
        //Note: img la anh nhi phan
        Mat imgt = new Mat(img.rows(), img.cols(), CvType.CV_8UC3);
        for (int i = 0; i < img.rows(); i++) {
            for (int j = 0; j < img.cols(); j++) {
                int gt = j + 1;
                if (gt < img.cols()) {
                    int in = (int) img.get(i, j)[0];
                    int af = (int) img.get(i, gt)[0];
                    double[] point = imgt.get(i, j);
                    if (in < af || in > af) {
                        point[0] = 0;
                        point[1] = 0;
                        point[2] = 0;
                    } else {
                        point[0] = 255;
                        point[1] = 255;
                        point[2] = 255;
                    }
                    img.put(i, j, point);
                }
            }
        }
    }

    //f6
    public void imageGray(Mat img) {
        for (int i = 0; i < img.rows(); i++) {
            for (int j = 0; j < img.cols(); j++) {
                double[] point = img.get(i, j);
                int avg = (int) ((point[0] + point[1] + point[2]) / 3);
                point[0] = avg;
                point[1] = avg;
                point[2] = avg;
                img.put(i, j, point);
            }
        }
    }

    //f7 = 2 = 4
    public static void lightBalance(Mat img, int alpha, int beta) {
        for (int i = 0; i < img.rows(); i++) {
            for (int j = 0; j < img.cols(); j++) {
                double[] point = img.get(i, j);
                point[0] = alpha * point[0] + beta;
                point[1] = alpha * point[1] + beta;
                point[2] = alpha * point[2] + beta;
                img.put(i, j, point);
            }
        }
    }

    public static void filter1(Mat src) {
        Imgproc.filter2D(src, src, src.depth(), kernel1());
    }

    //f8
    public static void filter2(Mat src) {
        Imgproc.filter2D(src, src, src.depth(), kernel2());
    }

    private static Mat kernel2() {
        Mat kernel = new Mat(3, 3, CvType.CV_16S);
        kernel.put(0, 0,
                2, 0, -1,
                1, 3, -2,
                1, 0, -2);
        return kernel;
    }

    //f9
    public static void medianBlurring(Mat src) {
        Imgproc.medianBlur(src, src, 25);
    }

    //f10
    public static void gaussianBlurring(Mat src) {
        Imgproc.GaussianBlur(src, src, new Size(45, 45), 0);
    }

    private static Mat kernel1() {
        Mat kernel = new Mat(3, 3, CvType.CV_16S);
        kernel.put(0, 0,
                -1, 0, 1,
                2, 0, -2,
                1, 0, -1);
        return kernel;
    }

    public static Mat filterSharpening(Mat src) {
        Imgproc.filter2D(src, src, src.depth(), kernelSharpening());
        return src;
    }

    private static Mat kernelSharpening() {
        Mat kernel = new Mat(3, 3, CvType.CV_16S);
        kernel.put(0, 0,
                0, -1, 0,
                -1, 5, -1,
                0, -1, 0);
        return kernel;
    }

    public static Mat filterMedianBlurring(Mat src) {
        Imgproc.filter2D(src, src, src.depth(), kernelMedianBlurring());
        return src;
    }

    private static Mat kernelMedianBlurring() {
        Mat kernel = new Mat(3, 3, CvType.CV_16S);
        kernel.put(0, 0,
                0, -1, 0,
                -1, 5, -1,
                0, -1, 0);
        return kernel;
    }

    public static Mat filter(Mat src) {
        Imgproc.filter2D(src, src, -1, kernel());
        return src;
    }

    private static Mat kernel() {
        Mat kernel = new Mat(3, 3, CvType.CV_16S);
        kernel.put(0, 0,
                -1, -1, -1,
                -1, 9, -1,
                -1, -1, -1);
        return kernel;
    }

    //f11 = filter1()
    public static void sobel(Mat src) {
        Imgproc.Sobel(src, src, src.depth(), 1, 0, 3);
    }

    //f12 = 50 = 50
    public static void mixImage(Mat src1, double alpha, Mat src2, double beta) {
        int sum = (int) (alpha + beta);
        alpha /= sum;
        beta /= sum;

        Imgproc.resize(src2, src2, src1.size());

        Core.addWeighted(src1, alpha, src2, beta, 0.0, src1);
    }

    //f13 = 0.5
    public static void lightBalanceGamma(Mat src, Mat dst, double gamma) {
        Mat lookUpTable = new Mat(1, 256, CvType.CV_8U);
        for (int i = 0; i < 256; i++) {
            double[] point = lookUpTable.get(0, i);
            point[0] = Math.pow(i / 255.0, gamma) * 255.0;
            lookUpTable.put(0, i, point);
        }

        Core.LUT(src, lookUpTable, dst);
    }

}
