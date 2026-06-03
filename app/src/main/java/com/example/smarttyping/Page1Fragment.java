package com.example.smarttyping;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

public class Page1Fragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.page1, container, false);

        // ボタン取得
        Button btn = view.findViewById(R.id.button11);

        // ボタン押したら Page2Fragment に遷移
        btn.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, new Page2Fragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }
}
