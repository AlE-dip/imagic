package com.ale.imagic.model.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ale.imagic.EditPictureActivity;
import com.ale.imagic.R;
import com.ale.imagic.convertor.Convert;
import com.ale.imagic.convertor.Handle;
import com.ale.imagic.model.cache.CacheImage;

import java.util.ArrayList;

public class GridImageAdapter extends RecyclerView.Adapter<GridImageAdapter.ViewHolder> {

    private ArrayList<CacheImage> cacheImages;
    private Context context;

    public GridImageAdapter(ArrayList<CacheImage> cacheImages, Context context) {
        this.cacheImages = cacheImages;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        CacheImage cacheImage = cacheImages.get(position);
        holder.id = cacheImage.getId();
        holder.imImage.setImageResource(R.color.white);

        Handle.loadCacheImage(cacheImage, new Size(200, 200), bitmap -> {
            holder.imImage.post(new Runnable() {
                @Override
                public void run() {
                    if (holder.id == cacheImage.getId()) {
                        holder.imImage.setImageBitmap(bitmap);
                    }
                }
            });
        });

        holder.imImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, EditPictureActivity.class);
                intent.putExtra("path", cacheImage.getPath());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cacheImages.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        long id;
        ImageView imImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            id = -111;
            imImage = itemView.findViewById(R.id.imImage);
        }
    }
}
