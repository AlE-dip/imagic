package com.ale.imagic;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.ale.imagic.convertor.Convert;
import com.ale.imagic.convertor.Handle;
import com.ale.imagic.model.Album;
import com.ale.imagic.model.adapter.ImageAutoSizeAdapter;
import com.ale.imagic.model.cache.CacheImage;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class MainActivity extends AppCompatActivity {

    private AdView mAdView;
    public static String FACE_DETECTION = "FaceDetection";
    private BottomNavigationView navigationBar;
    public static ArrayList<Album> albums;
    private static boolean isSetAlbums;
    public static FinishSetAlbum finishSetAlbum;
    private RecyclerView rcImageLeft;
    private SwipeRefreshLayout wrRefreshListPreview;

    static {
        if (OpenCVLoader.initDebug()) {
            Log.d("Check", "OpenCv configured successfully");
        } else {
            Log.d("Check", "OpenCv doesn’t configured successfully");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        hideSystemBar(MainActivity.this);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {

            }
        });
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        createView();

        if (AskPermission.filePermission(this)) {
            startGetFile();
        }

        setActionView();
    }

    public void startGetFile() {
        albums = new ArrayList<>();
        isSetAlbums = false;
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void run() {
                wrRefreshListPreview.post(new Runnable() {
                    @Override
                    public void run() {
                        wrRefreshListPreview.setRefreshing(true);
                    }
                });
                File path = Environment.getExternalStorageDirectory();
                getListAlbum(path, albums);
                setFinishSetAlbum(null);
                getSelfAlbum();
                wrRefreshListPreview.post(new Runnable() {
                    @Override
                    public void run() {
                        wrRefreshListPreview.setRefreshing(false);
                    }
                });
            }
        }).start();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void getSelfAlbum() {
        albums.forEach(album -> {
            if(album.getName().equals(UtilContains.LOCATION)){
                ArrayList<CacheImage> cacheImageLefts = new ArrayList<>();
                for (int i = 0; i < album.getCacheImages().size(); i++){
                    CacheImage cacheImage = album.getCacheImages().get(i);
                    cacheImage.setBitmap(Convert.readImage(cacheImage.getPath()));
                    cacheImageLefts.add(cacheImage);
                }
                ImageAutoSizeAdapter imageAutoSizeAdapterLeft = new ImageAutoSizeAdapter(this, cacheImageLefts);
                rcImageLeft.post(new Runnable() {
                    @Override
                    public void run() {
                        rcImageLeft.setAdapter(imageAutoSizeAdapterLeft);
                        rcImageLeft.setLayoutManager(new GridLayoutManager(MainActivity.this, 2));
                    }
                });
            }
        });
    }

    private void createView() {
        navigationBar = findViewById(R.id.navigation_bar);
        rcImageLeft = findViewById(R.id.rc_image_left);
        wrRefreshListPreview = findViewById(R.id.wr_refresh_list_preview);
    }

    private void setActionView() {
        navigationBar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.mnGallery:
                        Intent intent = new Intent(MainActivity.this, ChosePictureActivity.class);
                        startActivity(intent);
                        break;
                }
                return true;
            }
        });

        wrRefreshListPreview.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startGetFile();
                wrRefreshListPreview.setRefreshing(false);
            }
        });
    }

    public static void hideSystemBar(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                activity.getWindow().setDecorFitsSystemWindows(false);
                WindowInsetsController controller = activity.getWindow().getInsetsController();
                if (controller != null) {
                    controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                    controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                }
            } catch (Exception e) {
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == AskPermission.REQUEST_FILE_CODE) {
            startGetFile();
        }

    }

    private void getListAlbum(File file, ArrayList<Album> albums) {
        Queue<File[]> queueFile = new LinkedList<>();
        File[] arrFile = {file};
        queueFile.add(arrFile);

        while (queueFile.size() > 0) {
            arrFile = queueFile.poll();

            for (int i = 0; i < arrFile.length; i++) {

                if (arrFile[i].isDirectory()) {
                    File[] dirFiles = arrFile[i].listFiles(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String filename) {
                            File file2 = new File(dir, filename);
                            return file2.isDirectory() && !file2.isHidden() && !filename.startsWith(".");
                        }
                    });
                    if (dirFiles != null && dirFiles.length > 0) {
                        queueFile.add(dirFiles);
                    }
                    File[] imageFiles = arrFile[i].listFiles(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String filename) {
                            return filename.contains(".png") || filename.contains(".jpg");
                        }
                    });
                    if (imageFiles != null && imageFiles.length > 0) {
                        Album album = new Album(arrFile[i].getName(), imageFiles);
                        albums.add(album);
                    }
                }
            }
        }
    }

    public synchronized static void setFinishSetAlbum(FinishSetAlbum finishSetAlbum) {
        if (MainActivity.isSetAlbums) {
            finishSetAlbum.excute();
        } else {
            MainActivity.isSetAlbums = true;
            MainActivity.finishSetAlbum = finishSetAlbum;
        }
    }

    public interface FinishSetAlbum {
        public void excute();
    }
}