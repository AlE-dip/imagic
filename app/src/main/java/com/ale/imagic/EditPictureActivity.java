package com.ale.imagic;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.ale.imagic.convertor.Convert;
import com.ale.imagic.model.cache.CacheFilter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class EditPictureActivity extends AppCompatActivity {

    private ImageView imEditPicture;
    private FrameLayout fmConfig;
    private BottomNavigationView nvOption;

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_picture);
        MainActivity.hideSystemBar(EditPictureActivity.this);

        createView();

        Intent intent = getIntent();
        String pathImage = intent.getStringExtra("path");

        Bitmap bitmap = Convert.readImage(pathImage);
        imEditPicture.setImageBitmap(bitmap);

        CacheFilter cacheFilter = new CacheFilter();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fmConfig, new ListFilterFragment(EditPictureActivity.this, bitmap, cacheFilter, imEditPicture));
        ft.commit();
    }

    private void createView() {
        imEditPicture = findViewById(R.id.im_edit_picture);
        fmConfig = findViewById(R.id.fmConfig);
    }
}