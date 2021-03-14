package com.example.android.cropix;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import org.json.simple.JSONObject;
import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Map;

import org.json.simple.parser.*;


import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import pl.droidsonroids.gif.GifImageView;

public class ResultActivity extends Activity {
    String mCurrentPhotoPath;
    String final_result = null;
    GifImageView detect_anim;
    private LinearLayout bottomSheetLayout;
    private LinearLayout gestureLayout;
    private BottomSheetBehavior sheetBehavior;
    protected ImageView bottomSheetArrowImageView;
    private androidx.cardview.widget.CardView bottomSheetCard;
    private ImageView bottomSheetCardImage;
    private TextView bottomSheetHead;
    private TextView bottomSheetSciName;
    private TextView bottomSheetDesc;
    private LinearLayout bottomSheetOut;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent prev_scr = getIntent();
        Log.e("intent get", "Success");
        mCurrentPhotoPath= prev_scr.getStringExtra("PhotoPath");
        Log.e("set mCurrent", "Success");
        try {
            displayImage();
            Log.e("display image", "Success");
        } catch (IOException e) {
            Log.e("display image", "Failed");
            e.printStackTrace();
            Toast.makeText(ResultActivity.this,"Something went Wrong",Toast.LENGTH_SHORT).show();
        }
    }

public void displayImage() throws IOException {
    Log.e("Inside display image", "Success");
    File file = null;
    if(mCurrentPhotoPath != null) {
        file = new File(mCurrentPhotoPath);
    }
    Log.e("create file object", "Success");
    final Bitmap bitmap = CamUtils.handleSamplingAndRotationBitmap(getApplicationContext(), Uri.fromFile(file));
    Log.e("create Bitmap", "Success");
    if (bitmap != null) {
        Log.e("bitmao not null", "Success");
        setContentView(R.layout.activity_img_display);
        Log.e("set content view result", "Success");
        setTheme(R.style.AppTheme);
        bottomSheetLayout = findViewById(R.id.bottom_sheet_layout);
        sheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);
        sheetBehavior.setPeekHeight(0);
        Log.e("set theme result", "Success");
        ImageView imgTakenPhoto = findViewById(R.id.imageView1);
        imgTakenPhoto.setImageBitmap(bitmap);
        Log.e("set image", "Success");
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                Log.e("delay before anim", "Success");
                detect_anim = (GifImageView) findViewById(R.id.overlay_anim);
                detect_anim.setImageResource(R.drawable.detection_animation);
                Log.e("show anim", "Success");
                try {
                    ClassifierUtils cutils = new ClassifierUtils();
                    Log.e("Inside try after anim", "Success");
                    InputStream inputStream = getResources().getAssets().open("cropix_model.tflite");
//                    File file2 = new File("/storage/emulated/0/Android/data/com.example.android.cropix/files/Model/cropix_model.tflite");
//                    Log.e("file2", "Success");
//                    Uri modelUri = FileProvider.getUriForFile(ResultActivity.this, "com.example.android.cropix.fileprovider", file2);
//                    Log.e("get model uri", "Success");
//                    InputStream inputStream = getContentResolver().openInputStream(modelUri);
                    Log.e("open input stream", "Success");
                    Interpreter interpreter = cutils.initModel(inputStream);
                    Log.e("interpreter init", "Success");



                    BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("custom_labels.txt")));
                    final_result = cutils.Classify(cutils.preprocessImage(bitmap), interpreter, reader);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }, 1000);

    }

    new Handler().postDelayed(new Runnable() {

        @Override
        public void run() {
            if(final_result != null) {
                Log.e("show result", "Success");
        detect_anim.setVisibility(View.GONE);
        gestureLayout = findViewById(R.id.gesture_layout);
        bottomSheetArrowImageView = findViewById(R.id.bottom_sheet_arrow);
        bottomSheetCardImage = findViewById(R.id.res_img);
        bottomSheetHead = findViewById(R.id.bottom_sheet_head);
        bottomSheetSciName = findViewById(R.id.bottom_sheet_sciname);
        bottomSheetDesc = findViewById(R.id.bottom_sheet_desc);
        bottomSheetCard = findViewById(R.id.card_view);
        bottomSheetOut = findViewById(R.id.bot_sheet_out);

        String sci ="";
        String desc = "";
        String bottomImageName = "";
        String bottomUrl = "";


                try {
                    org.json.simple.JSONObject jo = (org.json.simple.JSONObject) new JSONParser().parse(new InputStreamReader(getAssets().open("data.json")));

                    Map address = ((Map)jo.get(final_result));

                    for (Map.Entry pair : (Iterable<Map.Entry>) address.entrySet()) {
                        if (pair.getKey().toString().equals("name"))
                        {sci = pair.getValue().toString();
                            Log.e("value1", sci);}
                        else if(pair.getKey().toString().equals("desc"))
                        {desc = pair.getValue().toString();
                            Log.e("value2", desc);}
                        else if(pair.getKey().toString().equals("url"))
                        {bottomUrl = pair.getValue().toString();
                            Log.e("value3", bottomUrl);}
                        else {
                            bottomImageName = pair.getValue().toString();
                            Log.e("value4", bottomImageName);}
                    }

                } catch (Exception e) {
                    Log.e("catchexcep", e.toString());
        }

        if(!(bottomImageName.equals(""))) {
            Context context = bottomSheetCardImage.getContext();
            int id = context.getResources().getIdentifier(bottomImageName, "drawable", context.getPackageName());
            Bitmap bot_img = BitmapFactory.decodeResource(getResources(), id);
            final float bottom_scale = getResources().getDisplayMetrics().density;
            int px = (int) (75 * bottom_scale + 0.5f);
            Bitmap bottom_img = Bitmap.createScaledBitmap(bot_img, px, px, true);
            bottomSheetCardImage.setImageBitmap(bottom_img);
        }

                final String finalBottomUrl = bottomUrl;
                bottomSheetOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(finalBottomUrl.equals("")){
                    Toast.makeText(ResultActivity.this, "No Data Available", Toast.LENGTH_SHORT).show();
                }
                else {
                    Uri uri = Uri.parse(finalBottomUrl);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }

            }
        });

        bottomSheetHead.setText(final_result);
        bottomSheetSciName.setText(sci);
        bottomSheetDesc.setText(desc);

        final float initCardElev = bottomSheetCard.getCardElevation();


        ViewTreeObserver vto = gestureLayout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                            gestureLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            int height = gestureLayout.getMeasuredHeight();
                            sheetBehavior.setPeekHeight(height);
                    }
                });
        sheetBehavior.setHideable(false);

        sheetBehavior.addBottomSheetCallback(
                new BottomSheetBehavior.BottomSheetCallback() {
                    @Override
                    public void onStateChanged(@NonNull View bottomSheet, int newState) {
                        switch (newState) {
                            case BottomSheetBehavior.STATE_HIDDEN:
                                break;
                            case BottomSheetBehavior.STATE_EXPANDED:
                            {
                                bottomSheetArrowImageView.setImageResource(R.drawable.icn_chevron_down);
                                bottomSheetCard.setCardElevation(0);
                            }
                            break;
                            case BottomSheetBehavior.STATE_COLLAPSED:
                            {
                                bottomSheetArrowImageView.setImageResource(R.drawable.icn_chevron_up);
                                bottomSheetCard.setCardElevation(initCardElev);

                            }
                            break;
                            case BottomSheetBehavior.STATE_DRAGGING:
                            {
                                bottomSheetCard.setCardElevation(0);
                                break;
                            }
                            case BottomSheetBehavior.STATE_SETTLING:
                                bottomSheetCard.setCardElevation(0);
                                bottomSheetArrowImageView.setImageResource(R.drawable.icn_chevron_up);
                                break;
                        }
                    }

                    @Override
                    public void onSlide(@NonNull View bottomSheet, float slideOffset) {}
                });
            }

        }
    }, 3000);


    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(ResultActivity.this, MainActivity.class);
        startActivity(i);
    }

}