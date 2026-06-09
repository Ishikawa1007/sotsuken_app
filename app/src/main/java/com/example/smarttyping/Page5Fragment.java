package com.example.smarttyping;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

public class Page5Fragment extends Fragment {

    private int countdownSeconds = 60;  // デフォルト値
    private Handler handler = new Handler();
    private boolean running = true;
    private PreviewView previewView;
    private static final int CAMERA_PERMISSION_CODE = 101;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page5, container, false);

        Button btnQuit = view.findViewById(R.id.button51);
        TextView timerView = view.findViewById(R.id.textTimer);
        previewView = view.findViewById(R.id.cameraPreview5);

        // Page4から送られてきたデータを取得
        int minutes = 1;
        if (getArguments() != null) {
            minutes = getArguments().getInt("minutes", 1);
        }
        countdownSeconds = 60 * minutes;

        // カウントダウン開始
        startCountdown(timerView);

        // カメラ権限のチェックと起動
        checkPermissionAndStartCamera();

        // 「やめる」ボタン
        btnQuit.setOnClickListener(v -> {
            stopTimerAndGoBack();
        });

        return view;
    }

    private void checkPermissionAndStartCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(requireContext(), "カメラ権限が必要です", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(getViewLifecycleOwner(), cameraSelector, preview);

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void startCountdown(TextView timerView) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (!running) return;

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
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, new Page1Fragment())
                .commit();
    }

    private void finishSession() {
        running = false;
        if (isAdded() && getActivity() != null) {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, new Page6Fragment())
                    .commit();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        running = false;
        handler.removeCallbacksAndMessages(null);
    }
}
