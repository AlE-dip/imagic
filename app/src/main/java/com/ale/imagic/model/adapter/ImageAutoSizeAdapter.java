package com.ale.imagic.model.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ale.imagic.EditPictureActivity;
import com.ale.imagic.R;
import com.ale.imagic.convertor.Handle;
import com.ale.imagic.model.cache.CacheImage;

import java.util.ArrayList;

public class ImageAutoSizeAdapter extends RecyclerView.Adapter<ImageAutoSizeAdapter.ViewHolder> {

    private Context context;
    private ArrayList<CacheImage> cacheImages;

    public ImageAutoSizeAdapter(Context context, ArrayList<CacheImage> cacheImages) {
        this.context = context;
        this.cacheImages = cacheImages;
    }

    @NonNull
    @Override
    public ImageAutoSizeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_image_auto_size, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageAutoSizeAdapter.ViewHolder holder, int position) {
        CacheImage cacheImage = cacheImages.get(position);

//        Handle.loadCacheImage(cacheImage, null, bitmap -> {
            holder.imAutoSize.post(new Runnable() {
                @Override
                public void run() {
                    holder.imAutoSize.setImageBitmap(cacheImage.getBitmap());
                }
            });
//        });

        holder.imAutoSize.setOnClickListener(new View.OnClickListener() {
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

        ImageView imAutoSize;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imAutoSize = itemView.findViewById(R.id.im_auto_size);
        }
    }
}
