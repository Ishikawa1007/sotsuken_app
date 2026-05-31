package com.example.sotsuken_app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

public class Page3Fragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // page3.xml を読み込む
        View view = inflater.inflate(R.layout.page3, container, false);

        // page2 に戻るボタン
        Button btnToPage2 = view.findViewById(R.id.button31);
        btnToPage2.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, new Page2Fragment())
                    .addToBackStack(null)
                    .commit();
        });

        // page4 に進むボタン
        Button btnToPage4 = view.findViewById(R.id.button32);
        btnToPage4.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, new Page4Fragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }
}
