package com.example.smarttyping;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.opengl.Matrix;
import android.util.AttributeSet;
import android.view.MotionEvent;
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

    // AR用座標変換行列
    private float[] viewMatrix = new float[16];
    private float[] projectionMatrix = new float[16];
    private boolean arMode = false;
    private float[] planePoseMatrix = new float[16];
    private Paint borderPaint = new Paint();

    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaints();
    }

    public void setArMatrices(float[] viewMatrix, float[] projectionMatrix, float[] planePoseMatrix) {
        System.arraycopy(viewMatrix, 0, this.viewMatrix, 0, 16);
        System.arraycopy(projectionMatrix, 0, this.projectionMatrix, 0, 16);
        System.arraycopy(planePoseMatrix, 0, this.planePoseMatrix, 0, 16);
        this.arMode = true;
        invalidate();
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

        borderPaint.setColor(Color.WHITE);
        borderPaint.setStrokeWidth(3f);
        borderPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initKeys();
    }

    private void initKeys() {
        if (getWidth() == 0 || getHeight() == 0) return;
        keys.clear();
        
        if (arMode) {
            // ARモード：平面上の相対座標で配置（メートル単位）
            float kw = 0.04f;  // キー幅 4cm
            float kh = 0.04f;  // キー高 4cm
            float m = 0.005f;  // 隙間 0.5cm
            
            // 1段目
            String[] row1 = {"Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"};
            for (int i = 0; i < row1.length; i++) {
                keys.add(new VirtualKey(row1[i], i * (kw + m) - 0.22f, -0.1f, kw, kh));
            }
            // 2段目
            String[] row2 = {"A", "S", "D", "F", "G", "H", "J", "K", "L"};
            for (int i = 0; i < row2.length; i++) {
                keys.add(new VirtualKey(row2[i], i * (kw + m) - 0.20f, 0.0f, kw, kh));
            }
            // 3段目
            String[] row3 = {"Z", "X", "C", "V", "B", "N", "M", "←"};
            for (int i = 0; i < row3.length; i++) {
                keys.add(new VirtualKey(row3[i], i * (kw + m) - 0.18f, 0.1f, kw, kh));
            }
        } else {
            // 通常モード
            float margin = 10f;
            int cols = 10;
            float keyWidth = (getWidth() - margin * (cols + 1)) / cols;
            float keyHeight = 80f; // 120f から 80f に縮小
            float startY = getHeight() - 350f; // 全体が低くなりすぎるのを防ぐため開始位置を調整

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
    }

    private float[] projectPoint(float localX, float localZ) {
        float[] localPos = {localX, 0, localZ, 1.0f};
        float[] worldPos = new float[4];
        float[] viewPos = new float[4];
        float[] clipPos = new float[4];

        Matrix.multiplyMV(worldPos, 0, planePoseMatrix, 0, localPos, 0);
        Matrix.multiplyMV(viewPos, 0, viewMatrix, 0, worldPos, 0);
        Matrix.multiplyMV(clipPos, 0, projectionMatrix, 0, viewPos, 0);

        if (clipPos[3] == 0) return null;
        
        float ndcX = clipPos[0] / clipPos[3];
        float ndcY = clipPos[1] / clipPos[3];

        float screenX = (ndcX + 1.0f) * 0.5f * getWidth();
        float screenY = (1.0f - ndcY) * 0.5f * getHeight();

        return new float[]{screenX, screenY};
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
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                for (VirtualKey key : keys) {
                    boolean hit = false;
                    if (arMode) {
                        // ARモード：投影後の範囲で判定
                        float[] p1 = projectPoint(key.x, key.y);
                        float[] p2 = projectPoint(key.x + key.width, key.y + key.height);
                        if (p1 != null && p2 != null) {
                            // 簡略化：矩形範囲で判定
                            float left = Math.min(p1[0], p2[0]);
                            float right = Math.max(p1[0], p2[0]);
                            float top = Math.min(p1[1], p2[1]);
                            float bottom = Math.max(p1[1], p2[1]);
                            hit = touchX >= left && touchX <= right && touchY >= top && touchY <= bottom;
                        }
                    } else {
                        hit = key.contains(touchX, touchY);
                    }

                    if (hit) {
                        key.isPressed = true;
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            if (keyEventListener != null) {
                                keyEventListener.onKeyClicked(key.label);
                            }
                        }
                    } else {
                        key.isPressed = false;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                for (VirtualKey key : keys) {
                    key.isPressed = false;
                }
                break;
        }
        invalidate();
        return true;
    }

    private void drawArGrid(Canvas canvas) {
        Paint gridPaint = new Paint();
        gridPaint.setColor(Color.argb(50, 255, 255, 255));
        gridPaint.setStrokeWidth(2f);
        gridPaint.setStyle(Paint.Style.STROKE);

        float size = 0.5f; // 50cmの範囲にグリッドを描く
        int lines = 10;
        float step = size / lines;

        for (int i = 0; i <= lines; i++) {
            float pos = -size/2 + i * step;
            
            // X方向の線
            float[] p1 = projectPoint(-size/2, pos);
            float[] p2 = projectPoint(size/2, pos);
            if (p1 != null && p2 != null) canvas.drawLine(p1[0], p1[1], p2[0], p2[1], gridPaint);

            // Z方向の線
            float[] p3 = projectPoint(pos, -size/2);
            float[] p4 = projectPoint(pos, size/2);
            if (p3 != null && p4 != null) canvas.drawLine(p3[0], p3[1], p4[0], p4[1], gridPaint);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (keys.isEmpty()) initKeys();

        if (arMode) {
            drawArGrid(canvas);
        } else {
            // 通常モード時のみソフトウェアレンダリング（影用）を使用
            setLayerType(LAYER_TYPE_SOFTWARE, null);
        }

        for (VirtualKey key : keys) {
            if (arMode) {
                // ARモード：3D投影描画
                float[][] corners = {
                    {key.x, key.y}, 
                    {key.x + key.width, key.y}, 
                    {key.x + key.width, key.y + key.height}, 
                    {key.x, key.y + key.height}
                };
                
                Path path = new Path();
                float centerX = 0, centerY = 0;
                int validPoints = 0;

                for (int i = 0; i < 4; i++) {
                    float[] p = projectPoint(corners[i][0], corners[i][1]);
                    if (p != null) {
                        if (validPoints == 0) path.moveTo(p[0], p[1]);
                        else path.lineTo(p[0], p[1]);
                        centerX += p[0];
                        centerY += p[1];
                        validPoints++;
                    }
                }

                if (validPoints == 4) {
                    path.close();
                    canvas.drawPath(path, key.isPressed ? keySelectedPaint : keyPaint);
                    canvas.drawPath(path, borderPaint); // 枠線を追加してクッキリさせる
                    canvas.drawText(key.label, centerX / 4 - 10, centerY / 4 + 10, textPaint);
                }
            } else {
                // 通常モード（既存）
                // ... (通常モードの描画ロジックは維持)
                // 通常モード：ピクセル描画（既存のロジック）
                float pressProgress = 0f;
                if (key.isPressed) {
                    pressProgress = currentPressScore / PRESS_RATIO_THRESHOLD;
                    pressProgress = Math.max(0f, Math.min(pressProgress, 1.0f));
                }

                float scale = 1.0f - (pressProgress * 0.15f);
                float offsetX = (key.width * (1 - scale)) / 2;
                float offsetY = (key.height * (1 - scale)) / 2 + (pressProgress * 15f);

                float drawX = key.x + offsetX;
                float drawY = key.y + offsetY;
                float drawW = key.width * scale;
                float drawH = key.height * scale;

                canvas.drawRoundRect(drawX, drawY, drawX + drawW, drawY + drawH, 15f, 15f, key.isPressed ? keySelectedPaint : keyPaint);
                
                if (key.isPressed && !key.hasTriggered) {
                    canvas.drawRoundRect(drawX, drawY + drawH * (1 - pressProgress), drawX + drawW, drawY + drawH, 15f, 15f, progressPaint);
                }

                canvas.drawText(key.label, drawX + drawW * 0.3f, drawY + drawH * 0.75f, textPaint);
            }
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
