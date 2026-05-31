package com.example.sotsuken_app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;  // ← AndroidX の Fragment を使う

public class HomeFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page1, container, false);

        Button btn = view.findViewById(R.id.button11);
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
