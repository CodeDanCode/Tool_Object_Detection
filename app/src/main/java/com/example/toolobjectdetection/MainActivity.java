package com.example.toolobjectdetection;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.graphics.RectF;
import android.graphics.YuvImage;
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
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.task.vision.detector.Detection;
import org.tensorflow.lite.task.vision.detector.ObjectDetector;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;



public class MainActivity extends AppCompatActivity {

    private static final int MY_CAMERA_REQUEST_CODE = 101;
    private static final String TAG = "Probability";

    private Button btnCapture;
    private TextView featureText;
    private TextView confidenceText;
    private CameraView cameraView;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Bitmap bitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnCapture = findViewById(R.id.button);
        featureText = findViewById(R.id.feature);
        confidenceText = findViewById(R.id.probability);

        surfaceView = findViewById(R.id.surfaceView);
        surfaceView.setZOrderMediaOverlay(true);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT);

        cameraView = findViewById(R.id.cameraView);
        cameraView.setLifecycleOwner(this);

        if(!hasCamera()){
            btnCapture.setEnabled(false);
        }

        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });


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
        Intent intent = new Intent(this,takePictureActivity.class);
        startActivity(intent);
    }



    @SuppressLint("SetTextI18n")
    public void TestStream(Frame frame){
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
//        Log.d("Test",String.valueOf(results));

        for ( Detection detected: results) {
            RectF boundingBox = detected.getBoundingBox();
            for (Category labels : detected.getCategories()) {
                String text = labels.getLabel();
                float confidence = labels.getScore();
                featureText.setText("Feature: "+text);
                confidenceText.setText("Confidence: "+ confidence);
//                Log.d("Test",text);
//                Log.d("Test",String.valueOf(confidence));

                if(text.equals("b'Hammer'") || text.equals("b'Wrench'")||text.equals("b'Wrench_Head'")){
                    drawRect(boundingBox);
                }

            }

        }
    }

    public void drawRect(RectF bounds){
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
            Log.d("Test","error in drawing");
        }
    }


    public Bitmap FrameImage(Frame frame) {
        byte[] data = frame.getData();
        int imageFormat = frame.getFormat();
        if (imageFormat == ImageFormat.NV21) {
            YuvImage img = new YuvImage(data, ImageFormat.NV21, frame.getSize().getWidth(), frame.getSize().getHeight(), null);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            img.compressToJpeg(new android.graphics.Rect(0, 0, img.getWidth(), img.getHeight()), 50, out);
            byte[] imageBytes = out.toByteArray();
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        }
        else {
            Log.i(TAG, "Preview image not NV21");
            return null;
        }
    }

}