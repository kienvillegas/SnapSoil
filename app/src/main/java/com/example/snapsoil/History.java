package com.example.snapsoil;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class History extends Fragment {
    private static final String PREFS_NAME = "myPrefs";

    private SharedPreferences sharedPreferences;

    private boolean isDesc;
    List<HistoryData> historyDataList;
    private HistoryData historyData;
    private HistoryAdapter historyAdapter;
    private FirebaseFirestoreHelper db;
    private FirebaseAuthHelper auth;
    ImageButton imBtnSort;
    RecyclerView recyclerView;
    ProgressBar pbHistory;

    public History() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        imBtnSort = view.findViewById(R.id.imBtnSort);
        recyclerView = view.findViewById(R.id.recycler_view);
        pbHistory = view.findViewById(R.id.pbHistory);

        db = new FirebaseFirestoreHelper();
        auth = new FirebaseAuthHelper();
        historyDataList = new ArrayList<>();
        historyAdapter = new HistoryAdapter(historyDataList);

        recyclerView.setAdapter(historyAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fetchUserHistory();
        isDesc = sharedPreferences.getBoolean("asc", true);

        imBtnSort.setOnClickListener(v -> {
            pbHistory.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            toggleSortOrder();
            fetchUserHistory();
        });

        return view;
    }
    private void toggleSortOrder() {
        isDesc = !isDesc;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("asc", isDesc);
        editor.apply();
        imBtnSort.setImageResource(isDesc ? R.drawable.sort_desc : R.drawable.sort_asc);
    }
    private void fetchUserHistory(){
        boolean asc = sharedPreferences.getBoolean("asc", false);
        String userId = auth.getUserId();
        Log.d(TAG, "USER id: " + userId);
        db.getHistory(userId, asc, task -> {
            if(task.isSuccessful()){
                Log.d(TAG, "fetchUserHistory: Successful");
                QuerySnapshot querySnapshot = task.getResult();
                if(querySnapshot != null){
                    historyDataList.clear();
                    Log.d(TAG, "fetchUserHistory: " + querySnapshot.getDocuments());
                    for(DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()){
                        Log.d(TAG, "fetchUserHistory: " + documentSnapshot);
                        addToHistory(documentSnapshot);
                    }
                    historyAdapter.notifyDataSetChanged();
                }else{
                    Log.d(TAG, "fetchUserHistory: querysnapshot is null");
                }
            }else{
                Log.d(TAG, "fetchUserHistory: " + task.getException());
            }
        });
    }
    private void addToHistory(DocumentSnapshot documentSnapshot){
        Log.d(TAG, "addToHistory: History added to recyclerview");

        double nitrogen, phosphorus, potassium, pH;
        String date;
        nitrogen = documentSnapshot.getDouble("nitrogen");
        phosphorus = documentSnapshot.getDouble("phosphorus");
        potassium = documentSnapshot.getDouble("potassium");
        pH = documentSnapshot.getDouble("pH");
        date = documentSnapshot.getString("createdAt");
        Log.d(TAG, "Date: " + date);
        pbHistory.setVisibility(View.GONE);

        historyData = new HistoryData(nitrogen, phosphorus, potassium, pH, date);
        historyDataList.add(historyData);

        pbHistory.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }
}