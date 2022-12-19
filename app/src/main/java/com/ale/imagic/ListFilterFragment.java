package com.ale.imagic;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ale.imagic.model.cache.CacheFilter;
import com.ale.imagic.convertor.Filter;
import com.ale.imagic.model.ConfigFilter;
import com.ale.imagic.model.adapter.ListFilterAdapter;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListFilterFragment extends Fragment {

    private Context context;
    private ArrayList<CacheFilter> cacheFilters;
    private Bitmap bitmap;
    private RecyclerView rcListFilter, rcListConfig;
    private ListFilterAdapter listFilterAdapter;
    private ImageView imageView;
    private CacheFilter cacheFilter;
    private Mat avatar;

    public ListFilterFragment(Context context, Bitmap bitmap, CacheFilter cacheFilter, ImageView imageView) {
        this.context = context;
        this.bitmap = bitmap;
        this.imageView = imageView;
        this.cacheFilter = cacheFilter;
        createOperations();
    }

    private void createOperations() {
        cacheFilters = new ArrayList<>();

        //default
        cacheFilters.add(new CacheFilter(context.getString(R.string.default_image), null, null));

        //set default
        cacheFilter.setCache(cacheFilters.get(0));

        //Binary filter
        ConfigFilter configFilter2 = new ConfigFilter();
        configFilter2.createSeekBar(80, 1, 255, context.getString(R.string.thresh));
        configFilter2.createSeekBar(255, 1, 255, context.getString(R.string.maxval));
        configFilter2.setSelected(Imgproc.THRESH_BINARY);
        configFilter2.createSelection(Imgproc.THRESH_BINARY, "THRESH_BINARY");
        configFilter2.createSelection(Imgproc.THRESH_BINARY_INV, "THRESH_BINARY_INV");
        configFilter2.createSelection(Imgproc.THRESH_TRUNC, "THRESH_TRUNC");
        configFilter2.createSelection(Imgproc.THRESH_MASK, "THRESH_MASK");
        configFilter2.createSelection(Imgproc.THRESH_OTSU, "THRESH_OTSU");
        configFilter2.createSelection(Imgproc.THRESH_TOZERO, "THRESH_TOZERO");
        configFilter2.createSelection(Imgproc.THRESH_TOZERO_INV, "THRESH_TOZERO_INV");
        configFilter2.createSelection(Imgproc.THRESH_TRIANGLE, "THRESH_TRIANGLE");
        cacheFilters.add(new CacheFilter(context.getString(R.string.binary_image), configFilter2, (mat, configFilter) -> {
            Mat dst = new Mat(mat.size(), mat.type());
            Imgproc.cvtColor(mat, dst, Imgproc.COLOR_BGR2GRAY);
            Imgproc.threshold(dst, dst, configFilter.seekBars.get(0).value, configFilter.seekBars.get(1).value, configFilter.selected);
            return dst;
        }));

        //Gray filter
        cacheFilters.add(new CacheFilter(context.getString(R.string.gray_image), null, (mat, configFilter) -> {
            Mat dst = new Mat(mat.size(), mat.type());
            Imgproc.cvtColor(mat, dst, Imgproc.COLOR_BGR2GRAY);
            return dst;
        }));

        //Blur filter
        ConfigFilter configFilter4 = new ConfigFilter();
        configFilter4.createSeekBar(100, 1, 200, context.getString(R.string.opacity));
        configFilter4.setSelected(5);
        configFilter4.createSelection(5, "5");
        configFilter4.createSelection(45, "45");
        configFilter4.createSelection(51, "51");
        cacheFilters.add(new CacheFilter(context.getString(R.string.gaussian_blur_image), configFilter4, (mat, configFilter) -> {
            Mat dst = new Mat(mat.size(), mat.type());
            Imgproc.GaussianBlur(mat, dst, new Size(configFilter.selected, configFilter.selected), configFilter.seekBars.get(0).value / 10.0);
            return dst;
        }));

        //Light filter
        ConfigFilter configFilter5 = new ConfigFilter();
        configFilter5.createSeekBar(-8, -20, -1, context.getString(R.string.brightness));
        cacheFilters.add(new CacheFilter(context.getString(R.string.light_balance_gamma), configFilter5, (mat, configFilter) -> {
            Mat dst = new Mat(mat.size(), mat.type());
            Filter.lightBalanceGamma(mat, dst, configFilter.seekBars.get(0).value / 10.0 * -1);
            return dst;
        }));

        //Dart filter
        ConfigFilter configFilter6 = new ConfigFilter();
        configFilter6.createSeekBar(2, 2, 50, context.getString(R.string.darkness));
        cacheFilters.add(new CacheFilter(context.getString(R.string.dark_image), configFilter6, (mat, configFilter) -> {
            Mat dst = new Mat(mat.size(), mat.type());
            Filter.lightBalanceGamma(mat, dst, configFilter.seekBars.get(0).value);
            return dst;
        }));

        //Histogram
        ConfigFilter cfHistogram = new ConfigFilter();
//        cfHistogram.createSeekBar(50, 0, 255, context.getString(R.string.thresh));
//        cfHistogram.createSeekBar(0, 0, 255, context.getString(R.string.histogram_index));
        cfHistogram.selected = 0;
        cfHistogram.createSelection(3, "All");
        cfHistogram.createSelection(0, "red");
        cfHistogram.createSelection(1, "green");
        cfHistogram.createSelection(2, "blue");
        cacheFilters.add(new CacheFilter(context.getString(R.string.histogram), cfHistogram, (mat, cfFilter) -> {
            Mat histogram = new Mat(mat.size(), mat.type());

            Size rgbaSize = mat.size();
            int histSize = 256;
            MatOfInt histogramSize = new MatOfInt(histSize);

            int histogramHeight = (int) rgbaSize.height;
            int binWidth = 5;

            MatOfFloat histogramRange = new MatOfFloat(0f, 256f);

            Scalar[] colorsRgb = new Scalar[]{new Scalar(200, 0, 0, 255), new Scalar(0, 200, 0, 255), new Scalar(0, 0, 200, 255)};
            MatOfInt[] channels = new MatOfInt[]{new MatOfInt(0), new MatOfInt(1), new MatOfInt(2)};

            Mat[] histograms = new Mat[]{new Mat(), new Mat(), new Mat()};

            if(cfFilter.selected == 3){
                for (int i = 0; i < channels.length; i++) {
                    Imgproc.calcHist(Collections.singletonList(mat), channels[i], new Mat(), histograms[i], histogramSize, histogramRange);
                    Core.normalize(histograms[i], histograms[i], histogramHeight, 0, Core.NORM_INF);
                    for (int j = 0; j < histSize; j++) {
                        Point p1 = new Point(binWidth * (j - 1), histogramHeight - Math.round(histograms[i].get(j - 1, 0)[0]));
                        Point p2 = new Point(binWidth * j, histogramHeight - Math.round(histograms[i].get(j, 0)[0]));
                        Imgproc.line(mat, p1, p2, colorsRgb[i], 2, 8, 0);
                    }
                }
            } else {
                Imgproc.calcHist(Collections.singletonList(mat), channels[cfFilter.selected], new Mat(), histograms[cfFilter.selected], histogramSize, histogramRange);
                Core.normalize(histograms[cfFilter.selected], histograms[cfFilter.selected], histogramHeight, 0, Core.NORM_INF);
                for (int j = 0; j < histSize; j++) {
                    Point p1 = new Point(binWidth * (j - 1), histogramHeight - Math.round(histograms[cfFilter.selected].get(j - 1, 0)[0]));
                    Point p2 = new Point(binWidth * j, histogramHeight - Math.round(histograms[cfFilter.selected].get(j, 0)[0]));
                    Imgproc.line(mat, p1, p2, colorsRgb[cfFilter.selected], 2, 8, 0);
                }
            }
            return mat;
        }));

        //Delete color
        ConfigFilter cfDeleteColor = new ConfigFilter();
        cfDeleteColor.setSelected(Filter.BLUE);
        cfDeleteColor.createSelection(Filter.BLUE, context.getString(R.string.blue));
        cfDeleteColor.createSelection(Filter.GREEN, context.getString(R.string.green));
        cfDeleteColor.createSelection(Filter.RED, context.getString(R.string.red));
        cfDeleteColor.createSelection(Filter.YELLOW, context.getString(R.string.yellow));
        cfDeleteColor.createSelection(Filter.VIOLET, context.getString(R.string.violet));
        cfDeleteColor.createSelection(Filter.DARK_GREEN, context.getString(R.string.dark_green));
        cacheFilters.add(new CacheFilter(context.getString(R.string.color), cfDeleteColor, (mat, cfFilter) -> {
            Mat dst = new Mat(mat.size(), mat.type());
            Filter.removeColor(mat, dst, cfFilter.selected);
            return dst;
        }));

        //Negative color
        cacheFilters.add(new CacheFilter(context.getString(R.string.negative), null, (mat, cfFilter) -> {
            Mat dst = new Mat(mat.size(), mat.type());
            Core.bitwise_not(mat, dst);
            return dst;
        }));

        //Contour
        cacheFilters.add(new CacheFilter(context.getString(R.string.contour), null, (mat, cfFilter) -> {
            Mat dst = new Mat(mat.size(), mat.type());
            Imgproc.filter2D(mat, dst, mat.depth(), Filter.kernelBlackLine());
            return dst;
        }));

        //Shaping
        cacheFilters.add(new CacheFilter(context.getString(R.string.shaping), null, (mat, cfFilter) -> {
            Mat dst = new Mat(mat.size(), mat.type());
            Imgproc.filter2D(mat, dst, mat.depth(), Filter.kernelShaping());
            return dst;
        }));

        //Emboss
        ConfigFilter configFilter7 = new ConfigFilter();
        configFilter7.createSeekBar(2, 1, 10, context.getString(R.string.emboss_value));
        cacheFilters.add(new CacheFilter(context.getString(R.string.emboss), configFilter7, (mat, cfFilter) -> {
            Mat dst = new Mat(mat.size(), mat.type());
            Imgproc.filter2D(mat, dst, mat.depth(), Filter.kernelEmboss(cfFilter.seekBars.get(0).value));
            return dst;
        }));

//        //Change color
//        cfDeleteColor.createSelection(Filter.BLUE, context.getString(R.string.blue));
//        cfDeleteColor.createSelection(Filter.GREEN, context.getString(R.string.green));
//        cfDeleteColor.createSelection(Filter.RED, context.getString(R.string.red));
//        cacheFilters.add(new CacheFilter(context.getString(R.string.emboss), null, (mat, cfFilter) -> {
//            Mat dst = new Mat();
//            Mat src = new Mat();
//            Imgproc.cvtColor(mat, src, Imgproc.COLOR_RGB2HSV);
//            Core.inRange(src, new Scalar(100,100,100), new Scalar(255,255,255), dst);
//            return dst;
//        }));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_filter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rcListFilter = view.findViewById(R.id.rcListFilter);
        rcListConfig = view.findViewById(R.id.rcListConfig);
        listFilterAdapter = new ListFilterAdapter(cacheFilters, cacheFilter, context, bitmap, imageView, rcListConfig);
        rcListFilter.setAdapter(listFilterAdapter);
        rcListFilter.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
    }
}