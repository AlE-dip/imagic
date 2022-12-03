package com.ale.imagic;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.ImageView;

import com.ale.imagic.model.Album;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class MainActivity extends AppCompatActivity {

    public static String FACE_DETECTION = "FaceDetection";
    private ImageView imPicture;
    private BottomNavigationView navigationBar;
    public static ArrayList<Album> albums;
    private static boolean isSetAlbums;
    public static FinishSetAlbum finishSetAlbum;

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

        createView();
        isSetAlbums = false;

        albums = new ArrayList<>();
        if (AskPermission.filePermission(this)) {
            startGetFile();
        }

        setActionView();
    }

    public void startGetFile() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                File path = Environment.getExternalStorageDirectory();
                getListAlbum(path, albums);
                setFinishSetAlbum(null);
            }
        }).start();
    }

    private void createView() {
        imPicture = findViewById(R.id.im_picture);
        navigationBar = findViewById(R.id.navigation_bar);
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