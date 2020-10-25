package com.example.toolobjectdetection;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.frame.Frame;
import com.otaliastudios.cameraview.frame.FrameProcessor;
import com.otaliastudios.cameraview.size.Size;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.Tensor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.task.vision.detector.Detection;
import org.tensorflow.lite.task.vision.detector.ObjectDetector;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final int MY_CAMERA_REQUEST_CODE = 101;
    private static final String TAG = "Probability";

    private Button btnCapture;
    private TextView feature;
    private TextView probability;
    private CameraView cameraView;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Interpreter interpreter;
//    private ImageView imageView;
    private Bitmap bitmap;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnCapture = findViewById(R.id.button);
        feature = findViewById(R.id.feature);
        probability = findViewById(R.id.probability);

        surfaceView = findViewById(R.id.surfaceView);
        surfaceView.setZOrderMediaOverlay(true);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT);

//        imageView = findViewById(R.id.imageView);

        if(!hasCamera()){
            btnCapture.setEnabled(false);
        }

        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });

        cameraView = findViewById(R.id.cameraview);
        cameraView.setLifecycleOwner(this);

        cameraView.addFrameProcessor(new FrameProcessor() {
            @Override
            @WorkerThread
            public void process(@NonNull Frame frame) {

                long time = frame.getTime();
                Size size = frame.getSize();
                int format = frame.getFormat();
                int userRotation = frame.getRotationToUser();
                int viewRotation = frame.getRotationToView();
                byte[] data = frame.getData();

//                extractData(frame);
                  TestStream(frame);
            }});

    }

    private boolean hasCamera(){
        if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)){
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},
                        MY_CAMERA_REQUEST_CODE);
            }
            return true;
        }else{
            return false;
        }
    }

    public void takePicture(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,2);
    }



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode,int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bundle extras = data.getExtras();
        bitmap = (Bitmap) extras.get("data");

        if (requestCode == 2) {
            if (resultCode == RESULT_OK) {

//                imageView.setImageBitmap(bitmap);

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
                Log.d("Test",String.valueOf(results));


            }
        }
    }



    public void TestStream(Frame frame){
//        byte[] data = frame.getData();

        bitmap = FrameImage(frame);

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
        Log.d("Test",String.valueOf(results));

    }


    public void drawRect(Rect bounds){
        try{
            Canvas canvas = surfaceHolder.lockCanvas();
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.BLUE);
            paint.setStrokeWidth(5);
            canvas.drawRect(bounds,paint);
            surfaceHolder.unlockCanvasAndPost(canvas);
        }catch (Throwable e){
            Log.d("Failed","error in drawing");
        }
    }


    public Bitmap FrameImage(Frame frame)
    {
        byte[] data = frame.getData();
        int imageFormat = frame.getFormat();

        if (imageFormat == ImageFormat.NV21)
        {
            YuvImage img = new YuvImage(data, ImageFormat.NV21, frame.getSize().getWidth(), frame.getSize().getHeight(), null);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            img.compressToJpeg(new android.graphics.Rect(0, 0, img.getWidth(), img.getHeight()), 50, out);
            byte[] imageBytes = out.toByteArray();
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        }
        else
        {
            Log.i(TAG, "Preview image not NV21");
            return null;
        }
    }



}