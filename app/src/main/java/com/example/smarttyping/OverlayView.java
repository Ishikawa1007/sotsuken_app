package com.example.smarttyping;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult;
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;

import java.util.List;

public class OverlayView extends View {
    private HandLandmarkerResult results;
    private Paint linePaint = new Paint();
    private Paint pointPaint = new Paint();
    private Paint textPaint = new Paint();

    private float scaleFactor = 1f;
    private int imageWidth = 1;
    private int imageHeight = 1;

    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaints();
    }

    private void initPaints() {
        linePaint.setColor(Color.GREEN);
        linePaint.setStrokeWidth(8f);
        linePaint.setStyle(Paint.Style.STROKE);

        pointPaint.setColor(Color.YELLOW);
        pointPaint.setStrokeWidth(15f);
        pointPaint.setStyle(Paint.Style.FILL);

        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(30f);
        textPaint.setShadowLayer(5f, 0f, 0f, Color.BLACK);
    }

    public void setResults(HandLandmarkerResult results, int imageWidth, int imageHeight) {
        this.results = results;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;

        // PreviewViewのサイズに合わせてスケーリングを計算
        scaleFactor = Math.max(getWidth() * 1f / imageWidth, getHeight() * 1f / imageHeight);
        invalidate(); // 再描画を指示
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (results == null || results.landmarks() == null) return;

        for (List<NormalizedLandmark> landmarks : results.landmarks()) {
            // 指先（4, 8, 12, 16, 20）にマークをつける
            int[] fingerTips = {4, 8, 12, 16, 20};
            
            for (int idx : fingerTips) {
                NormalizedLandmark landmark = landmarks.get(idx);
                float x = landmark.x() * getWidth();
                float y = landmark.y() * getHeight();

                // 円を描画
                canvas.drawCircle(x, y, 20f, pointPaint);

                // 座標テキストを表示
                String coords = String.format("(%.2f, %.2f)", landmark.x(), landmark.y());
                canvas.drawText(coords, x + 25, y, textPaint);
            }
        }
    }
}
