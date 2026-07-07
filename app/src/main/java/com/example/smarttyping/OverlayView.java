package com.example.smarttyping;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult;
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;

import java.util.ArrayList;
import java.util.List;

public class OverlayView extends View {
    private HandLandmarkerResult results;
    private Paint linePaint = new Paint();
    private Paint pointPaint = new Paint();
    private Paint textPaint = new Paint();
    private Paint keyPaint = new Paint();
    private Paint keySelectedPaint = new Paint();
    private Paint progressPaint = new Paint();

    private float scaleFactor = 1f;
    private int imageWidth = 1;
    private int imageHeight = 1;

    // キーイベントリスナー
    public interface KeyEventListener {
        void onKeyClicked(String label);
    }

    private KeyEventListener keyEventListener;

    public void setKeyEventListener(KeyEventListener listener) {
        this.keyEventListener = listener;
    }

    // キーボードの定義
    private static class VirtualKey {
        String label;
        float x, y, width, height;
        boolean isPressed = false;
        boolean hasTriggered = false;

        VirtualKey(String label, float x, float y, float width, float height) {
            this.label = label;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        boolean contains(float px, float py) {
            return px >= x && px <= x + width && py >= y && py <= y + height;
        }
    }

    private List<VirtualKey> keys = new ArrayList<>();
    private static final float PRESS_RATIO_THRESHOLD = -0.35f; // 手のサイズに対する突き出し率のしきい値
    private float currentPressScore = 0f;

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
        textPaint.setTextSize(40f);
        textPaint.setShadowLayer(5f, 0f, 0f, Color.BLACK);

        keyPaint.setColor(Color.argb(128, 200, 200, 200));
        keyPaint.setStyle(Paint.Style.FILL);

        keySelectedPaint.setColor(Color.argb(200, 255, 255, 0));
        keySelectedPaint.setStyle(Paint.Style.FILL);

        progressPaint.setColor(Color.argb(150, 0, 255, 0));
        progressPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initKeys();
    }

    private void initKeys() {
        if (getWidth() == 0 || getHeight() == 0) return;
        keys.clear();
        
        float margin = 10f;
        int cols = 10;
        float keyWidth = (getWidth() - margin * (cols + 1)) / cols;
        float keyHeight = 120f;
        float startY = getHeight() - 450f;

        String[] row1 = {"Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"};
        for (int i = 0; i < row1.length; i++) {
            keys.add(new VirtualKey(row1[i], margin + i * (keyWidth + margin), startY, keyWidth, keyHeight));
        }

        String[] row2 = {"A", "S", "D", "F", "G", "H", "J", "K", "L"};
        float row2Offset = keyWidth / 2;
        for (int i = 0; i < row2.length; i++) {
            keys.add(new VirtualKey(row2[i], margin + row2Offset + i * (keyWidth + margin), startY + keyHeight + margin, keyWidth, keyHeight));
        }

        String[] row3 = {"Z", "X", "C", "V", "B", "N", "M", "←"};
        float row3Offset = keyWidth;
        for (int i = 0; i < row3.length; i++) {
            keys.add(new VirtualKey(row3[i], margin + row3Offset + i * (keyWidth + margin), startY + 2 * (keyHeight + margin), keyWidth, keyHeight));
        }
    }

    public void setResults(HandLandmarkerResult results, int imageWidth, int imageHeight) {
        this.results = results;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;

        if (keys.isEmpty()) {
            initKeys();
        }

        // 判定のリセット
        for (VirtualKey key : keys) {
            key.isPressed = false;
        }

        if (results != null && results.landmarks() != null && !results.landmarks().isEmpty()) {
            for (List<NormalizedLandmark> hand : results.landmarks()) {
                // 1. 手の基準長を計算 (手首[0]から中指付け根[9]までの距離)
                float dx = hand.get(9).x() - hand.get(0).x();
                float dy = hand.get(9).y() - hand.get(0).y();
                float handScale = (float) Math.sqrt(dx * dx + dy * dy);
                if (handScale < 0.01f) handScale = 0.01f; // ゼロ除算防止

                // 2. 人差し指の付け根[5]と先端[8]のZ軸の差を計算
                float tipZ = hand.get(8).z();
                float knuckleZ = hand.get(5).z();
                float zDiff = tipZ - knuckleZ;

                // 3. 突き出しスコアを算出
                currentPressScore = zDiff / handScale;

                // 指のX,Y座標（判定用）
                float x = (1 - hand.get(8).x()) * getWidth();
                float y = hand.get(8).y() * getHeight();

                for (VirtualKey key : keys) {
                    if (key.contains(x, y)) {
                        key.isPressed = true;
                        // スコアがしきい値を超えた（カメラに近づいた）らクリック
                        if (currentPressScore < PRESS_RATIO_THRESHOLD && !key.hasTriggered) {
                            if (keyEventListener != null) {
                                keyEventListener.onKeyClicked(key.label);
                            }
                            key.hasTriggered = true;
                        }
                    }
                }
            }
        }

        // リセット判定（指を引いたら次の入力が可能に）
        if (currentPressScore > PRESS_RATIO_THRESHOLD + 0.1f) {
            for (VirtualKey key : keys) {
                key.hasTriggered = false;
            }
        }

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // キーボードの描画
        if (keys.isEmpty()) initKeys();
        for (VirtualKey key : keys) {
            canvas.drawRect(key.x, key.y, key.x + key.width, key.y + key.height, key.isPressed ? keySelectedPaint : keyPaint);
            
            // 押し込み進行状況の描画（選択中のキーのみ）
            if (key.isPressed && !key.hasTriggered) {
                // スコア 0.0(平ら) 〜 PRESS_RATIO_THRESHOLD(クリック) の範囲を 0〜1 に変換
                float progress = currentPressScore / PRESS_RATIO_THRESHOLD;
                progress = Math.max(0f, Math.min(progress, 1.0f));
                canvas.drawRect(key.x, key.y + key.height * (1 - progress), key.x + key.width, key.y + key.height, progressPaint);
            }

            canvas.drawText(key.label, key.x + key.width / 4, key.y + key.height * 0.7f, textPaint);
        }

        // デバッグ用：全体の押し込みスコアバーを画面上部に表示
        float barWidth = 300f;
        float barHeight = 20f;
        float barX = (getWidth() - barWidth) / 2;
        float barY = 50f;
        float progress = currentPressScore / PRESS_RATIO_THRESHOLD;
        progress = Math.max(0f, Math.min(progress, 1.0f));
        
        canvas.drawRect(barX, barY, barX + barWidth, barY + barHeight, keyPaint); // 背景
        canvas.drawRect(barX, barY, barX + (barWidth * progress), barY + barHeight, progressPaint); // 進捗
        canvas.drawText("Press Depth", barX, barY - 10, textPaint);

        if (results == null || results.landmarks() == null) return;

        for (List<NormalizedLandmark> landmarks : results.landmarks()) {
            int[] fingerTips = {4, 8, 12, 16, 20};
            for (int idx : fingerTips) {
                NormalizedLandmark landmark = landmarks.get(idx);
                float x = (1 - landmark.x()) * getWidth();
                float y = landmark.y() * getHeight();
                canvas.drawCircle(x, y, 20f, pointPaint);
            }
        }
    }
}
