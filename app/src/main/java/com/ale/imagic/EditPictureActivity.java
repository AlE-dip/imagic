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
import com.ale.imagic.convertor.Filter;
import com.ale.imagic.model.ConfigFilter;
import com.ale.imagic.model.ContentShare;
import com.ale.imagic.model.adapter.ConfigFilterAdapter;
import com.ale.imagic.model.adapter.FeatureAdapter;
import com.ale.imagic.model.cache.CacheFilter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.soundcloud.android.crop.Crop;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class EditPictureActivity extends AppCompatActivity {

    private static final int ALL_VIEW = 0;
    private static final int LIST_CONFIG_VIEW = 1;
    private static final int FRAGMENT_VIEW = 2;

    private ImageView imEditPicture, imBack, imDownload, imUndo, imPredo;
    private static ImageView imSave;
    private FrameLayout fmConfig;
    private BottomNavigationView nvOption;
    private RecyclerView rcFooter, rcListConfig;
    private FeatureFunction featureFunctionClick;
    private static Bitmap cacheBitmap;
    private static String pathImage;
    private static String pathImageTemp;
    public Stack<Bitmap> stCacheBitmap, stPredoBitmap;

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_picture);
        MainActivity.hideSystemBar(EditPictureActivity.this);

        createView();

        Intent intent = getIntent();
        pathImage = intent.getStringExtra("path");

        stCacheBitmap = new Stack<>();
        stPredoBitmap = new Stack<>();
        pushCacheBitmap(Convert.readImage(pathImage));
        imEditPicture.setImageBitmap(peekBitmap());

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
            if (!featureFunction.isClick()) {
                //remove this last click
                removeCacheClick(featureFunction);

                CacheFilter cacheFilter = new CacheFilter();
                ListFilterFragment listFilterFragment = new ListFilterFragment(EditPictureActivity.this, stCacheBitmap, cacheFilter, imEditPicture);
                addFragment(featureFunction, listFilterFragment);
                featureFunctionClick = featureFunction;
            } else {
                removeAllFragment(featureFunction, ALL_VIEW);
            }
        }));
        //resize
        featureFunctions.add(new FeatureFunction(getString(R.string.resize), R.drawable.resize, featureFunction -> {
            if (!featureFunction.isClick) {
                //remove this last click
                removeCacheClick(featureFunction);
                addResizeDialog();
            } else {
                removeAllFragment(featureFunction, ALL_VIEW);
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
                removeAllFragment(featureFunction, ALL_VIEW);
            }
        }));

        //brightness
        featureFunctions.add(new FeatureFunction(getString(R.string.brightness_darkness), R.drawable.brightness, featureFunction -> {
            if (!featureFunction.isClick()) {
                //remove this last click
                removeCacheClick(featureFunction);
                ConfigFilter lightDark = new ConfigFilter();
                lightDark.createSeekBar(-8, -20, -1, getString(R.string.advance));
                lightDark.createSeekBar(0, -255, 255, getString(R.string.brightness_darkness));
                ConfigFilterAdapter configFilterAdapter = new ConfigFilterAdapter(EditPictureActivity.this, new CacheFilter(
                        getString(R.string.brightness_darkness),
                        lightDark,
                        (mat, configFilter) -> {
                            Mat dst = new Mat(mat.size(), mat.type());
                            Filter.lightBalanceGamma(mat, dst, configFilter.seekBars.get(0).value / 10.0 * -1);

                            int value = configFilter.seekBars.get(1).value;
                            if(value > 0){
                                Core.add(dst, new Scalar(value, value, value), dst);
                            } else if(value < 0){
                                value *= -1;
                                Core.subtract(dst, new Scalar(value, value, value), dst);
                            }
                            return dst;
                        }),
                        imEditPicture,
                        stCacheBitmap
                );
                rcListConfig.setVisibility(View.VISIBLE);
                rcListConfig.setAdapter(configFilterAdapter);
                rcListConfig.setLayoutManager(new LinearLayoutManager(EditPictureActivity.this, LinearLayoutManager.VERTICAL, false));
                featureFunctionClick = featureFunction;
            } else {
                removeAllFragment(featureFunction, ALL_VIEW);
            }
        }));

        //contrast
        featureFunctions.add(new FeatureFunction(getString(R.string.contrast), R.drawable.contrast, featureFunction -> {
            if (!featureFunction.isClick()) {
                //remove this last click
                removeCacheClick(featureFunction);
                ConfigFilter lightDark = new ConfigFilter();
                lightDark.createSeekBar(1, 0, 100, getString(R.string.contrast));
                ConfigFilterAdapter configFilterAdapter = new ConfigFilterAdapter(EditPictureActivity.this, new CacheFilter(
                        getString(R.string.contrast),
                        lightDark,
                        (mat, configFilter) -> {
                            int value = configFilter.seekBars.get(0).value;
                            double contrast = value / 10.0;
                            Mat contrastAdjusted = new Mat();
                            mat.convertTo(contrastAdjusted, -1, contrast, 0);
                            return contrastAdjusted;
                        }),
                        imEditPicture,
                        stCacheBitmap
                );
                rcListConfig.setVisibility(View.VISIBLE);
                rcListConfig.setAdapter(configFilterAdapter);
                rcListConfig.setLayoutManager(new LinearLayoutManager(EditPictureActivity.this, LinearLayoutManager.VERTICAL, false));
                featureFunctionClick = featureFunction;
            } else {
                removeAllFragment(featureFunction, ALL_VIEW);
            }
        }));

        //rotate
        featureFunctions.add(new FeatureFunction(getString(R.string.rotate), R.drawable.rotation, featureFunction -> {
            if (!featureFunction.isClick()) {
                //remove this last click
                removeCacheClick(featureFunction);
                ConfigFilter lightDark = new ConfigFilter();
                lightDark.createSelection(3, getString(R.string.default_image));
                lightDark.createSelection(Core.ROTATE_90_CLOCKWISE, getString(R.string.rotate_right));
                lightDark.createSelection(Core.ROTATE_90_COUNTERCLOCKWISE, getString(R.string.rotate_left));
                lightDark.createSelection(Core.ROTATE_180, getString(R.string.rotate_180));
                ConfigFilterAdapter configFilterAdapter = new ConfigFilterAdapter(EditPictureActivity.this, new CacheFilter(
                        getString(R.string.rotate),
                        lightDark,
                        (mat, configFilter) -> {
                            Mat rotated = new Mat();
                            if(configFilter.selected == 3){
                                return mat;
                            }
                            Core.rotate(mat, rotated, configFilter.selected);
                            return rotated;
                        }),
                        imEditPicture,
                        stCacheBitmap
                );
                rcListConfig.setVisibility(View.VISIBLE);
                rcListConfig.setAdapter(configFilterAdapter);
                rcListConfig.setLayoutManager(new LinearLayoutManager(EditPictureActivity.this, LinearLayoutManager.VERTICAL, false));
                featureFunctionClick = featureFunction;
            } else {
                removeAllFragment(featureFunction, ALL_VIEW);
            }
        }));

        //flip
        featureFunctions.add(new FeatureFunction(getString(R.string.flip), R.drawable.flip, featureFunction -> {
            if (!featureFunction.isClick()) {
                //remove this last click
                removeCacheClick(featureFunction);
                ConfigFilter lightDark = new ConfigFilter();
                lightDark.createSelection(2, getString(R.string.default_image));
                lightDark.createSelection(0, getString(R.string.flip_vertical));
                lightDark.createSelection(1, getString(R.string.flip_horizontal));

                ConfigFilterAdapter configFilterAdapter = new ConfigFilterAdapter(EditPictureActivity.this, new CacheFilter(
                        getString(R.string.flip),
                        lightDark,
                        (mat, configFilter) -> {
                            Mat flipped = new Mat();
                            if(configFilter.selected == 2){
                                return mat;
                            }

                            Core.flip(mat, flipped, configFilter.selected);
                            return flipped;
                        }),
                        imEditPicture,
                        stCacheBitmap
                );
                rcListConfig.setVisibility(View.VISIBLE);
                rcListConfig.setAdapter(configFilterAdapter);
                rcListConfig.setLayoutManager(new LinearLayoutManager(EditPictureActivity.this, LinearLayoutManager.VERTICAL, false));
                featureFunctionClick = featureFunction;
            } else {
                removeAllFragment(featureFunction, ALL_VIEW);
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
        Imgproc.cvtColor(Convert.createMatFromBitmap(stCacheBitmap.peek()), save, Imgproc.COLOR_RGB2BGRA);
        Imgcodecs.imwrite(imageSave.toString(), save);
        pathImageTemp = imageSave.toString();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent result) {
        super.onActivityResult(requestCode, resultCode, result);
        if (requestCode == Crop.REQUEST_CROP && resultCode == RESULT_OK) {
            pushCacheBitmap(Convert.readImage(pathImageTemp));
            imEditPicture.setImageBitmap(stCacheBitmap.peek());
            File file = new File(pathImageTemp);
            if(file.exists()){
                file.delete();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void addResizeDialog() {
        Bitmap bitmap = peekBitmap();
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
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
                    Mat mat = Convert.createMatFromBitmap(peekBitmap());
                    Convert.resize(mat, new Size(newWidth, newHeight));
                    pushCacheBitmap(Convert.createBitmapFromMat(mat));
                    imEditPicture.setImageBitmap(peekBitmap());
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
    }

    private void removeAllFragment(FeatureFunction featureFunction, int type) {
        List<Fragment> fragList = getSupportFragmentManager().getFragments();
        switch (type) {
            case ALL_VIEW:
                for (Fragment fragment : fragList) {
                    getSupportFragmentManager().beginTransaction().remove(fragment).commit();
                }
                featureFunction.isClick = false;
                rcListConfig.setVisibility(View.GONE);
                break;
            case LIST_CONFIG_VIEW:
                rcListConfig.setVisibility(View.GONE);
                break;
            case FRAGMENT_VIEW:
                for (Fragment fragment : fragList) {
                    getSupportFragmentManager().beginTransaction().remove(fragment).commit();
                }
                featureFunction.isClick = false;
                break;
        }
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
            Imgproc.cvtColor(Convert.createMatFromBitmap(peekBitmap()), save, Imgproc.COLOR_RGB2BGRA);
            Imgcodecs.imwrite(imageSave.toString(), save);
            Toast.makeText(this, R.string.downloaded, Toast.LENGTH_LONG).show();
        });
        builder.show();
    }

    private void createEvent() {
        imBack.setOnClickListener(view -> {
            finish();
        });

        imDownload.setOnClickListener(view -> {
            saveImage();
        });

        imUndo.setOnClickListener(view -> {
            Bitmap bitmap = stCacheBitmap.pop();
            imEditPicture.setImageBitmap(stCacheBitmap.peek());
            stPredoBitmap.push(bitmap);
            if(stPredoBitmap.size() > 0){
                imPredo.setVisibility(View.VISIBLE);
            }
            if(stCacheBitmap.size() == 1){
                imUndo.setVisibility(View.INVISIBLE);
            }
        });

        imPredo.setOnClickListener(view -> {
            Bitmap bitmap = stPredoBitmap.pop();
            stCacheBitmap.push(bitmap);
            imEditPicture.setImageBitmap(stCacheBitmap.peek());
            if(stCacheBitmap.size() > 1){
                imUndo.setVisibility(View.VISIBLE);
            }
            if(stPredoBitmap.size() == 0){
                imPredo.setVisibility(View.INVISIBLE);
            }
        });

        imSave.setOnClickListener(view -> {
            pushCacheBitmap(cacheBitmap);
            cacheBitmap = null;
            imSave.setVisibility(View.INVISIBLE);
        });
    }

    private void createView() {
        imEditPicture = findViewById(R.id.im_edit_picture);
        fmConfig = findViewById(R.id.fmConfig);
        imBack = findViewById(R.id.im_back);
        rcFooter = findViewById(R.id.rc_footer);
        imDownload = findViewById(R.id.im_download);
        rcListConfig = findViewById(R.id.rc_list_config);
        imSave = findViewById(R.id.im_save);
        imUndo = findViewById(R.id.im_undo);
        imPredo = findViewById(R.id.im_predo);
    }

    public class FeatureFunction {
        private boolean isClick;
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

        public boolean isClick() {
            if(!isClick){
                isClick = true;
                //cache function
                return false;
            }
            return true;
        }
    }

    public interface FeatureListener {
        public void run(FeatureFunction featureFunction);
    }

    public static void setCacheImage(Bitmap cacheBitmap){
        EditPictureActivity.cacheBitmap = cacheBitmap;
        imSave.setVisibility(View.VISIBLE);
    }

    public static Bitmap getCacheImage(){
        return EditPictureActivity.cacheBitmap;
    }
    
    public void pushCacheBitmap(Bitmap bitmap){
        if(stCacheBitmap.size() >= 5){
            stCacheBitmap.remove(0);
        }
        stCacheBitmap.push(bitmap);
        if(stCacheBitmap.size() > 1){
            imUndo.setVisibility(View.VISIBLE);
        }
        stPredoBitmap.clear();
        imPredo.setVisibility(View.INVISIBLE);
    }

    public Bitmap peekBitmap(){
        return stCacheBitmap.peek();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(pathImageTemp != null){
            File file = new File(pathImageTemp);
            if(file.exists()){
                file.delete();
            }
        }
    }
}