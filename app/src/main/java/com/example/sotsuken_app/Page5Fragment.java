package com.example.sotsuken_app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

public class Page5Fragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // page5.xml を読み込む
        View view = inflater.inflate(R.layout.page5, container, false);

        // page4 に戻るボタン
        Button btnToPage4 = view.findViewById(R.id.button51);
        btnToPage4.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, new Page1Fragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }
}
