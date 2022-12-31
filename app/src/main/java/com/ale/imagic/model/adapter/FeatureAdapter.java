package com.ale.imagic.model.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.ale.imagic.EditPictureActivity;
import com.ale.imagic.R;

import java.util.ArrayList;

public class FeatureAdapter extends RecyclerView.Adapter<FeatureAdapter.ViewHolder> {

    Context context;
    ArrayList<EditPictureActivity.FeatureFunction> featureFunctions;

    public FeatureAdapter(Context context, ArrayList<EditPictureActivity.FeatureFunction> featureFunctions) {
        this.context = context;
        this.featureFunctions = featureFunctions;
    }

    @NonNull
    @Override
    public FeatureAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_feature, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeatureAdapter.ViewHolder holder, int position) {
        EditPictureActivity.FeatureFunction featureFunction = featureFunctions.get(position);
        holder.imIcon.setImageResource(featureFunction.resource);
        holder.txName.setText(featureFunction.name);

        holder.ct_item.setOnClickListener(view -> {
            featureFunction.execute();
        });
    }

    @Override
    public int getItemCount() {
        return featureFunctions.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imIcon;
        TextView txName;
        ConstraintLayout ct_item;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imIcon = itemView.findViewById(R.id.im_icon);
            txName = itemView.findViewById(R.id.tx_name);
            ct_item = itemView.findViewById(R.id.ct_item);
        }
    }
}
