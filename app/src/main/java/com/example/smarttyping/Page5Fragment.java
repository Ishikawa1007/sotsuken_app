package com.example.smarttyping;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mediapipe.framework.image.MPImage;
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Page5Fragment extends Fragment implements HandLandmarkerHelper.LandmarkerListener {

    private static final String TAG = "SmartTyping_Page5";
    private int countdownSeconds = 60;
    private final Handler handler = new Handler();
    private boolean running = true;
    private PreviewView previewView;
    private OverlayView overlayView;
    private TextView timerView;
    private TextView loadingText;

    private HandLandmarkerHelper handLandmarkerHelper;
    private ExecutorService backgroundExecutor;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startCamera();
                } else {
                    Toast.makeText(requireContext(), "カメラ権限が必要です", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.page5, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        backgroundExecutor = Executors.newSingleThreadExecutor();

        Button btnQuit = view.findViewById(R.id.button51);
        timerView = view.findViewById(R.id.textTimer);
        previewView = view.findViewById(R.id.cameraPreview5);
        overlayView = view.findViewById(R.id.overlayView5);
        loadingText = view.findViewById(R.id.textLoading5);

        int minutes = 1;
        if (getArguments() != null) {
            minutes = getArguments().getInt("minutes", 1);
        }
        countdownSeconds = 60 * minutes;

        // MediaPipeの準備
        handLandmarkerHelper = new HandLandmarkerHelper(requireContext(), this);

        startCountdown();
        checkAndStartCamera();

        btnQuit.setOnClickListener(v -> stopTimerAndGoBack());
    }

    private void checkAndStartCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void startCamera() {
        if (previewView == null) return;

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                // MediaPipe用の画像解析
                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                        .build();

                imageAnalysis.setAnalyzer(backgroundExecutor, imageProxy -> {
                    Bitmap bitmap = Bitmap.createBitmap(
                            imageProxy.getWidth(),
                            imageProxy.getHeight(),
                            Bitmap.Config.ARGB_8888
                    );
                    bitmap.copyPixelsFromBuffer(imageProxy.getPlanes()[0].getBuffer());
                    imageProxy.close();

                    if (handLandmarkerHelper != null) {
                        handLandmarkerHelper.detectLiveStream(bitmap, true);
                    }
                });

                // フロントカメラ（内側）を指定
                CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(getViewLifecycleOwner(), cameraSelector, preview, imageAnalysis);
                
                if (loadingText != null) {
                    loadingText.setVisibility(View.GONE);
                }
                Log.d(TAG, "Camera started successfully with MediaPipe");

            } catch (ExecutionException | InterruptedException e) {
                if (loadingText != null) {
                    loadingText.setText("カメラのきどうに失敗しました");
                }
                Log.e(TAG, "Camera initialization failed", e);
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    @Override
    public void onResults(HandLandmarkerResult result, MPImage image) {
        requireActivity().runOnUiThread(() -> {
            if (overlayView != null) {
                overlayView.setResults(result, image.getWidth(), image.getHeight());
            }
        });
    }

    @Override
    public void onError(String error) {
        requireActivity().runOnUiThread(() -> {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
        });
    }

    private void startCountdown() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (!running || timerView == null) return;

                int minutes = countdownSeconds / 60;
                int secs = countdownSeconds % 60;

                String time = String.format("%02d:%02d", minutes, secs);
                timerView.setText(time);

                if (countdownSeconds > 0) {
                    countdownSeconds--;
                    handler.postDelayed(this, 1000);
                } else {
                    finishSession();
                }
            }
        });
    }

    private void stopTimerAndGoBack() {
        running = false;
        if (isAdded()) {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, new Page1Fragment())
                    .commit();
        }
    }

    private void finishSession() {
        running = false;
        if (isAdded()) {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, new Page6Fragment())
                    .commit();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        backgroundExecutor.shutdown();
        if (handLandmarkerHelper != null) {
            handLandmarkerHelper.clear();
        }
        running = false;
        handler.removeCallbacksAndMessages(null);
        previewView = null;
        overlayView = null;
        timerView = null;
        loadingText = null;
    }
}
