package com.example.smarttyping;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

public class Page6Fragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // page6.xml を読み込む
        View view = inflater.inflate(R.layout.page6, container, false);

        // page7 に進むボタン
        Button btnToPage7 = view.findViewById(R.id.button61);
        btnToPage7.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, new Page7Fragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }
}
