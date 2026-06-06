package com.example.smarttyping;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;  // ← Fragmentを使うために必須

public class Page3Fragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page3, container, false);

        Button btnBack = view.findViewById(R.id.button31);
        Button btnNext = view.findViewById(R.id.button32);

        // Page2から送られてきたデータを取得
        int minutesTmp = 1;  // デフォルト値
        if (getArguments() != null) {
            minutesTmp = getArguments().getInt("minutes", 1);
        }
        final int minutes = minutesTmp;  // ★ラムダで使うためにfinal化

        btnBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, new Page2Fragment())
                    .addToBackStack(null)
                    .commit();
        });

        btnNext.setOnClickListener(v -> {
            Page4Fragment page4 = new Page4Fragment();
            Bundle bundle = new Bundle();
            bundle.putInt("minutes", minutes);  // ★ここで使える
            page4.setArguments(bundle);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, page4)
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }
}
