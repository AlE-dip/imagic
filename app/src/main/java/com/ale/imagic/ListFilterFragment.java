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

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

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
        cacheFilters.add(new CacheFilter(context.getString(R.string.binary_image), configFilter2, (mat, dst, configFilter) -> {
            Imgproc.cvtColor(mat, dst, Imgproc.COLOR_BGR2GRAY);
            Imgproc.threshold(dst, dst, configFilter.seekBars.get(0).value, configFilter.seekBars.get(1).value, configFilter.selected);
            return true;
        }));

        //Gray filter
        cacheFilters.add(new CacheFilter(context.getString(R.string.gray_image), null, (mat, dst, configFilter) -> {
            Imgproc.cvtColor(mat, dst, Imgproc.COLOR_BGR2GRAY);
            return true;
        }));

        //Blur filter
        ConfigFilter configFilter4 = new ConfigFilter();
        configFilter4.createSeekBar(100, 1, 200, context.getString(R.string.opacity));
        configFilter4.setSelected(5);
        configFilter4.createSelection(5, "5");
        configFilter4.createSelection(45, "45");
        configFilter4.createSelection(51, "51");
        cacheFilters.add(new CacheFilter(context.getString(R.string.gaussian_blur_image), configFilter4, (mat, dst, configFilter) -> {
            Imgproc.GaussianBlur(mat, dst, new Size(configFilter.selected, configFilter.selected), configFilter.seekBars.get(0).value / 10.0);
            return true;
        }));

        //Light filter
        ConfigFilter configFilter5 = new ConfigFilter();
        configFilter5.createSeekBar(-8, -20, -1, context.getString(R.string.brightness));
        cacheFilters.add(new CacheFilter(context.getString(R.string.light_balance_gamma), configFilter5, (mat, dst, configFilter) -> {
            Filter.lightBalanceGamma(mat, dst, configFilter.seekBars.get(0).value / 10.0 * -1);
            return true;
        }));

        //Dart filter
        ConfigFilter configFilter6 = new ConfigFilter();
        configFilter6.createSeekBar(2, 2, 50, context.getString(R.string.darkness));
        cacheFilters.add(new CacheFilter(context.getString(R.string.dark_image), configFilter6, (mat, dst, configFilter) -> {
            Filter.lightBalanceGamma(mat, dst, configFilter.seekBars.get(0).value);
            return true;
        }));

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