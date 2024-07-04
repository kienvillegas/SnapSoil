package com.example.snapsoil;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseUser;

public class MonthlyChart extends Fragment {
    private static FirebaseAuthHelper firebaseAuthHelper;

    public MonthlyChart() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_monthly_chart, container, false);
        firebaseAuthHelper = new FirebaseAuthHelper();
        return view;
    }
    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = firebaseAuthHelper.getCurrentUser();
        if(currentUser != null){
            currentUser.reload();
        }
    }
}