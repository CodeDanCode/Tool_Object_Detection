package com.example.toolobjectdetection;


import androidx.appcompat.app.AppCompatActivity;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Bundle;
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

public class takePictureActivity extends AppCompatActivity{
    private static final String TAG = "Test";

    ImageView imageView;
    CameraView cameraView;
    TextView feature,probability;
    SurfaceView surfaceView;
    Button btnCapture;
    SurfaceHolder surfaceHolder;
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
                        detectObject = new DetectObject(bitmap,surfaceHolder);
                        detectObject.setStream(false);
                        detectObject.setContext(context);
                        detectObject.Detect();
                        feature.setText(detectObject.getFeature());
                        probability.setText(detectObject.getFinalSize());
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