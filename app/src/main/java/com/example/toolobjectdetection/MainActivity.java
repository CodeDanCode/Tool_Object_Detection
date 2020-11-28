package com.example.toolobjectdetection;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.YuvImage;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.SizeF;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatActivity;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.frame.Frame;
import com.otaliastudios.cameraview.frame.FrameProcessor;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Test";
    private static final String cameraId = "0";

    private DetectObject detectObject;
    public TextView featureText;
    public TextView confidenceText;
    private SurfaceHolder surfaceHolder;
    private Bitmap bitmap;
    private SurfaceView surfaceView;
    private Button btnCapture;
    private Context context;
    private CameraView cameraView;
    private int rotation;


    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();


//        try {
//           rotation = getRotationCompensation(this);
//           Log.d(TAG,String.valueOf(rotation));
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }

        btnCapture = findViewById(R.id.button);
        featureText = findViewById(R.id.feature);
        confidenceText = findViewById(R.id.probability);

        surfaceView = findViewById(R.id.surfaceView);
        surfaceView.setZOrderMediaOverlay(true);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT);

        cameraView = findViewById(R.id.cameraView);
        cameraView.setLifecycleOwner(this);
//        cameraView.setRotation(90);

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

        detectObject = new DetectObject(bitmap,surfaceHolder);
        detectObject.setStream(true);
        detectObject.setContext(context);
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


//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    private int getRotationCompensation(Activity activity)
//            throws CameraAccessException {
//        // Get the device's current rotation relative to its "native" orientation.
//        // Then, from the ORIENTATIONS table, look up the angle the image must be
//        // rotated to compensate for the device's rotation.
//        int deviceRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
//        int rotationCompensation = ORIENTATIONS.get(deviceRotation);
//
//        // Get the device's sensor orientation.
//        CameraManager cameraManager = (CameraManager) activity.getSystemService(CAMERA_SERVICE);
//        int sensorOrientation = cameraManager
//                .getCameraCharacteristics(MainActivity.cameraId)
//                .get(CameraCharacteristics.SENSOR_ORIENTATION);
//
//        if(false) {
//            rotationCompensation = (sensorOrientation + rotationCompensation) % 360;
//        } else { // back-facing
//            rotationCompensation = (sensorOrientation - rotationCompensation + 360) % 360;
//        }
//        return rotationCompensation;
//    }

}