package com.example.toolobjectdetection;



import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.util.Log;
import android.view.SurfaceHolder;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.image.ops.Rot90Op;
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
    private DrawBoundingBox drawBoundingBox;
    private DetectSize detectSize;
    private RectF referenceBox,unknownBox;

    public DetectObject(Bitmap bitmap, SurfaceHolder surfaceHolder){
        this.bitmap = bitmap;
        this.feature = null;
        this.probability = null;
        this.surfaceHolder = surfaceHolder;
        this.Stream = false;
    }


    public void Detect(){

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Log.d("Test","Height: "+height);
        Log.d("test","Width: "+width);
        int size = Math.min(height, width);

        ImageProcessor imageProcessor =
                new ImageProcessor.Builder()
                        // Center crop the image to the largest square possible
                        // Rotation counter-clockwise in 90 degree increments
                        .add(new ResizeOp(1440,1080 , ResizeOp.ResizeMethod.BILINEAR))
                        .add(new Rot90Op(90))
                        .build();

        TensorImage image = new TensorImage(DataType.UINT8);
        image.load(bitmap);
        image = imageProcessor.process(image);

//       add .setScoreThreshold((float) 0.5) to set threshold for detector
        ObjectDetector.ObjectDetectorOptions options = ObjectDetector.ObjectDetectorOptions.builder().setMaxResults(2).setScoreThreshold((float)0.20).build();
        ObjectDetector objectDetector = null;
        try {
            objectDetector = ObjectDetector.createFromFileAndOptions(context, "tool_object_detect_2_model.tflite", options);
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert objectDetector != null;
        List<Detection> results = objectDetector.detect(image);
        Log.d("Test",String.valueOf(results));



        if(results.size() == 2) {
            float first = results.get(0).getBoundingBox().left;
            float second = results.get(1).getBoundingBox().left;

//            // comparing left cordinates
//            if (first < second) {
//                referenceBox = results.get(0).getBoundingBox();
//                unknownBox = results.get(1).getBoundingBox();
//                Log.d("Test", "reference1 :" + results.get(0).getCategories().get(0).getLabel());
//            } else{
//                referenceBox = results.get(1).getBoundingBox();
//                unknownBox = results.get(0).getBoundingBox();
////                Log.d("Test", "reference2 :" + results.get(1).getCategories().get(1).getLabel());
//            }
            detectSize = new DetectSize(context,referenceBox,unknownBox);
            detectSize.setContext(context);
        }


        for ( Detection detected: results) {

            RectF boundingBox = detected.getBoundingBox();

            drawBoundingBox = new DrawBoundingBox(surfaceHolder,boundingBox);

            for(Category labels: detected.getCategories()){
                String text = labels.getLabel();
                float confidence = labels.getScore();

                if(Stream){
                    drawBoundingBox.DrawBBoxforStream();
                }else{
                    drawBoundingBox.drawBBoxforPic();
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
                    case "b'Wrench_Head'":
                        feature = "Reference";
                }
//                if(confidence < 0.5){
//                    feature = "Not Identified";
//                }
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

    public void setFeature(String s){
        this.feature = s;
    }

    public void setProbability(String s){
        this.probability = s;
    }
    public void setStream(Boolean t){
        this.Stream = t;
    }

    public void setContext(Context c){
        context = c;
    }

}
