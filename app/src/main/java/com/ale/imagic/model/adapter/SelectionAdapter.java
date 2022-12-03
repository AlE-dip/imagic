package com.ale.imagic.model.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.ale.imagic.R;
import com.ale.imagic.convertor.Convert;
import com.ale.imagic.model.ConfigFilter;
import com.ale.imagic.model.cache.CacheFilter;


public class SelectionAdapter extends RecyclerView.Adapter<SelectionAdapter.ViewHolder> {

    private Context context;
    private CacheFilter cacheFilter;
    private ImageView imageView;
    private Bitmap bitmap;
    private ViewHolder cacheViewCLick;

    public SelectionAdapter(Context context, CacheFilter cacheFilter, Bitmap bitmap, ImageView imageView) {
        this.context = context;
        this.cacheFilter = cacheFilter;
        this.bitmap = bitmap;
        this.imageView = imageView;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_selection, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ConfigFilter.Selection selection = cacheFilter.getConfigFilter().selections.get(position);
        holder.txSelector.setText(selection.name);

        holder.txSelector.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                if (cacheViewCLick != null && cacheViewCLick.isClick) {
                    cacheViewCLick.txSelector.setBackgroundColor(context.getColor(R.color.non));
                    cacheViewCLick = holder;
                } else {
                    cacheViewCLick = holder;
                }
                holder.isClick = true;
                holder.txSelector.setBackgroundColor(context.getColor(R.color.gray));

                cacheFilter.getConfigFilter().setSelected(selection.value);

                imageView.setImageBitmap(Convert.applyEffect(cacheFilter, bitmap));
            }
        });
    }

    @Override
    public int getItemCount() {
        return cacheFilter.getConfigFilter().selections.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView txSelector;
        public boolean isClick;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txSelector = itemView.findViewById(R.id.txSelector);
            isClick = false;
        }
    }
}
