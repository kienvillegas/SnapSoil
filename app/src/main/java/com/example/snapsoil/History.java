package com.example.snapsoil;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

public class History extends Fragment {
    ImageButton imBtnSort;
    private boolean isDesc = true;

    public History() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        imBtnSort = view.findViewById(R.id.imBtnSort);

        imBtnSort.setOnClickListener(v -> {
            if (isDesc) {
                imBtnSort.setImageResource(R.drawable.sort_desc);
            } else {
                imBtnSort.setImageResource(R.drawable.sort_asc);
            }
            isDesc = !isDesc;
        });

        return view;
    }
}