package com.example.smarttyping;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult;
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;

import java.util.ArrayList;
import java.util.List;

public class OverlayView extends View {
    private static final String TAG = "OverlayView";
    private HandLandmarkerResult results;
    private Paint pointPaint = new Paint();
    private Paint textPaint = new Paint();
    private Paint keyPaint = new Paint();
    private Paint keySelectedPaint = new Paint();
    private Paint progressPaint = new Paint();
    private Paint borderPaint = new Paint();

    public interface KeyEventListener {
        void onKeyClicked(String label);
    }

    private KeyEventListener keyEventListener;

    public void setKeyEventListener(KeyEventListener listener) {
        this.keyEventListener = listener;
    }

    private static class VirtualKey {
        String label;
        float[] pointsX;
        float[] pointsY;
        Path path = new Path();
        boolean isPressed = false;
        boolean hasTriggered = false;

        VirtualKey(String label, float[] x, float[] y) {
            this.label = label;
            this.pointsX = x;
            this.pointsY = y;
        }

        void updatePath(float yOffset) {
            path.reset();
            // Y座標を正しく計算（最小Y値81をベース0に）
            float minY = 81f;
            path.moveTo(pointsX[0], pointsY[0] - minY + yOffset);
            for (int i = 1; i < pointsX.length; i++) {
                path.lineTo(pointsX[i], pointsY[i] - minY + yOffset);
            }
            path.close();
        }

        boolean contains(float px, float py) {
            RectF bounds = new RectF();
            path.computeBounds(bounds, true);
            Region region = new Region();
            region.setPath(path, new Region((int) bounds.left, (int) bounds.top, (int) bounds.right, (int) bounds.bottom));
            return region.contains((int) px, (int) py);
        }
    }

    private List<VirtualKey> keys = new ArrayList<>();
    private static final float PRESS_RATIO_THRESHOLD = -0.35f;
    private static final float RELEASE_RATIO_THRESHOLD = -0.20f;
    private float currentPressScore = 0f;

    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.d(TAG, "OverlayView constructor called");
        initPaints();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.d(TAG, "onAttachedToWindow: width=" + getWidth() + ", height=" + getHeight());
    }

    private void initPaints() {
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        pointPaint.setColor(Color.YELLOW);
        pointPaint.setStrokeWidth(15f);
        pointPaint.setStyle(Paint.Style.FILL);

        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(30f);
        textPaint.setFakeBoldText(true);
        textPaint.setShadowLayer(5f, 0f, 0f, Color.BLACK);

        keyPaint.setColor(Color.argb(180, 60, 60, 70));
        keyPaint.setStyle(Paint.Style.FILL);
        keyPaint.setShadowLayer(15f, 0f, 10f, Color.BLACK);

        keySelectedPaint.setColor(Color.argb(220, 255, 255, 100));
        keySelectedPaint.setStyle(Paint.Style.FILL);
        keySelectedPaint.setShadowLayer(20f, 0f, 0f, Color.YELLOW);

        progressPaint.setColor(Color.argb(180, 0, 255, 120));
        progressPaint.setStyle(Paint.Style.FILL);

        borderPaint.setColor(Color.WHITE);
        borderPaint.setStrokeWidth(2f);
        borderPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.d(TAG, "onSizeChanged: w=" + w + ", h=" + h + ", oldw=" + oldw + ", oldh=" + oldh);
        if (w > 0 && h > 0) {
            initKeysWithDimensions(w, h);
        }
    }

    private void initKeys() {
        initKeysWithDimensions(getWidth(), getHeight());
    }

    private void initKeysWithDimensions(int width, int height) {
        if (width <= 0 || height <= 0) {
            Log.d(TAG, "initKeysWithDimensions: invalid dimensions - width=" + width + ", height=" + height);
            return;
        }
        Log.d(TAG, "initKeysWithDimensions called with width=" + width + ", height=" + height);
        keys.clear();

        float yOffset = height * 0.4f; // 画面下部に配置
        addKey("-", new float[]{537, 555, 613, 640}, new float[]{137, 127, 128, 138}, yOffset);
        // Row 1
        addKey("Q", new float[]{  0,  32,  81,  56}, new float[]{117, 108, 109, 119}, yOffset);
        addKey("W", new float[]{ 56,  81, 133, 112}, new float[]{119, 108, 109, 119}, yOffset);
        addKey("E", new float[]{112, 133, 183, 169}, new float[]{119, 110, 111, 120}, yOffset);
        addKey("R", new float[]{169, 183, 238, 228}, new float[]{120, 111, 112, 121}, yOffset);
        addKey("T", new float[]{228, 238, 290, 286}, new float[]{121, 112, 112, 122}, yOffset);
        addKey("Y", new float[]{286, 290, 343, 346}, new float[]{122, 112, 113, 124}, yOffset);
        addKey("U", new float[]{346, 343, 396, 403}, new float[]{124, 113, 115, 124}, yOffset);
        addKey("I", new float[]{403, 396, 450, 462}, new float[]{124, 115, 116, 124}, yOffset);
        addKey("O", new float[]{462, 450, 508, 525}, new float[]{124, 116, 118, 127}, yOffset);
        addKey("P", new float[]{525, 508, 560, 585}, new float[]{127, 118, 118, 127}, yOffset);

        // Row 2
        float r2o = 30;
        addKey("A", new float[]{ 47,  72, 118,  98}, new float[]{108, 101, 103, 110}, yOffset + r2o);
        addKey("S", new float[]{ 98, 118, 165, 150}, new float[]{110, 103, 103, 110}, yOffset + r2o);
        addKey("D", new float[]{150, 165, 213, 202}, new float[]{110, 103, 103, 112}, yOffset + r2o);
        addKey("F", new float[]{202, 213, 260, 251}, new float[]{112, 103, 103, 113}, yOffset + r2o);
        addKey("G", new float[]{251, 260, 304, 303}, new float[]{113, 103, 104, 112}, yOffset + r2o);
        addKey("H", new float[]{303, 304, 354, 355}, new float[]{112, 104, 105, 113}, yOffset + r2o);
        addKey("J", new float[]{355, 354, 402, 408}, new float[]{113, 105, 104, 115}, yOffset + r2o);
        addKey("K", new float[]{408, 402, 450, 460}, new float[]{115, 104, 104, 117}, yOffset + r2o);
        addKey("L", new float[]{460, 450, 497, 514}, new float[]{117, 104, 105, 118}, yOffset + r2o);

        // Row 3
        float r3o = 60;
        addKey("Z", new float[]{ 97, 116, 158, 143}, new float[]{102,  95,  96, 103}, yOffset + r3o);
        addKey("X", new float[]{143, 158, 201, 189}, new float[]{103,  96,  95, 104}, yOffset + r3o);
        addKey("C", new float[]{189, 201, 245, 238}, new float[]{104,  95,  95, 103}, yOffset + r3o);
        addKey("V", new float[]{238, 245, 288, 284}, new float[]{103,  95,  95, 104}, yOffset + r3o);
        addKey("B", new float[]{284, 288, 331, 332}, new float[]{104,  95,  94, 105}, yOffset + r3o);
        addKey("N", new float[]{332, 331, 375, 379}, new float[]{105,  94,  94, 105}, yOffset + r3o);
        addKey("M", new float[]{379, 375, 420, 428}, new float[]{105,  94,  95, 105}, yOffset + r3o);
        addKey(",", new float[]{428, 420, 466, 477}, new float[]{105,  95,  97, 104}, yOffset + r3o);
        addKey(".", new float[]{477, 466, 510, 528}, new float[]{104,  97,  97, 106}, yOffset + r3o);
    }

    private void addKey(String label, float[] x, float[] y, float yOffset) {
        VirtualKey key = new VirtualKey(label, x, y);
        key.updatePath(yOffset);
        keys.add(key);
    }

    public void setResults(HandLandmarkerResult results, int imageWidth, int imageHeight) {
        this.results = results;
        Log.d(TAG, "setResults called: width=" + getWidth() + ", height=" + getHeight() + ", keys.size=" + keys.size());

        if (keys.isEmpty() && getWidth() > 0 && getHeight() > 0) {
            Log.d(TAG, "setResults: Initializing keys");
            initKeys();
        }

        for (VirtualKey key : keys) key.isPressed = false;

        if (results != null && results.landmarks() != null && !results.landmarks().isEmpty()) {
            for (List<NormalizedLandmark> hand : results.landmarks()) {
                float dx = hand.get(9).x() - hand.get(0).x();
                float dy = hand.get(9).y() - hand.get(0).y();
                float handScale = (float) Math.sqrt(dx * dx + dy * dy);
                if (handScale < 0.01f) handScale = 0.01f;

                currentPressScore = (hand.get(8).z() - hand.get(5).z()) / handScale;
                float x = (1 - hand.get(8).x()) * getWidth();
                float y = hand.get(8).y() * getHeight();

                for (VirtualKey key : keys) {
                    if (key.contains(x, y)) {
                        key.isPressed = true;
                        if (currentPressScore < PRESS_RATIO_THRESHOLD && !key.hasTriggered) {
                            if (keyEventListener != null) keyEventListener.onKeyClicked(key.label);
                            key.hasTriggered = true;
                        }
                    }
                }
            }
        }

        if (currentPressScore > RELEASE_RATIO_THRESHOLD) {
            for (VirtualKey key : keys) key.hasTriggered = false;
        }
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float tx = event.getX();
        float ty = event.getY();
        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
            for (VirtualKey key : keys) {
                if (key.contains(tx, ty)) {
                    key.isPressed = true;
                    if (event.getAction() == MotionEvent.ACTION_DOWN && keyEventListener != null) {
                        keyEventListener.onKeyClicked(key.label);
                    }
                } else {
                    key.isPressed = false;
                }
            }
        } else {
            for (VirtualKey key : keys) key.isPressed = false;
        }
        invalidate();
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d(TAG, "onDraw called: width=" + getWidth() + ", height=" + getHeight() + ", keys.size=" + keys.size());


        if (keys.isEmpty()) {
            Log.d(TAG, "onDraw: keys are empty, calling initKeys()");
            initKeys();
        }
        if (keys.isEmpty()) {
            Log.w(TAG, "onDraw: keys still empty after initialization!");
            return;
        }

        for (VirtualKey key : keys) {
            float progress = Math.max(0f, Math.min(currentPressScore / PRESS_RATIO_THRESHOLD, 1.0f));
            canvas.drawPath(key.path, key.isPressed ? keySelectedPaint : keyPaint);
            canvas.drawPath(key.path, borderPaint);

            RectF b = new RectF();
            key.path.computeBounds(b, true);
            canvas.drawText(key.label, b.centerX() - 10, b.centerY() + 10, textPaint);

            if (key.isPressed && !key.hasTriggered) {
                canvas.save();
                canvas.clipPath(key.path);
                canvas.drawRect(b.left, b.bottom - b.height() * progress, b.right, b.bottom, progressPaint);
                canvas.restore();
            }
        }

        drawDepthBar(canvas);
        if (results != null && results.landmarks() != null) drawFingerTips(canvas);
    }

    private void drawDepthBar(Canvas canvas) {
        float bw = 300f, bh = 20f;
        float bx = (getWidth() - bw) / 2, by = 50f;
        float p = Math.max(0f, Math.min(currentPressScore / PRESS_RATIO_THRESHOLD, 1.0f));
        Paint bg = new Paint(); bg.setColor(Color.argb(100, 0, 0, 0));
        canvas.drawRect(bx, by, bx + bw, by + bh, bg);
        canvas.drawRect(bx, by, bx + bw * p, by + bh, progressPaint);
    }

    private void drawFingerTips(Canvas canvas) {
        for (List<NormalizedLandmark> landmarks : results.landmarks()) {
            NormalizedLandmark tip = landmarks.get(8);
            float x = (1 - tip.x()) * getWidth();
            float y = tip.y() * getHeight();
            canvas.drawCircle(x, y, 15f, pointPaint);
        }
    }
}
