package com.example.android.cropix;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static final int CAM_REQ_CODE = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("IN Main", "yes");
        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Intent scr_1 = new Intent(MainActivity.this, CameraActivity.class);
            startActivity(scr_1);
        }
        else {
            String[] permissionsReq = {Manifest.permission.CAMERA};
            requestPermissions(permissionsReq, CAM_REQ_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == CAM_REQ_CODE){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Intent scr_1 = new Intent(MainActivity.this, CameraActivity.class);
                startActivity(scr_1);
            }
            else {
                Toast.makeText(MainActivity.this, "Camera Permissions Required", Toast.LENGTH_LONG).show();
            }
        }
    }
}
