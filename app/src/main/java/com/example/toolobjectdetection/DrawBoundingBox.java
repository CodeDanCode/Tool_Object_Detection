package com.example.toolobjectdetection;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


public class DrawBoundingBox{

    public SurfaceHolder surfaceHolder;
    private RectF bounds;

    public DrawBoundingBox(SurfaceHolder surfaceHolder,RectF bounds){
        this.bounds = bounds;
        this.surfaceHolder = surfaceHolder;
    }

    public void DrawBB() {
        try {
            Canvas canvas = surfaceHolder.lockCanvas();
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.BLUE);
            paint.setStrokeWidth(5);
            canvas.drawRect(bounds, paint);
            surfaceHolder.unlockCanvasAndPost(canvas);

        }catch (Throwable e){
            Log.d("Test","Error in drawing");
        }
    }
}
