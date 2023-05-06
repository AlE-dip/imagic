package com.ale.imagic;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.ale.imagic.convertor.Convert;
import com.ale.imagic.model.ConfigFilter;
import com.ale.imagic.model.ContentShare;
import com.ale.imagic.model.adapter.ConfigFilterAdapter;
import com.ale.imagic.model.adapter.FeatureAdapter;
import com.ale.imagic.model.cache.CacheFilter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.soundcloud.android.crop.Crop;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EditPictureActivity extends AppCompatActivity {

    private ImageView imEditPicture, imBack, imSave;
    private FrameLayout fmConfig;
    private BottomNavigationView nvOption;
    private RecyclerView rcFooter, rcListConfig;
    private FeatureFunction featureFunctionClick;
    private static Bitmap cacheBitmap;
    private static String pathImage;
    private static String pathImageTemp;

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
        imEditPicture.setImageBitmap(cacheBitmap);

        //
        createEvent();

        ArrayList<FeatureFunction> featureFunctions = new ArrayList<>();
        addListFeture(featureFunctions);

        FeatureAdapter featureAdapter = new FeatureAdapter(this, featureFunctions);
        rcFooter.setAdapter(featureAdapter);
        rcFooter.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void addListFeture(ArrayList<FeatureFunction> featureFunctions) {
        //footer
        //filter color
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
                addResizeDialog();
            } else {
                removeAllFragment(featureFunction);
            }
        }));
        //Crop
        featureFunctions.add(new FeatureFunction(getString(R.string.crop), R.drawable.crop, featureFunction -> {
            if (!featureFunction.isClick) {
                //remove this last click
                removeCacheClick(featureFunction);
                saveTempImage();
                Crop.of(Uri.fromFile(new File(pathImageTemp)), Uri.fromFile(new File(pathImageTemp)))
                        .withAspect(0, 0)
                        .start(this);
                //call after onActivityResult
            } else {
                removeAllFragment(featureFunction);
            }
        }));
        //Text to image
        featureFunctions.add(new FeatureFunction(getString(R.string.brightness_darkness), R.drawable.brightness, featureFunction -> {
            if (!featureFunction.isClick) {
                //remove this last click
                removeCacheClick(featureFunction);
                ConfigFilter lightDark = new ConfigFilter();
                lightDark.createSeekBar(0, -255, 255, getString(R.string.brightness_darkness));
                ConfigFilterAdapter configFilterAdapter = new ConfigFilterAdapter(EditPictureActivity.this, new CacheFilter(
                        getString(R.string.brightness_darkness),
                        lightDark,
                        (mat, configFilter) -> {
                            Mat dst = new Mat(mat.size(), mat.type());
                            int value = configFilter.seekBars.get(0).value;
                            if(value > 0){
                                Core.add(mat, new Scalar(value, value, value), dst);
                            } else if(value < 0){
                                value *= -1;
                                Core.subtract(mat, new Scalar(value, value, value), dst);
                            }
                            return dst;
                        }),
                        imEditPicture,
                        cacheBitmap
                );
                rcListConfig.setVisibility(View.VISIBLE);
                rcListConfig.setAdapter(configFilterAdapter);
                rcListConfig.setLayoutManager(new LinearLayoutManager(EditPictureActivity.this, LinearLayoutManager.VERTICAL, false));
            } else {
                removeAllFragment(featureFunction);
                rcListConfig.setVisibility(View.GONE);
            }
        }));

    }

    private void saveTempImage(){
        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        File file = new File(root, UtilContains.LOCATION);
        if (!file.exists()) {
            file.mkdir();
        }
        File image = new File(pathImage);
        String nameImage = UtilContains.TEMP + "_" +image.getName();
        File imageSave = new File(file, nameImage);
        if (imageSave.exists()) imageSave.delete();
        Mat save = new Mat();
        Imgproc.cvtColor(Convert.createMatFromBitmap(cacheBitmap), save, Imgproc.COLOR_RGB2BGRA);
        Imgcodecs.imwrite(imageSave.toString(), save);
        pathImageTemp = imageSave.toString();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent result) {
        super.onActivityResult(requestCode, resultCode, result);
        if (requestCode == Crop.REQUEST_CROP && resultCode == RESULT_OK) {
            cacheBitmap = Convert.readImage(pathImageTemp);
            imEditPicture.setImageBitmap(cacheBitmap);
            File file = new File(pathImageTemp);
            if(file.exists()){
                file.delete();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void addResizeDialog() {
        int width = cacheBitmap.getWidth();
        int height = cacheBitmap.getHeight();
        boolean[] isChanged = {false};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.change_size_image));

        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.resize_image_dialog, null);
        EditText edWidth = view.findViewById(R.id.ed_width);
        EditText edHeight = view.findViewById(R.id.ed_height);
        //set size
        edWidth.setText(width + "");
        edHeight.setText(height + "");

        edWidth.addTextChangedListener(eventChangeSizeImage(width, height, edHeight, isChanged));
        edHeight.addTextChangedListener(eventChangeSizeImage(height, width, edWidth, isChanged));

        builder.setView(view);

        // Set up the buttons
        builder.setPositiveButton(getString(R.string.change), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(edHeight.getText().toString().equals("") || edWidth.getText().toString().equals("")){
                    Toast.makeText(EditPictureActivity.this, getString(R.string.error_input_blank), Toast.LENGTH_SHORT).show();
                } else if (Integer.valueOf(edHeight.getText().toString()) < 50 || Integer.valueOf(edWidth.getText().toString()) < 50){
                    Toast.makeText(EditPictureActivity.this, getString(R.string.width_height_50), Toast.LENGTH_SHORT).show();
                } else if (Integer.valueOf(edHeight.getText().toString()) * Integer.valueOf(edWidth.getText().toString()) > UtilContains.MAX_PIXEL){
                    Toast.makeText(EditPictureActivity.this, getString(R.string.file_is_large), Toast.LENGTH_SHORT).show();
                } else {
                    int newWidth = Integer.valueOf(edWidth.getText().toString());
                    int newHeight = Integer.valueOf(edHeight.getText().toString());
                    Mat mat = Convert.createMatFromBitmap(cacheBitmap);
                    Convert.resize(mat, new Size(newWidth, newHeight));
                    cacheBitmap = Convert.createBitmapFromMat(mat);
                    imEditPicture.setImageBitmap(cacheBitmap);
                }
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private TextWatcher eventChangeSizeImage(int rootSize, int sizeChange, EditText edChange, boolean[] isChanged){
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!isChanged[0] && !charSequence.toString().equals("")){
                    isChanged[0] = true;
                    double percent = Integer.valueOf(charSequence.toString()) * 1.0 / rootSize;
                    int afterSize = (int) (sizeChange * percent);
                    edChange.setText(afterSize + "");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                isChanged[0] = false;
            }
        };
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
            Imgproc.cvtColor(Convert.createMatFromBitmap(cacheBitmap), save, Imgproc.COLOR_RGB2BGRA);
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
        rcListConfig = findViewById(R.id.rc_list_config);
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

    public static void setCacheImage(Bitmap cacheBitmap){
        EditPictureActivity.cacheBitmap = cacheBitmap;
    }

    public static Bitmap getCacheImage(){
        return EditPictureActivity.cacheBitmap;
    }
}