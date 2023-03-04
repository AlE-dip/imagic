package com.ale.imagic;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.ale.imagic.convertor.Convert;
import com.ale.imagic.model.ContentShare;
import com.ale.imagic.model.adapter.FeatureAdapter;
import com.ale.imagic.model.cache.CacheFilter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EditPictureActivity extends AppCompatActivity {

    private ImageView imEditPicture, imBack, imSave;
    private FrameLayout fmConfig;
    private BottomNavigationView nvOption;
    private RecyclerView rcFooter;
    private FeatureFunction featureFunctionClick;
    private Bitmap cacheBitmap;
    private String pathImage;

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_picture);
        MainActivity.hideSystemBar(EditPictureActivity.this);

        createView();

        Intent intent = getIntent();
        pathImage = intent.getStringExtra("path");

        cacheBitmap = Convert.readImage(pathImage);
        ContentShare.saveImage = Convert.createMatFromBitmap(cacheBitmap);
        imEditPicture.setImageBitmap(cacheBitmap);

        //
        createEvent();

        //footer
        //filter color
        ArrayList<FeatureFunction> featureFunctions = new ArrayList<>();
        featureFunctions.add(new FeatureFunction(getString(R.string.filter), R.drawable.color, featureFunction -> {
            if (!featureFunction.isClick) {
                //remove this last click
                removeCacheClick(featureFunction);

                CacheFilter cacheFilter = new CacheFilter();
                ListFilterFragment listFilterFragment = new ListFilterFragment(EditPictureActivity.this, cacheBitmap, cacheFilter, imEditPicture);
                addFragment(featureFunction, listFilterFragment);
            } else {
                removeAllFragment(featureFunction);
            }
        }));
        //resize
        featureFunctions.add(new FeatureFunction(getString(R.string.resize), R.drawable.resize, featureFunction -> {
            if (!featureFunction.isClick) {
                //remove this last click
                removeCacheClick(featureFunction);
            } else {
                removeAllFragment(featureFunction);
            }
        }));

        FeatureAdapter featureAdapter = new FeatureAdapter(this, featureFunctions);
        rcFooter.setAdapter(featureAdapter);
        rcFooter.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
    }

    private void removeCacheClick(FeatureFunction featureFunction) {
        //remove this last click
        if (featureFunctionClick != null && !featureFunctionClick.equals(featureFunction) && featureFunctionClick.isClick) {
            featureFunctionClick.featureListener.run(featureFunctionClick);
            featureFunctionClick = null;
        }
    }

    private void addFragment(FeatureFunction featureFunction, Fragment listFilterFragment) {
        //add new
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fmConfig, listFilterFragment);
        fragmentTransaction.commit();
        featureFunction.isClick = true;
        //cache function
        featureFunctionClick = featureFunction;
    }

    private void removeAllFragment(FeatureFunction featureFunction) {
        List<Fragment> fragList = getSupportFragmentManager().getFragments();
        for (Fragment fragment : fragList) {
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
        }
        featureFunction.isClick = false;
    }

    private void saveImage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.download));
        builder.setMessage(getString(R.string.view_ads));
        builder.setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
            dialogInterface.cancel();
        });
        builder.setPositiveButton(R.string.download, (dialogInterface, i) -> {
            String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
            File file = new File(root, UtilContains.LOCATION);
            if (!file.exists()) {
                file.mkdir();
            }
            File image = new File(pathImage);
            String nameImage = UtilContains.LOCATION + "_" +image.getName();
            File imageSave = new File(file, nameImage);
            if (imageSave.exists()) imageSave.delete();
            Mat save = new Mat();
            Imgproc.cvtColor(ContentShare.saveImage, save, Imgproc.COLOR_RGB2BGRA);
            Imgcodecs.imwrite(imageSave.toString(), save);
            Toast.makeText(this, R.string.downloaded, Toast.LENGTH_LONG).show();
        });
        builder.show();
    }

    private void createEvent() {
        imBack.setOnClickListener(view -> {
            finish();
        });

        imSave.setOnClickListener(view -> {
            saveImage();
        });
    }

    private void createView() {
        imEditPicture = findViewById(R.id.im_edit_picture);
        fmConfig = findViewById(R.id.fmConfig);
        imBack = findViewById(R.id.im_back);
        rcFooter = findViewById(R.id.rc_footer);
        imSave = findViewById(R.id.im_save);
    }

    public class FeatureFunction {
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
        }
    }

    public interface FeatureListener {
        public void run(FeatureFunction featureFunction);
    }
}