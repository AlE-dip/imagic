package com.ale.imagic.model.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ale.imagic.R;
import com.ale.imagic.convertor.Convert;
import com.ale.imagic.model.cache.CacheFilter;

import java.util.ArrayList;
import java.util.Stack;

public class ListFilterAdapter extends RecyclerView.Adapter<ListFilterAdapter.ViewHolder> {

    private ArrayList<CacheFilter> cacheFilters;
    private Context context;
    private ImageView imageView;
    private Stack<Bitmap> stCacheBitmap;
    private ViewHolder cacheViewClick;
    private RecyclerView rcListConfig;
    public CacheFilter cacheFilter;

    public ListFilterAdapter(ArrayList<CacheFilter> cacheFilters, CacheFilter cacheFilter, Context context, Stack<Bitmap> stCacheBitmap, ImageView imageView, RecyclerView rcListConfig) {
        this.cacheFilters = cacheFilters;
        this.context = context;
        this.imageView = imageView;
        this.stCacheBitmap = stCacheBitmap;
        this.rcListConfig = rcListConfig;
        this.cacheFilter = cacheFilter;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_filter, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CacheFilter cacheFilter = cacheFilters.get(position);
        holder.id = cacheFilter.getId();
        holder.imImage.setImageResource(R.color.black);
        holder.txName.setText(cacheFilter.getName());
        holder.ctConfig.setVisibility(View.GONE);
        holder.imImage.setImageBitmap(Convert.applyEffect(cacheFilter, stCacheBitmap.peek(), Convert.NOT_SAVE_EFFECT));

        holder.imImage.setOnClickListener((view) -> {
            imageView.setImageBitmap(Convert.applyEffect(cacheFilter, stCacheBitmap.peek(), Convert.SAVE_EFFECT));
            this.cacheFilter.setCache(cacheFilter);
            if (cacheViewClick != null && cacheViewClick.ctConfig.getVisibility() == View.VISIBLE) {
                cacheViewClick.ctConfig.setVisibility(View.GONE);
                rcListConfig.setVisibility(View.GONE);
            }
            cacheViewClick = holder;
            //Set config filter
            if (this.cacheFilter.getConfigFilter() != null) {
                holder.ctConfig.setVisibility(View.VISIBLE);
                holder.ctConfig.setOnClickListener(createListenerConfig(this.cacheFilter));
            }
        });
    }

    public View.OnClickListener createListenerConfig(CacheFilter cacheFilter) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rcListConfig.getVisibility() == View.VISIBLE) {
                    rcListConfig.setVisibility(View.GONE);
                } else {
                    rcListConfig.setVisibility(View.VISIBLE);
                    ConfigFilterAdapter configFilterAdapter;
                    configFilterAdapter = new ConfigFilterAdapter(context, cacheFilter, imageView, stCacheBitmap);
                    rcListConfig.setAdapter(configFilterAdapter);
                    rcListConfig.setLayoutManager(new LinearLayoutManager(context));
                }
            }
        };
    }

    @Override
    public int getItemCount() {
        return cacheFilters.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        long id;
        ImageView imImage;
        ConstraintLayout ctConfig;
        TextView txName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            id = -111;
            imImage = itemView.findViewById(R.id.imImage);
            txName = itemView.findViewById(R.id.txName);
            ctConfig = itemView.findViewById(R.id.ct_config);
        }
    }
}
