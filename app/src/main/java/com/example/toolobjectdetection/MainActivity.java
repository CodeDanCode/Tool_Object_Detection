package com.example.toolobjectdetection;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.YuvImage;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatActivity;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.frame.Frame;
import com.otaliastudios.cameraview.frame.FrameProcessor;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import java.io.ByteArrayOutputStream;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Test";
    public static SurfaceHolder parentSurfaceHolder;
    private DetectObject detectObject;
    public TextView featureText;
    public TextView confidenceText;
    private SurfaceHolder surfaceHolder;
    private Bitmap bitmap;
    private SurfaceView surfaceView;
    private Button btnCapture;
    private Context context;
    private CameraView cameraView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();

        btnCapture = findViewById(R.id.button);
        featureText = findViewById(R.id.feature);
        confidenceText = findViewById(R.id.probability);

        surfaceView = findViewById(R.id.surfaceView);
        surfaceView.setZOrderMediaOverlay(true);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        parentSurfaceHolder = surfaceHolder;

        cameraView = findViewById(R.id.cameraView);
        cameraView.setLifecycleOwner(this);

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
                  TestStream(frame);
            }});
    }

    public void takePicture(){
        Intent intent = new Intent(this,takePictureActivity.class);
        startActivity(intent);
    }


    public void TestStream(Frame frame){

        bitmap = FrameImage(frame);
        TensorImage image = new TensorImage(DataType.UINT8);
        image.load(bitmap);

        detectObject = new DetectObject(context,bitmap,surfaceHolder);
        detectObject.setStream(true);
        detectObject.Detect();
        featureText.setText(detectObject.getFeature());
        confidenceText.setText(detectObject.getProbability());
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