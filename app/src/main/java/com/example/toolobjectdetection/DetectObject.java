package com.example.toolobjectdetection;



import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.util.Log;
import android.view.SurfaceHolder;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.task.vision.detector.Detection;
import org.tensorflow.lite.task.vision.detector.ObjectDetector;
import java.io.IOException;
import java.util.List;

public class DetectObject{

    public Context context;
    private Bitmap bitmap;
    private String feature,probability;
    private SurfaceHolder surfaceHolder;
    private Boolean Stream;

    public DetectObject(Context context,Bitmap bitmap, SurfaceHolder surfaceHolder){
        this.bitmap = bitmap;
        this.feature = null;
        this.probability = null;
        this.context = context;
        this.surfaceHolder = surfaceHolder;
        this.Stream = false;
    }


    public void Detect(){
        TensorImage image = new TensorImage(DataType.UINT8);
        image.load(bitmap);


        ObjectDetector.ObjectDetectorOptions options = ObjectDetector.ObjectDetectorOptions.builder().setMaxResults(5).setScoreThreshold((float) 0.5).build();
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

            DrawBoundingBox drawBoundingBox = new DrawBoundingBox(surfaceHolder,boundingBox);

            for(Category labels: detected.getCategories()){
                String text = labels.getLabel();
                float confidence = labels.getScore();

                if(confidence >= 0.50){
                    if(Stream){
                        drawBoundingBox.DrawBBforStream();
                    }else{
                        drawBoundingBox.drawBBforPic();
                    }

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
                }else{
                    feature = "Not Identified";
                }
                probability = String.valueOf(confidence);
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

    public void setStream(Boolean t){
        this.Stream = t;
    }

}
