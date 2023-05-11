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
import com.ale.imagic.model.cache.CacheImage;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.util.ArrayList;


public class ChosePictureActivity extends AppCompatActivity {

    private AdView mAdView;
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

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {

            }
        });
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        createView();

        MainActivity.setFinishSetAlbum(() -> {
            setAlbum();
            MainActivity.finishSetAlbum = null;
        });

        setActionView();
    }

    private void setAlbum() {
        albumAdapter = new AlbumAdapter(MainActivity.albums, this);
        rcListAlbum.setAdapter(albumAdapter);
        rcListAlbum.setLayoutManager(new LinearLayoutManager(this));

        ArrayList<CacheImage> cacheImages = MainActivity.albums.size() != 0 ? MainActivity.albums.get(0).getCacheImages() : new ArrayList<>();
        gridImageAdapter = new GridImageAdapter(cacheImages, this);
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