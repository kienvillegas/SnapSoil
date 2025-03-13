package com.example.snapsoil;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;

public class Info extends Fragment {

    Button imgBtnNutrients, imgBtnDemo;
    Dialog pbDialog;
    public Info() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    private void renderCircleProgressBar(){
        pbDialog = new Dialog(getContext());
        pbDialog.setContentView(R.layout.progress_bar_dialog);
        pbDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        pbDialog.getWindow().setBackgroundDrawableResource(R.drawable.card);
        pbDialog.setCancelable(false);
        pbDialog.show();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       View view = inflater.inflate(R.layout.fragment_info, container, false);
       getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

       imgBtnNutrients = view.findViewById(R.id.imgBtnNutrients);
       imgBtnDemo = view.findViewById(R.id.imgBtnDemo);


        imgBtnNutrients.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), InfoSoilNutrients.class);
            startActivity(intent);
        });

        imgBtnDemo.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AppDemo.class);
            startActivity(intent);
        });
       return view;
    }
}