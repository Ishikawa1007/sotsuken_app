package com.example.smarttyping;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.util.Log;

import com.google.mediapipe.framework.image.BitmapImageBuilder;
import com.google.mediapipe.framework.image.MPImage;
import com.google.mediapipe.tasks.core.BaseOptions;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker;
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult;

public class HandLandmarkerHelper {
    private static final String TAG = "HandLandmarkerHelper";
    private static final String MP_HAND_LANDMARKER_TASK = "hand_landmarker.task";

    private HandLandmarker handLandmarker;
    private final Context context;
    private final LandmarkerListener listener;

    public interface LandmarkerListener {
        void onError(String error);
        void onResults(HandLandmarkerResult result, MPImage image);
    }

    public HandLandmarkerHelper(Context context, LandmarkerListener listener) {
        this.context = context.getApplicationContext();
        this.listener = listener;
        setupHandLandmarker();
    }

    private void setupHandLandmarker() {
        try {
            BaseOptions.Builder baseOptionsBuilder = BaseOptions.builder()
                    .setModelAssetPath(MP_HAND_LANDMARKER_TASK);

            HandLandmarker.HandLandmarkerOptions options = HandLandmarker.HandLandmarkerOptions.builder()
                    .setBaseOptions(baseOptionsBuilder.build())
                    .setRunningMode(RunningMode.LIVE_STREAM)
                    .setResultListener(this::returnLivestreamResult)
                    .setErrorListener(this::returnLivestreamError)
                    .setNumHands(2)
                    .setMinHandDetectionConfidence(0.5f)
                    .setMinHandPresenceConfidence(0.5f)
                    .setMinTrackingConfidence(0.5f)
                    .build();

            handLandmarker = HandLandmarker.createFromOptions(context, options);
        } catch (Throwable t) {
            // UnsatisfiedLinkError も含めてすべてキャッチ
            Log.e(TAG, "MediaPipe could not be initialized: " + t.getMessage());
            if (listener != null) {
                listener.onError("MediaPipeは無効化されました（ライブラリエラー）");
            }
        }
    }

    public void detectLiveStream(Bitmap bitmap, boolean isFrontCamera) {
        if (handLandmarker == null) return; // 初期化に失敗している場合は何もしない
        
        long frameTime = SystemClock.uptimeMillis();
        MPImage mpImage = new BitmapImageBuilder(bitmap).build();
        
        handLandmarker.detectAsync(mpImage, frameTime);
    }

    private void returnLivestreamResult(HandLandmarkerResult result, MPImage image) {
        listener.onResults(result, image);
    }

    private void returnLivestreamError(RuntimeException error) {
        listener.onError(error.getMessage());
    }

    public void clear() {
        if (handLandmarker != null) {
            handLandmarker.close();
            handLandmarker = null;
        }
    }
}
