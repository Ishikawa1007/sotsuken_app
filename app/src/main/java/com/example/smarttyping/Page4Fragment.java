package com.example.smarttyping;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;  // ← Fragmentを使うために必須

public class Page4Fragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page4, container, false);

        Button btnBack = view.findViewById(R.id.button41);
        Button btnPlay = view.findViewById(R.id.button42);

        btnBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, new Page3Fragment())
                    .addToBackStack(null)
                    .commit();
        });

        btnPlay.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, new Page5Fragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }
}
