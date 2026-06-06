package com.example.smarttyping;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

public class Page4Fragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page4, container, false);

        Button btnBack = view.findViewById(R.id.button41);
        Button btnPlay = view.findViewById(R.id.button42);

        // Page3から送られてきたデータを取得
        int minutesTmp = 1;  // デフォルト値
        if (getArguments() != null) {
            minutesTmp = getArguments().getInt("minutes", 1);
        }
        final int minutes = minutesTmp;  // ★ラムダで使うためにfinal化

        btnBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, new Page3Fragment())
                    .addToBackStack(null)
                    .commit();
        });

        btnPlay.setOnClickListener(v -> {
            Page5Fragment page5 = new Page5Fragment();
            Bundle bundle = new Bundle();

            // ★Page5に minutes を渡す
            bundle.putInt("minutes", minutes);

            page5.setArguments(bundle);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, page5)
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }
}
