package com.ale.imagic.model.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ale.imagic.ChosePictureActivity;
import com.ale.imagic.R;
import com.ale.imagic.convertor.Convert;
import com.ale.imagic.model.Album;
import com.ale.imagic.model.cache.CacheImage;

import java.util.ArrayList;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ViewHolder> {

    private ArrayList<Album> albums;
    private ChosePictureActivity context;

    public AlbumAdapter(ArrayList<Album> albums, Context context) {
        this.albums = albums;
        this.context = (ChosePictureActivity) context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_album, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Album album = albums.get(position);
        CacheImage cacheImage = album.getCacheImages().get(0);

        holder.imAlbum.setImageResource(R.color.white);
        holder.txAlbumName.setText(album.getName());
        holder.txSizeAlbum.setText(album.getCacheImages().size() + "");

        if (cacheImage.getBitmap() == null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Bitmap bitmap = Convert.resizeBitmap(cacheImage.getPath(), 200, 200);
                    holder.imAlbum.post(new Runnable() {
                        @Override
                        public void run() {
                            holder.imAlbum.setImageBitmap(bitmap);
                            cacheImage.setBitmap(bitmap);
                        }
                    });
                }
            }).start();
        } else {
            holder.imAlbum.setImageBitmap(cacheImage.getBitmap());
        }

        holder.imAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GridImageAdapter gridImageAdapter = new GridImageAdapter(album.getCacheImages(), context);
                context.rcListImage.setAdapter(gridImageAdapter);
                context.rcListImage.setLayoutManager(new GridLayoutManager(context, 3));
                context.ctListAlbum.setVisibility(View.GONE);
                changeImageDirection();
            }
        });
    }

    private void changeImageDirection(){
        if(context instanceof ChosePictureActivity){
            ChosePictureActivity chosePictureActivity = context;
            chosePictureActivity.imDirection.setImageResource(R.drawable.down);
        }
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imAlbum;
        TextView txAlbumName;
        TextView txSizeAlbum;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imAlbum = itemView.findViewById(R.id.imAlbum);
            txAlbumName = itemView.findViewById(R.id.txAlbumName);
            txSizeAlbum = itemView.findViewById(R.id.txSizeAlbum);
        }
    }
}
