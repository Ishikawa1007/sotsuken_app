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
        // page4.xml を読み込む
        View view = inflater.inflate(R.layout.page4, container, false);

        // page3 に戻るボタン
        Button btnToPage1 = view.findViewById(R.id.button41);
        btnToPage1.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, new Page3Fragment())
                    .addToBackStack(null)
                    .commit();
        });

        // page5 に進むボタン
        Button btnToPage5 = view.findViewById(R.id.button42);
        btnToPage5.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, new Page5Fragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }
}
