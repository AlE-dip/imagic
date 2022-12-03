package com.ale.imagic.model.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ale.imagic.model.ConfigFilter;
import com.ale.imagic.model.cache.CacheFilter;
import com.ale.imagic.R;
import com.ale.imagic.convertor.Convert;

public class ConfigFilterAdapter extends RecyclerView.Adapter<ConfigFilterAdapter.ViewHolder> {

    private Context context;
    private CacheFilter cacheFilter;
    private ImageView imageView;
    private Bitmap bitmap;

    public ConfigFilterAdapter(Context context, CacheFilter cacheFilter, ImageView imageView, Bitmap bitmap) {
        this.context = context;
        this.cacheFilter = cacheFilter;
        this.imageView = imageView;
        this.bitmap = bitmap;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_config, parent, false);
        return new ViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ConfigFilter configFilter = cacheFilter.getConfigFilter();
        if (configFilter.seekBars != null && configFilter.seekBars.size() > position) {
            ConfigFilter.SeekBar sb = configFilter.seekBars.get(position);
            holder.rcListSelection.setVisibility(View.GONE);
            holder.txNameSeekBar.setText(sb.name);

            holder.sbConfig.setMin(sb.minSeekBar);
            holder.sbConfig.setMax(sb.maxSeekBar);
            holder.sbConfig.setProgress(sb.value);

            holder.sbConfig.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    sb.setValue(seekBar.getProgress());
                    imageView.setImageBitmap(Convert.applyEffect(cacheFilter, bitmap));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        } else if (configFilter.selections != null) {
            holder.sbConfig.setVisibility(View.GONE);
            holder.txNameSeekBar.setVisibility(View.GONE);
            SelectionAdapter selectionAdapter;
            selectionAdapter = new SelectionAdapter(context, cacheFilter, bitmap, imageView);
            holder.rcListSelection.setAdapter(selectionAdapter);
            holder.rcListSelection.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        }

    }

    @Override
    public int getItemCount() {
        int size = 0;
        if (cacheFilter.getConfigFilter().seekBars != null) {
            size += cacheFilter.getConfigFilter().seekBars.size();
        }
        if (cacheFilter.getConfigFilter().selections != null) {
            size++;
        }
        return size;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView txNameSeekBar;
        SeekBar sbConfig;
        RecyclerView rcListSelection;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txNameSeekBar = itemView.findViewById(R.id.txNameSeekBar);
            sbConfig = itemView.findViewById(R.id.sbConfig);
            rcListSelection = itemView.findViewById(R.id.rcListSelection);

        }
    }
}
