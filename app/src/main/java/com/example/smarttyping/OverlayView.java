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
        // ソフトウェアレンダリングを有効化（影の描画に必要）
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        linePaint.setColor(Color.GREEN);
        linePaint.setStrokeWidth(8f);
        linePaint.setStyle(Paint.Style.STROKE);

        pointPaint.setColor(Color.YELLOW);
        pointPaint.setStrokeWidth(15f);
        pointPaint.setStyle(Paint.Style.FILL);

        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(40f);
        textPaint.setFakeBoldText(true);
        textPaint.setShadowLayer(5f, 0f, 0f, Color.BLACK);

        keyPaint.setColor(Color.argb(180, 60, 60, 70)); // 少し濃いめの色
        keyPaint.setStyle(Paint.Style.FILL);
        keyPaint.setShadowLayer(15f, 0f, 10f, Color.BLACK); // 浮いている影

        keySelectedPaint.setColor(Color.argb(220, 255, 255, 100));
        keySelectedPaint.setStyle(Paint.Style.FILL);
        keySelectedPaint.setShadowLayer(20f, 0f, 0f, Color.YELLOW); // 選択時の光

        progressPaint.setColor(Color.argb(180, 0, 255, 120));
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
            // 押し込み度合い (0.0 〜 1.0)
            float pressProgress = 0f;
            if (key.isPressed) {
                pressProgress = currentPressScore / PRESS_RATIO_THRESHOLD;
                pressProgress = Math.max(0f, Math.min(pressProgress, 1.0f));
            }

            // 立体的な演出：押し込むほどサイズを少し小さくし、位置を下にずらす（奥に沈む表現）
            float scale = 1.0f - (pressProgress * 0.15f); // 最大15%縮小
            float offsetX = (key.width * (1 - scale)) / 2;
            float offsetY = (key.height * (1 - scale)) / 2 + (pressProgress * 15f); // 下にずらす

            float drawX = key.x + offsetX;
            float drawY = key.y + offsetY;
            float drawW = key.width * scale;
            float drawH = key.height * scale;

            // ボタン本体（角丸）
            canvas.drawRoundRect(drawX, drawY, drawX + drawW, drawY + drawH, 15f, 15f, key.isPressed ? keySelectedPaint : keyPaint);
            
            // 押し込み進行状況（ゲージ）
            if (key.isPressed && !key.hasTriggered) {
                canvas.drawRoundRect(drawX, drawY + drawH * (1 - pressProgress), drawX + drawW, drawY + drawH, 15f, 15f, progressPaint);
            }

            // 文字
            canvas.drawText(key.label, drawX + drawW * 0.3f, drawY + drawH * 0.7f, textPaint);
        }

        // デバッグ用：全体の押し込みスコアバー
        drawDepthBar(canvas);

        if (results == null || results.landmarks() == null) return;

        // 指先の描画
        drawFingerTips(canvas);
    }

    private void drawDepthBar(Canvas canvas) {
        float barWidth = 300f;
        float barHeight = 25f;
        float barX = (getWidth() - barWidth) / 2;
        float barY = 60f;
        float progress = currentPressScore / PRESS_RATIO_THRESHOLD;
        progress = Math.max(0f, Math.min(progress, 1.0f));
        
        Paint bgPaint = new Paint();
        bgPaint.setColor(Color.argb(100, 0, 0, 0));
        canvas.drawRoundRect(barX, barY, barX + barWidth, barY + barHeight, 10f, 10f, bgPaint);
        canvas.drawRoundRect(barX, barY, barX + (barWidth * progress), barY + barHeight, 10f, 10f, progressPaint);
        canvas.drawText("Press Depth", barX, barY - 15, textPaint);
    }

    private void drawFingerTips(Canvas canvas) {
        for (List<NormalizedLandmark> landmarks : results.landmarks()) {
            NormalizedLandmark tip = landmarks.get(8); // 人差し指
            float x = (1 - tip.x()) * getWidth();
            float y = tip.y() * getHeight();
            
            // 指先に光の輪をつける（AR風）
            Paint glowPaint = new Paint();
            glowPaint.setColor(Color.argb(100, 255, 255, 255));
            glowPaint.setStyle(Paint.Style.STROKE);
            glowPaint.setStrokeWidth(5f);
            canvas.drawCircle(x, y, 30f + (currentPressScore * 20f), glowPaint);
            canvas.drawCircle(x, y, 15f, pointPaint);
        }
    }
}
