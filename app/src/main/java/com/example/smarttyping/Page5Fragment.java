package com.example.smarttyping;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
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
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Plane;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.mediapipe.framework.image.MPImage;
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class Page5Fragment extends Fragment implements HandLandmarkerHelper.LandmarkerListener, GLSurfaceView.Renderer {

    private static final String TAG = "SmartTyping_Page5";
    private int countdownSeconds = 60;
    private final Handler handler = new Handler();
    private boolean running = true;
    
    private GLSurfaceView surfaceView;
    private Session arSession;
    private com.google.ar.core.Anchor keyboardAnchor = null;
    private int backgroundTextureId = -1;

    // 背景描画用
    private static final String VERTEX_SHADER =
            "attribute vec4 a_Position; attribute vec2 a_TexCoord; varying vec2 v_TexCoord; " +
            "void main() { gl_Position = a_Position; v_TexCoord = a_TexCoord; }";
    private static final String FRAGMENT_SHADER =
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float; varying vec2 v_TexCoord; uniform samplerExternalOES s_Texture; " +
            "void main() { gl_FragColor = texture2D(s_Texture, v_TexCoord); }";
    
    private int program;
    private java.nio.FloatBuffer vertexBuffer;
    private java.nio.FloatBuffer texCoordBuffer;
    
    private OverlayView overlayView;
    private TextView timerView;
    private TextView loadingText;
    private TextView targetTextView;
    private TextView inputTextView;
    private StringBuilder currentInput = new StringBuilder();

    private HandLandmarkerHelper handLandmarkerHelper;
    private ExecutorService backgroundExecutor;
    private volatile long lastAnalyzedTimeMs = 0;
    private static final long ANALYSIS_INTERVAL_MS = 100; // ARCoreとの並行処理のため少し長めに設定

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    resumeAR();
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
        surfaceView = view.findViewById(R.id.arSurfaceView5);
        overlayView = view.findViewById(R.id.overlayView5);
        loadingText = view.findViewById(R.id.textLoading5);
        targetTextView = view.findViewById(R.id.textTarget);
        inputTextView = view.findViewById(R.id.textInput);

        // GLSurfaceViewの設定
        surfaceView.setPreserveEGLContextOnPause(true);
        surfaceView.setEGLContextClientVersion(2);
        surfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        surfaceView.setRenderer(this);
        surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        overlayView.setKeyEventListener(label -> {
            requireActivity().runOnUiThread(() -> {
                if (label.equals("←")) {
                    if (currentInput.length() > 0) {
                        currentInput.deleteCharAt(currentInput.length() - 1);
                    }
                } else {
                    currentInput.append(label);
                }
                inputTextView.setText(currentInput.toString());
                checkInput();
            });
        });

        int minutes = 1;
        if (getArguments() != null) {
            minutes = getArguments().getInt("minutes", 1);
        }
        countdownSeconds = 60 * minutes;

        // MediaPipeの準備
        handLandmarkerHelper = new HandLandmarkerHelper(requireContext(), this);

        startCountdown();
        btnQuit.setOnClickListener(v -> stopTimerAndGoBack());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            resumeAR();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void resumeAR() {
        if (arSession == null) {
            try {
                arSession = new Session(requireContext());
                Config config = new Config(arSession);
                config.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);
                config.setPlaneFindingMode(Config.PlaneFindingMode.HORIZONTAL);
                arSession.configure(config);
            } catch (Exception e) {
                Log.e(TAG, "ARCore session creation failed", e);
                return;
            }
        }

        try {
            arSession.resume();
            surfaceView.onResume();
        } catch (Exception e) {
            Log.e(TAG, "ARCore session resume failed", e);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (arSession != null) {
            surfaceView.onPause();
            arSession.pause();
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        
        // テクスチャ作成
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        backgroundTextureId = textures[0];
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, backgroundTextureId);
        
        // シェーダー準備
        int vShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER);
        int fShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER);
        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vShader);
        GLES20.glAttachShader(program, fShader);
        GLES20.glLinkProgram(program);

        vertexBuffer = java.nio.ByteBuffer.allocateDirect(8 * 4).order(java.nio.ByteOrder.nativeOrder()).asFloatBuffer();
        vertexBuffer.put(new float[]{-1, -1, 1, -1, -1, 1, 1, 1}).position(0);
        texCoordBuffer = java.nio.ByteBuffer.allocateDirect(8 * 4).order(java.nio.ByteOrder.nativeOrder()).asFloatBuffer();
        texCoordBuffer.put(new float[]{0, 1, 1, 1, 0, 0, 1, 0}).position(0);
    }

    private int loadShader(int type, String code) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, code);
        GLES20.glCompileShader(shader);
        return shader;
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        if (arSession != null) {
            arSession.setDisplayGeometry(0, width, height);
        }
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (arSession == null) return;

        try {
            arSession.setCameraTextureName(backgroundTextureId);
            Frame frame = arSession.update();
            
            // 背景描画
            GLES20.glUseProgram(program);
            int posHandle = GLES20.glGetAttribLocation(program, "a_Position");
            int texHandle = GLES20.glGetAttribLocation(program, "a_TexCoord");
            GLES20.glEnableVertexAttribArray(posHandle);
            GLES20.glEnableVertexAttribArray(texHandle);
            GLES20.glVertexAttribPointer(posHandle, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);
            GLES20.glVertexAttribPointer(texHandle, 2, GLES20.GL_FLOAT, false, 0, texCoordBuffer);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

            Plane targetPlane = findFirstHorizontalPlane();
            if (targetPlane != null) {
                if (keyboardAnchor == null) {
                    keyboardAnchor = targetPlane.createAnchor(targetPlane.getCenterPose());
                }
                
                float[] viewMatrix = new float[16];
                float[] projectionMatrix = new float[16];
                float[] anchorMatrix = new float[16];
                
                frame.getCamera().getViewMatrix(viewMatrix, 0);
                frame.getCamera().getProjectionMatrix(projectionMatrix, 0, 0.1f, 100.0f);
                keyboardAnchor.getPose().toMatrix(anchorMatrix, 0);
                
                overlayView.setArMatrices(viewMatrix, projectionMatrix, anchorMatrix);
                if (loadingText != null && loadingText.getVisibility() == View.VISIBLE) {
                    requireActivity().runOnUiThread(() -> loadingText.setVisibility(View.GONE));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error onDrawFrame", e);
        }
    }

    private Plane findFirstHorizontalPlane() {
        for (Plane plane : arSession.getAllTrackables(Plane.class)) {
            if (plane.getTrackingState() == TrackingState.TRACKING && 
                plane.getType() == Plane.Type.HORIZONTAL_UPWARD_FACING) {
                return plane;
            }
        }
        return null;
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

    private void checkInput() {
        String target = targetTextView.getText().toString();
        String input = currentInput.toString();
        if (input.equals(target)) {
            Toast.makeText(requireContext(), "正解！", Toast.LENGTH_SHORT).show();
            // 次の単語へ行くか、セッション終了などの処理
            finishSession();
        }
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
        surfaceView = null;
        overlayView = null;
        timerView = null;
        loadingText = null;
    }
}
