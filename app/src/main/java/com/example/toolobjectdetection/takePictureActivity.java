package com.example.toolobjectdetection;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.otaliastudios.cameraview.BitmapCallback;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.PictureResult;
import com.otaliastudios.cameraview.controls.Mode;

import org.jetbrains.annotations.NotNull;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.task.vision.detector.Detection;
import org.tensorflow.lite.task.vision.detector.ObjectDetector;
import org.tensorflow.lite.task.vision.segmenter.ImageSegmenter;
import org.tensorflow.lite.task.vision.segmenter.OutputType;
import org.tensorflow.lite.task.vision.segmenter.Segmentation;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class takePictureActivity extends AppCompatActivity{
    private static final String TAG = "Test";

    ImageView imageView;
    CameraView cameraView;
    TextView feature,probability;
    SurfaceView surfaceView;
    Button btnCapture;
    SurfaceHolder surfaceHolder;
    AssetManager assetManager;
    DetectObject detectObject;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_picture);
        context = getApplicationContext();

        imageView = findViewById(R.id.imageView);
        feature = findViewById(R.id.feature);
        probability = findViewById(R.id.probability);
        btnCapture = findViewById(R.id.button);

        surfaceView = findViewById(R.id.surfaceView);
        surfaceView.setZOrderOnTop(true);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        assetManager = getAssets();

        cameraView = findViewById(R.id.cameraView);
        cameraView.setMode(Mode.PICTURE);
        cameraView.setLifecycleOwner(this);

        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cameraView.isOpened()) {
                    takePicture();
                   Log.d(TAG, "Picture Taken");
                }else{
                    cameraView.open();
                }

            }
        });
    }

    // take picture and listen for results
    public void takePicture(){
        cameraView.addCameraListener(new CameraListener() {

            @Override
            public void onPictureTaken(@NotNull PictureResult result) {
                result.toBitmap(320, 320, new BitmapCallback() {
                    @Override
                    public void onBitmapReady(Bitmap bitmap) {
                        imageView.setImageBitmap(bitmap);
                        Log.d(TAG,"in bitmap callback");
                        detectObject = new DetectObject(context,bitmap,surfaceHolder);
                        detectObject.Detect();
                        feature.setText(detectObject.getFeature());
                        probability.setText(detectObject.getProbability());
                        cameraView.close();
                    }
                });
            }
        });

        cameraView.takePicture();

    }


    @Override
    protected void onResume() {
        super.onResume();
        cameraView.open();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraView.destroy();
    }



}