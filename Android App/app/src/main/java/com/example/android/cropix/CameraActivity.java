package com.example.android.cropix;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.example.android.cropix.CamUtils;
import com.example.android.cropix.ClassifierUtils;

import org.tensorflow.lite.Interpreter;

import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import pl.droidsonroids.gif.GifImageView;

public class CameraActivity extends Activity {
    static final int REQUEST_TAKE_PHOTO = 1;
    private Handler mWaitHandler = new Handler();
    ImageView imgTakenPhoto;
    String mCurrentPhotoPath;
    Interpreter interpreter;
    String final_result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("onCreate", "Success");
        dispatchTakePictureIntent();
        Log.e("dispatchPictureIntent", "Success");
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                CamUtils cutil = new CamUtils();
                photoFile = cutil.createImageFile(storageDir);
                mCurrentPhotoPath = photoFile.getAbsolutePath();
            } catch (IOException ex) {
                // Error occurred while creating the Fil
                Toast.makeText(getApplicationContext(), "Unexpected Error", Toast.LENGTH_SHORT).show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(CameraActivity.this,
                        "com.example.android.cropix.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Log.e("onActivityResult Entry", "Success");
        try {
            Log.e("onActivityResult try ", "Success");
            switch (requestCode) {
                case 1: {
                    Log.e("In switch case 1", "Success");
                    if (resultCode == RESULT_OK) {
                        Log.e("resultcode if", "Success");
                        Intent resAct = new Intent(this, ResultActivity.class);
                        resAct.putExtra("PhotoPath", mCurrentPhotoPath);
                        Log.e("intent resAct create", "Success");
                        startActivity(resAct);
                    }
                    break;
                }
            }

        } catch (Exception error) {
            error.printStackTrace();
        }
    }
}
