package com.example.toolobjectdetection;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
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

import java.io.IOException;
import java.util.List;

public class takePictureActivity extends AppCompatActivity{
    Bitmap bitmapImage;
    ImageView imageView;
    CameraView cameraView;
    TextView feature,probability;
    SurfaceView surfaceView;
    Button btnCapture;
    SurfaceTexture surfaceTexture;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_picture);
        imageView = findViewById(R.id.imageView);
        feature = findViewById(R.id.feature);
        probability = findViewById(R.id.probability);
        btnCapture = findViewById(R.id.button);

        surfaceView = findViewById(R.id.surfaceView);
        surfaceView.setZOrderMediaOverlay(true);
        surfaceTexture = new SurfaceTexture(0);

        cameraView = findViewById(R.id.cameraView);
        cameraView.setMode(Mode.PICTURE);
        cameraView.setLifecycleOwner(this);

        // MediaStore Intent for Image Capture
//        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        startActivityForResult(intent,2);


        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cameraView.isOpened()) {
                    takePicture();
                   Log.d("Test", "Picture Taken");
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
                        //here bitmap you will get
                        imageView.setImageBitmap(bitmap);
                        Log.d("Test","in bitmap callback");
                        DetectObject(bitmap);
                        cameraView.close();

                    }
                });

            }
        });

        cameraView.takePicture();

    }


    public void DetectObject(Bitmap bitmap){

        TensorImage image = new TensorImage(DataType.UINT8);
        image.load(bitmap);

        // Initialization
        ObjectDetector.ObjectDetectorOptions options = ObjectDetector.ObjectDetectorOptions.builder().setMaxResults(1).build();
        ObjectDetector objectDetector = null;
        try {
            objectDetector = ObjectDetector.createFromFileAndOptions(this, "tool_object_detect_1_model.tflite", options);
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert objectDetector != null;
        List<Detection> results = objectDetector.detect(image);
        for ( Detection detected: results) {
            RectF boundingBox = detected.getBoundingBox();
            Log.d("Test",String.valueOf(boundingBox));

            //Draw Bounding box for still image

            for(Category labels: detected.getCategories()){
                String text = labels.getLabel();
                float confidence = labels.getScore();
                Log.d("Test",text);
                feature.setText(text);
                probability.setText(String.valueOf(confidence));
            }

        }
    }

    /*
    On Activity Result for Media Store. may use later
    or use other method
     */

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode,int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bundle extras = data.getExtras();
        assert extras != null;
        bitmapImage = (Bitmap) extras.get("data");

        if (requestCode == 2) {
            if (resultCode == RESULT_OK) {

                imageView.setImageBitmap(bitmapImage);

                TensorImage image = new TensorImage(DataType.UINT8);
                image.load(bitmapImage);

                // Initialization
                ObjectDetector.ObjectDetectorOptions options = ObjectDetector.ObjectDetectorOptions.builder().setMaxResults(1).build();
                ObjectDetector objectDetector = null;
                try {
                    objectDetector = ObjectDetector.createFromFileAndOptions(this, "tool_object_detect_1_model.tflite", options);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                assert objectDetector != null;
                List<Detection> results = objectDetector.detect(image);
                for ( Detection detected: results) {
                    RectF boundingBox = detected.getBoundingBox();
                    Log.d("Test",String.valueOf(boundingBox));

                    //Draw Bounding box for still image

                    for(Category labels: detected.getCategories()){
                        String text = labels.getLabel();
                        float confidence = labels.getScore();
                        Log.d("Test",text);
                        feature.setText("Feature: "+text);
                        probability.setText("Confidence: "+confidence);
                    }

                }

            }
        }
    }



}