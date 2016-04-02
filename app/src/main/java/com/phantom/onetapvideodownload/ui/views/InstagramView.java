package com.phantom.onetapvideodownload.ui.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

public class InstagramView extends View {
    String colors[] = new String[] {"#5f2c82", "#49a09d"};
    private final Paint mPaint = new Paint(2);
    private final Matrix matrix = new Matrix();

    private Bitmap bitmap;
    private int rotationAmount = 0;

    public InstagramView(Context context, AttributeSet attrs) {
        super(context, attrs);
        generateBitmap();
    }

    public void generateBitmap() {
        Paint paint = new Paint();
        bitmap = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888);
        Shader mShader = new LinearGradient(0, 0, 100, 70, new int[] {
                Color.parseColor(colors[0]), Color.parseColor(colors[1]) },
                null, Shader.TileMode.MIRROR);  // CLAMP MIRROR REPEAT

        Canvas canvas = new Canvas(bitmap);
        paint.setShader(mShader);
        canvas.drawPaint(paint);
    }

    @Override
    public void onDraw(Canvas paramCanvas) {
        matrix.reset();
        int canvasWidth = paramCanvas.getWidth();
        int canvasHeight = paramCanvas.getHeight();

        // Radius of smallest circle which can contain circle of canvas's width and height
        double circleRadius = Math.sqrt(canvasWidth*canvasWidth + canvasHeight*canvasHeight);
        float bitmapHalfWidth = bitmap.getWidth() / 2;
        float bitmapHalfHeight = bitmap.getHeight() / 2;

        // Center of circle
        float centerX = canvasWidth/2;
        float centerY = canvasHeight/2;

        // Translation amounts for origin
        float translatedX = centerX - bitmapHalfWidth;
        float translatedY = centerY - bitmapHalfHeight;
        matrix.preTranslate(translatedX, translatedY);

        rotationAmount++;
        rotationAmount %= 360;
        matrix.postRotate(rotationAmount, centerX, centerY);

        // scalingFactor = Diameter/bitmapDimension;
        float xScalingFactor = (float)circleRadius/bitmapHalfWidth;
        float yScalingFactor = (float)circleRadius/bitmapHalfHeight;

        matrix.postScale(xScalingFactor, yScalingFactor, centerX, centerY);
        paramCanvas.drawBitmap(bitmap, matrix, mPaint);
        invalidate();
    }

}
