package com.example.smarttyping;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class Page2Fragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // page2.xml を読み込む
        View view = inflater.inflate(R.layout.page2, container, false);

        // page1 に戻るボタン
        Button btnToPage1 = view.findViewById(R.id.button21);
        btnToPage1.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, new Page1Fragment())
                    .addToBackStack(null)
                    .commit();
        });

        // page3 に進むボタン
        Button btnToPage3 = view.findViewById(R.id.button22);
        btnToPage3.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, new Page3Fragment())
                    .addToBackStack(null)
                    .commit();
        });

        // スライダーの設定
        SeekBar slider = view.findViewById(R.id.slider);
        TextView textLabel = view.findViewById(R.id.textLabel);

        // スライダーの最大値を4に設定（0～4で、+1すると1～5の値になる）
        slider.setMax(4);
        // 初期値を0に設定（表示は1分）
        slider.setProgress(0);
        textLabel.setText("じかん: 1分");

        // スライダーの変化をリッスン
        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // progress は 0～4、+1で 1～5 として表示
                int minutes = progress + 1;
                textLabel.setText("じかん: " + minutes + "分");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        return view;
    }
}
