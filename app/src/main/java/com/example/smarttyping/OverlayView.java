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

        void updatePath(float scale, float yOffset) {
            path.reset();
            path.moveTo(pointsX[0] * scale, (pointsY[0] + 120f) * scale + yOffset);
            for (int i = 1; i < pointsX.length; i++) {
                path.lineTo(pointsX[i] * scale, (pointsY[i] + 120f) * scale + yOffset);
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
        initPaints();
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

        float scale = width / 640f;
        float yOffset = height * 0.4f; // 画面下部に配置

        // Row 1
        addKey("Q", new float[]{0, 28, 84, 58}, new float[]{101, 95, 95, 105}, scale, yOffset);
        addKey("W", new float[]{58, 84, 143, 121}, new float[]{105, 95, 96, 106}, scale, yOffset);
        addKey("E", new float[]{121, 143, 201, 187}, new float[]{106, 96, 96, 106}, scale, yOffset);
        addKey("R", new float[]{187, 201, 258, 253}, new float[]{106, 96, 96, 107}, scale, yOffset);
        addKey("T", new float[]{253, 258, 317, 317}, new float[]{107, 96, 96, 108}, scale, yOffset);
        addKey("Y", new float[]{317, 317, 373, 382}, new float[]{108, 96, 98, 108}, scale, yOffset);
        addKey("U", new float[]{382, 373, 434, 447}, new float[]{108, 98, 99, 110}, scale, yOffset);
        addKey("I", new float[]{447, 434, 493, 515}, new float[]{110, 99, 99, 111}, scale, yOffset);
        addKey("O", new float[]{515, 493, 551, 579}, new float[]{111, 99, 101, 111}, scale, yOffset);
        addKey("P", new float[]{579, 551, 608, 640}, new float[]{111, 101, 101, 111}, scale, yOffset);

        // Row 2
        float r2o = 30 * scale;
        addKey("A", new float[]{45, 70, 120, 101}, new float[]{95, 87, 88, 96}, scale, yOffset + r2o);
        addKey("S", new float[]{101, 120, 172, 159}, new float[]{96, 88, 88, 96}, scale, yOffset + r2o);
        addKey("D", new float[]{159, 172, 225, 215}, new float[]{96, 88, 88, 96}, scale, yOffset + r2o);
        addKey("F", new float[]{215, 225, 277, 272}, new float[]{96, 88, 88, 97}, scale, yOffset + r2o);
        addKey("G", new float[]{272, 277, 328, 329}, new float[]{97, 88, 89, 97}, scale, yOffset + r2o);
        addKey("H", new float[]{329, 328, 379, 387}, new float[]{97, 89, 90, 98}, scale, yOffset + r2o);
        addKey("J", new float[]{387, 379, 432, 445}, new float[]{98, 90, 91, 99}, scale, yOffset + r2o);
        addKey("K", new float[]{445, 432, 485, 504}, new float[]{99, 91, 91, 100}, scale, yOffset + r2o);
        addKey("L", new float[]{504, 485, 538, 560}, new float[]{100, 91, 93, 101}, scale, yOffset + r2o);

        // Row 3
        float r3o = 60 * scale;
        addKey("Z", new float[]{93, 112, 158, 145}, new float[]{87, 81, 81, 88}, scale, yOffset + r3o);
        addKey("X", new float[]{145, 188, 205, 197}, new float[]{88, 81, 82, 89}, scale, yOffset + r3o);
        addKey("C", new float[]{197, 205, 253, 247}, new float[]{89, 82, 82, 89}, scale, yOffset + r3o);
        addKey("V", new float[]{247, 253, 299, 299}, new float[]{89, 82, 81, 88}, scale, yOffset + r3o);
        addKey("B", new float[]{299, 299, 346, 349}, new float[]{88, 81, 83, 90}, scale, yOffset + r3o);
        addKey("N", new float[]{349, 346, 392, 401}, new float[]{90, 83, 84, 90}, scale, yOffset + r3o);
        addKey("M", new float[]{401, 392, 441, 452}, new float[]{90, 84, 84, 91}, scale, yOffset + r3o);
        addKey("←", new float[]{452, 441, 558, 560}, new float[]{91, 84, 86, 93}, scale, yOffset + r3o);
    }

    private void addKey(String label, float[] x, float[] y, float scale, float yOffset) {
        VirtualKey key = new VirtualKey(label, x, y);
        key.updatePath(scale, yOffset);
        keys.add(key);
    }

    public void setResults(HandLandmarkerResult results, int imageWidth, int imageHeight) {
        this.results = results;
        if (keys.isEmpty() && getWidth() > 0 && getHeight() > 0) {
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
