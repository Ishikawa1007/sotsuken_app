package com.example.smarttyping;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.util.Size;
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

public class Page4Fragment extends Fragment implements HandLandmarkerHelper.LandmarkerListener {

    private static final String TAG = "SmartTyping_Page4";
    private PreviewView previewView;
    private OverlayView overlayView;
    private TextView loadingText;
    private int minutes = 1;

    private HandLandmarkerHelper handLandmarkerHelper;
    private ExecutorService backgroundExecutor;
    // Throttle inference to approximately 20 FPS (50 ms interval)
    private volatile long lastAnalyzedTimeMs = 0;
    private static final long ANALYSIS_INTERVAL_MS = 50;

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
        return inflater.inflate(R.layout.page4, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        backgroundExecutor = Executors.newSingleThreadExecutor();

        Button btnBack = view.findViewById(R.id.button41);
        Button btnPlay = view.findViewById(R.id.button42);
        previewView = view.findViewById(R.id.cameraPreview4);
        overlayView = view.findViewById(R.id.overlayView4);
        loadingText = view.findViewById(R.id.textLoading4);

        if (getArguments() != null) {
            minutes = getArguments().getInt("minutes", 1);
        }

        // MediaPipeの準備
        handLandmarkerHelper = new HandLandmarkerHelper(requireContext(), this);

        btnBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, new Page3Fragment())
                    .commit();
        });

        btnPlay.setOnClickListener(v -> {
            Page5Fragment page5 = new Page5Fragment();
            Bundle bundle = new Bundle();
            bundle.putInt("minutes", minutes);
            page5.setArguments(bundle);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, page5)
                    .addToBackStack(null)
                    .commit();
        });

        checkPermissionAndStartCamera();
    }

    private void checkPermissionAndStartCamera() {
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
                previewView.setScaleType(PreviewView.ScaleType.FILL_CENTER);

                // MediaPipe用の画像解析
                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                        .build();

                imageAnalysis.setAnalyzer(backgroundExecutor, imageProxy -> {
                    long now = SystemClock.uptimeMillis();
                    if (now - lastAnalyzedTimeMs < ANALYSIS_INTERVAL_MS) {
                        imageProxy.close();
                        return;
                    }
                    lastAnalyzedTimeMs = now;

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

                // フロントカメラ（内側）を優先し、なければ背面を使用
                CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
                try {
                    if (!cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)) {
                        cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                    }
                } catch (Exception e) {
                    cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                }

                cameraProvider.unbindAll();
                try {
                    cameraProvider.bindToLifecycle(getViewLifecycleOwner(), cameraSelector, preview, imageAnalysis);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to bind camera", e);
                }
                
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        backgroundExecutor.shutdown();
        if (handLandmarkerHelper != null) {
            handLandmarkerHelper.clear();
        }
        previewView = null;
        overlayView = null;
        loadingText = null;
    }
}
