package com.example.toolobjectdetection;

/*
This is to be completed.....

The goal of this class is to be able to make size comparisons
between a detected object and a reference object.
to do this we may use 2 different methods. these still need to be tested

Method 1
find native resolution of images (height X width)
get focal point value from metadata
convert pixels per millimeter
We need to know the pixels per millimeter(px/mm) on the image sensor.
f_x=f*m_x
f_y=f*m_y
solve for m_x and m_y. to solve average f_x and f_y to get f_xy.
m=f_xy/focal_length_of_camera
know the distance between object and camera.
Find the dimension of the image (height1 X width1)
Find the Object size in pixels
Determine the size of object in pixels.
use distance formula to find length of a selected line.
Convert px/mm in the lower resolution
pxpermm_in_lower_resolution = (width1*m)/width
Size of object in the image sensor
size_of_object_in_image_sensor = object_size_in_pixels/(pxpermm_in_lower_resolution)
Actual size of object
The actual size of object can be found with the above data as,
real_size = (dist*size_of_object_in_image_sensor)/focal_length

Method 2 solves finding distance from camera to detected object
find focal length per pixel
double focal_length_px = (size.width * 0.5) / tan(horizontalViewAngle * 0.5 * PI/180);
////////
solve for distance in Millimeters
get average distance between both objects in millimeters
not sure how to do this since the reference and tool object
will not have a set distance from each other.
////////

to get actual_distance_between_taillights_px.
prior to this step we would have detected the two objects
and find the center point for each.
normalize and subtract one another to get the distance in pixels between the two points.

float calc_distance_mm = _avg_distance_between_taillights_mm * focal_length_px / actual_distance_between_taillights_px;

 */


import android.content.Context;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SizeF;
import android.util.TypedValue;

import com.otaliastudios.cameraview.frame.Frame;
import com.otaliastudios.cameraview.size.Size;

import org.tensorflow.lite.task.vision.detector.Detection;

import java.util.Arrays;
import java.util.List;

import static java.lang.Math.PI;

public class DetectSize {

    RectF detectedBounds, referenceBounds;
    Double final_size;
    CameraCharacteristics info;
    Context context;
    SizeF sensorSize;
    float[] focalLength;


    public DetectSize(Context context, RectF referenceBounds,RectF detectedBounds){

        this.detectedBounds = detectedBounds;
        this.referenceBounds = referenceBounds;
        this.context = context;

        compare();
    }


    public void compare(){

        CameraManager manager =
                (CameraManager)context.getSystemService(Context.CAMERA_SERVICE);
        try{
            String[] cameraId = manager.getCameraIdList();
            info = manager.getCameraCharacteristics(cameraId[0]);
            sensorSize = info.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE);
            focalLength = info.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);

        }catch(CameraAccessException e){
            e.printStackTrace();
        }

        float R_WidthMm = (referenceBounds.width()/1440)*sensorSize.getWidth();


//        change distance in millimeters for correct calculations
//      distance = 304.8  // 1 foot
        double distance = 488.95;
        final_size= (distance/focalLength[0])* R_WidthMm;
        Log.d("Test","Final Size: "+ Math.round(final_size));


        

//        float sizeW = preview.getSize().getWidth();
////        double focal_length_px = (size.width * 0.5) / Math.tan(horizontalViewAngle * 0.5 * PI/180);
//        double focal_length_px = (sizeW * 0.5) / Math.tan(getHFOV(info)*0.5 * PI/180);

    }


//    //camera 2 method for deprecated horizontalViewAngle
//    private float getHFOV(CameraCharacteristics info) {
//        SizeF sensorSize = info.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE);
//
//        float[] focalLengths = info.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
//
//        if (focalLengths != null && focalLengths.length > 0) {
//            assert sensorSize != null;
//            return (float) (2.0f * Math.atan(sensorSize.getWidth() / (2.0f * focalLengths[0])));
//        }
//        return 1.1f;
//    }

    
    // convert pixels to millimeters
    public static float pxToMm(final float px, final Context context)
    {
        final DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return px / TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, 1, dm);
    }


    public void setReferenceBounds(RectF b){
        referenceBounds = b;
    }
    public void setDetectedBounds(RectF b){
        detectedBounds = b;
    }

    public void setInfo(CameraCharacteristics i){
        info = i;
    }

    public Double getObjectSize(){
        return final_size;
    }

    public void setContext(Context c){
        context = c;
    }


}


