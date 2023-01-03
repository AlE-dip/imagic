package com.ale.imagic.convertor;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.util.Log;

import com.ale.imagic.MainActivity;
import com.ale.imagic.model.ContentShare;
import com.ale.imagic.model.cache.CacheFilter;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.List;

public class Convert {

    public static Bitmap readImageMatToBitmap(String path) {
        Mat mat = Imgcodecs.imread(path, Imgcodecs.IMREAD_UNCHANGED);
        if (mat.cols() <= 0 || mat.rows() <= 0) {
            mat = Mat.zeros(100, 100, CvType.CV_8U);
        } else {
            resize(mat, 3);
        }
        Log.d(MainActivity.FACE_DETECTION, mat.cols() + "  " + mat.rows() + path);
        Bitmap bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bitmap);
        return bitmap;
    }

    public static Bitmap matToBitmap(Mat mat) {
        if (mat.cols() <= 0 || mat.rows() <= 0) {
            return null;
        }
        Bitmap bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bitmap);
        return bitmap;
    }

    public static Bitmap readImage(String path) {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inMutable = true;
        bmOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(path, bmOptions);
        return bitmap;
    }

    public static Bitmap createBitmapFromMat(Mat mat) {
        Bitmap bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bitmap);
        return bitmap;
    }

    public static Mat createMatFromBitmap(Bitmap bitmap) {
        Mat mat = new Mat();
        Utils.bitmapToMat(bitmap, mat);
        return mat;
    }

    public static void resize(Mat mat, int size) {
        if (size <= 0) {
            size = 1;
        }
        Size size1 = new Size(mat.width() / size, mat.height() / size);
        Imgproc.resize(mat, mat, size1);
    }

    public static Mat zoomAtPoint(Mat mat, double size, Point point) {
        Size s = mat.size();
        if (size <= 0) {
            size = 1;
        }
        Size size1 = new Size(mat.width() * size, mat.height() * size);
        Imgproc.resize(mat, mat, size1);
        int inX = (int) (point.x * size - point.x);
        int inY = (int) (point.y * size - point.y);
        if (inX < 0) inX = 0;
        if (inY < 0) inY = 0;
        if (inX + s.width > mat.cols()) inX = (int) (inX - (s.width - (mat.cols() - inX)));
        if (inY + s.height > mat.rows()) inY = (int) (inY - (s.height - (mat.rows() - inY)));
        Rect rect = new Rect(inX, inY, (int) s.width, (int) s.height);
        Mat dst = new Mat(mat, rect);
        return dst;
    }

    public static MatOfPoint zoomPoint(MatOfPoint matOfPoint, Mat mat, double size, Point point) {
        MatOfPoint mop = new MatOfPoint();
        Point[] points = matOfPoint.toArray();
        for (Point p : points) {
            double inX = point.x - ((point.x - p.x) * size);
            double inY = point.y - ((point.y - p.y) * size);
            if (inX < 0) inX = 0;
            if (inX > mat.cols()) inX = mat.cols();
            if (inY < 0) inY = 0;
            if (inY > mat.rows()) inY = mat.rows();
            p.x = inX;
            p.y = inY;
        }
        mop.fromArray(points);
        return mop;
    }

    public static MatOfPoint pointsToMatContour(List<PointF> points) {
        Point[] ps = new Point[points.size()];
        for (int i = 0; i < points.size(); i++) {
            PointF p = points.get(i);
            ps[i] = new Point(p.x, p.y);
        }
        MatOfPoint matOfPoint = new MatOfPoint();
        matOfPoint.fromArray(ps);
        return matOfPoint;
    }

    public static void resize(Mat mat, Size size) {
        Imgproc.resize(mat, mat, size);
    }

    public static Bitmap applyEffect(CacheFilter cacheFilter, Bitmap bitmap) {
        if (cacheFilter.getChangeImage() != null) {
            Mat mat = new Mat();
            Utils.bitmapToMat(bitmap, mat);
            Mat dst = cacheFilter.getChangeImage().Filter(mat, cacheFilter.getConfigFilter());
            ContentShare.saveImage = dst;
            return Convert.createBitmapFromMat(dst);
        } else {
            Utils.bitmapToMat(bitmap, ContentShare.saveImage);
            return bitmap;
        }
    }

    public static Bitmap resizeBitmap(String photoPath, int targetW, int targetH) {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = 1;
        if ((targetW > 0) || (targetH > 0)) {
            scaleFactor = Math.min(photoW / targetW, photoH / targetH);
        }

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        return BitmapFactory.decodeFile(photoPath, bmOptions);
    }

}
