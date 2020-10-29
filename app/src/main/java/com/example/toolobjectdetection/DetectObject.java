package com.example.toolobjectdetection;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.util.JsonUtils;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.task.vision.detector.Detection;
import org.tensorflow.lite.task.vision.detector.ObjectDetector;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.List;

public class DetectObject{

    public Context context;
    private Bitmap bitmap;
    private String feature,probability;
    private SurfaceHolder surfaceHolder;

    public DetectObject(Context context,Bitmap bitmap, SurfaceHolder surfaceHolder){
        this.bitmap = bitmap;
        this.feature = null;
        this.probability = null;
        this.context = context;
        this.surfaceHolder = surfaceHolder;
    }


    public void Detect(){
        TensorImage image = new TensorImage(DataType.UINT8);
        image.load(bitmap);

        ObjectDetector.ObjectDetectorOptions options = ObjectDetector.ObjectDetectorOptions.builder().setMaxResults(1).build();
        ObjectDetector objectDetector = null;
        try {
            objectDetector = ObjectDetector.createFromFileAndOptions(context, "tool_object_detect_1_model.tflite", options);
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert objectDetector != null;
        List<Detection> results = objectDetector.detect(image);
        for ( Detection detected: results) {
            Log.d("Test",String.valueOf(results));

            RectF boundingBox = detected.getBoundingBox();
            Log.d("Test",String.valueOf(boundingBox));

            Log.d("Test","Left: "+String.valueOf(detected.getBoundingBox().left));
            Log.d("Test","Top: "+String.valueOf(detected.getBoundingBox().top));
            Log.d("Test","Right: "+String.valueOf(detected.getBoundingBox().right));
            Log.d("Test","Bottom: "+String.valueOf(detected.getBoundingBox().bottom));

            DrawBoundingBox drawBoundingBox = new DrawBoundingBox(this.surfaceHolder,boundingBox);

            for(Category labels: detected.getCategories()){
                String text = labels.getLabel();
                float confidence = labels.getScore();
                Log.d("Test",text);
                if(confidence >= 0.5){
                    switch (text) {
                        case "background":
                            feature = "Wrench";
                            break;
                        case "b'Hammer'":
                            feature = "Wrench Head";
                            break;
                        case "b'Wrench'":
                            feature = "Hammer";
                            break;
                    }
                    drawBoundingBox.DrawBB();
                    probability = String.valueOf(confidence);


                }else{
                    feature = "Not Identified";
                    probability = String.valueOf(confidence);
                }
            }
        }
    }

    public String getFeature(){
        if(this.feature != null){
            return feature;
        }else
        return "Null String";
    }
    public String getProbability(){
        if(this.probability != null){
            return probability;
        }else{
            return "Null String";
        }
    }

}
