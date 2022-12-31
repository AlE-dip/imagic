package com.ale.imagic;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.ale.imagic.convertor.Convert;
import com.ale.imagic.model.adapter.FeatureAdapter;
import com.ale.imagic.model.cache.CacheFilter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class EditPictureActivity extends AppCompatActivity {

    private ImageView imEditPicture, imBack;
    private FrameLayout fmConfig;
    private BottomNavigationView nvOption;
    private RecyclerView rcFooter;

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

        //
        createEvent();

        //footer
        ArrayList<FeatureFunction> featureFunctions = new ArrayList<>();
        featureFunctions.add(new FeatureFunction(getString(R.string.filter), R.drawable.color, (featureFunction) -> {
            if (!featureFunction.isClick) {
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                CacheFilter cacheFilter = new CacheFilter();
                ListFilterFragment listFilterFragment = new ListFilterFragment(EditPictureActivity.this, bitmap, cacheFilter, imEditPicture);
                fragmentTransaction.replace(R.id.fmConfig, listFilterFragment);
                fragmentTransaction.commit();
                featureFunction.isClick = true;
            } else {
                List<Fragment> fragList = getSupportFragmentManager().getFragments();
                for (Fragment fragment: fragList) {
                    getSupportFragmentManager().beginTransaction().remove(fragment).commit();
                }
                featureFunction.isClick = false;
            }

        }));
        FeatureAdapter featureAdapter = new FeatureAdapter(this, featureFunctions);
        rcFooter.setAdapter(featureAdapter);
        rcFooter.setLayoutManager(new LinearLayoutManager(this));
    }

    private void createEvent() {
        imBack.setOnClickListener(view -> {
            finish();
        });
    }

    private void createView() {
        imEditPicture = findViewById(R.id.im_edit_picture);
        fmConfig = findViewById(R.id.fmConfig);
        imBack = findViewById(R.id.im_back);
        rcFooter = findViewById(R.id.rc_footer);
    }

    public class  FeatureFunction {
        public boolean isClick;
        public String name;
        public int resource;
        public FeatureListener featureListener;

        public FeatureFunction(String name, int resource, FeatureListener featureListener) {
            isClick = false;
            this.name = name;
            this.resource = resource;
            this.featureListener = featureListener;
        }

        public void execute() {
            featureListener.run(this);
        };
    }

    public interface FeatureListener{
        public void run(FeatureFunction featureFunction);
    }
}