package com.example.sotsuken_app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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

        return view;
    }
}
