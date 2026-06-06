package com.example.smarttyping;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class Page5Fragment extends Fragment {

    private int countdownSeconds = 60;  // デフォルト値は60秒（1分）
    private Handler handler = new Handler();
    private boolean running = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page5, container, false);

        Button btnQuit = view.findViewById(R.id.button51);
        TextView timerView = view.findViewById(R.id.textTimer);

        // Page4から送られてきたデータを取得
        int minutes = 1;  // デフォルト値
        if (getArguments() != null) {
            minutes = getArguments().getInt("minutes", 1);
        }

        // countdownSeconds = 60 * minutes に設定
        countdownSeconds = 60 * minutes;

        // カウントダウン開始
        startCountdown(timerView);

        // 「やめる」ボタン
        btnQuit.setOnClickListener(v -> {
            running = false;  // タイマー停止
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, new Page1Fragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    private void startCountdown(TextView timerView) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                int minutes = countdownSeconds / 60;
                int secs = countdownSeconds % 60;

                String time = String.format("%02d:%02d", minutes, secs);
                timerView.setText(time);

                if (!running) {
                    // 停止状態なら何もしない
                    return;
                }

                if (countdownSeconds > 0) {
                    // カウントダウン継続
                    countdownSeconds--;
                    handler.postDelayed(this, 1000);
                } else {
                    // タイムアップ：Page6 に遷移
                    running = false; // 再入防止
                    if (isAdded() && getActivity() != null) {
                        requireActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.container, new Page6Fragment())
                                .addToBackStack(null)
                                .commit();
                    }
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // タイマー停止とハンドラのコールバック解除
        running = false;
        handler.removeCallbacksAndMessages(null);
    }
}
