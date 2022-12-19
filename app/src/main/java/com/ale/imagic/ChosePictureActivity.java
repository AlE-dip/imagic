package com.ale.imagic;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ale.imagic.model.adapter.AlbumAdapter;
import com.ale.imagic.model.adapter.GridImageAdapter;


public class ChosePictureActivity extends AppCompatActivity {

    private TextView txAlbum;
    public ConstraintLayout ctListAlbum, ctTitleAlbum;
    public RecyclerView rcListAlbum, rcListImage;
    private AlbumAdapter albumAdapter;
    private GridImageAdapter gridImageAdapter;
    public ImageView imBack, imDirection;

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chose_picture);
        MainActivity.hideSystemBar(ChosePictureActivity.this);

        createView();

        MainActivity.setFinishSetAlbum(() -> {
            setAlbum();
        });

        setActionView();
    }

    private void setAlbum() {
        albumAdapter = new AlbumAdapter(MainActivity.albums, this);
        rcListAlbum.setAdapter(albumAdapter);
        rcListAlbum.setLayoutManager(new LinearLayoutManager(this));

        gridImageAdapter = new GridImageAdapter(MainActivity.albums.get(0).getCacheImages(), this);
        rcListImage.setAdapter(gridImageAdapter);
        rcListImage.setLayoutManager(new GridLayoutManager(this, 3));

    }


    private void createView() {
        ctTitleAlbum = findViewById(R.id.ctAlbum);
        ctListAlbum = findViewById(R.id.ctListAlbum);
        rcListAlbum = findViewById(R.id.rcListAlbum);
        rcListImage = findViewById(R.id.rcListImage);
        imBack = findViewById(R.id.imBack);
        imDirection = findViewById(R.id.imDirection);
        txAlbum = findViewById(R.id.txTitleAlbum);
    }

    private void setActionView() {
        ctTitleAlbum.setOnClickListener((view) -> {
            if (ctListAlbum.getVisibility() == View.VISIBLE) {
                ctListAlbum.setVisibility(View.GONE);
                imDirection.setImageResource(R.drawable.down);
            } else {
                ctListAlbum.setVisibility(View.VISIBLE);
                imDirection.setImageResource(R.drawable.up);
            }
        });
        imBack.setOnClickListener(view -> {
            finish();
        });
    }


}