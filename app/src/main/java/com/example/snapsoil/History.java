package com.example.snapsoil;

import static android.content.ContentValues.TAG;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class History extends Fragment {
    private static final String PREFS_NAME = "myPrefs";

    private SharedPreferences sharedPreferences;

    private boolean isDesc;
    List<HistoryData> historyDataList;
    private HistoryData historyData;
    private HistoryAdapter historyAdapter;
    private FirebaseFirestoreHelper db;
    private FirebaseAuthHelper auth;
    ImageButton imBtnFilter;
    RecyclerView recyclerView;
    Dialog pbDialog;

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
        imBtnFilter = view.findViewById(R.id.imBtnFilter);
        recyclerView = view.findViewById(R.id.recycler_view);

        db = new FirebaseFirestoreHelper();
        auth = new FirebaseAuthHelper();
        historyDataList = new ArrayList<>();
        historyAdapter = new HistoryAdapter(historyDataList);

        recyclerView.setAdapter(historyAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fetchUserHistory();
        isDesc = sharedPreferences.getBoolean("asc", true);

        imBtnFilter.setOnClickListener(v -> {
            renderDateRangeDialog();
        });

        return view;
    }

    private void renderDateRangeDialog(){
        final String datePattern = "^((\\d{4})-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01]))$";
        Pattern pattern = Pattern.compile(datePattern);
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.filter_date_dialog);
        dialog.getWindow().setLayout(1000, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.card);
        dialog.setCancelable(true);

        EditText etStartDate = dialog.findViewById(R.id.etStartDate);
        EditText etEndDate = dialog.findViewById(R.id.etEndDate);
        Button btnFilter = dialog.findViewById(R.id.btnFilter);
        resetEditText(etStartDate);
        resetEditText(etEndDate);
        btnFilter.setOnClickListener(v -> {
            String userId = auth.getUserId();

            String strStartDate = etStartDate.getText().toString().trim();
            String strEndDate = etEndDate.getText().toString().trim();
            Matcher startDateMatcher = pattern.matcher(strStartDate);
            Matcher endDateMatcher = pattern.matcher(strEndDate);
            boolean isStartDateMatch = startDateMatcher.find();
            boolean isEndDateMatch = endDateMatcher.find();

//          Check if empty
            if(strStartDate.isEmpty()){
                etStartDate.setError("Please Enter Date");
                etStartDate.requestFocus();
                return;
            }

            if(strEndDate.isEmpty()){
                etEndDate.setError("Please Enter Date");
                etEndDate.requestFocus();
                return;
            }

//          Check format
            if(!isStartDateMatch){
                etStartDate.requestFocus();
                etStartDate.setError("Ex. 2024-01-01");
            }else if(!isEndDateMatch){
                etEndDate.requestFocus();
                etEndDate.setError("Ex. 2024-01-01");
            }else{
                renderCircleProgressBar();
                recyclerView.setVisibility(View.GONE);
                dialog.dismiss();
                Log.d(TAG, "Filtering...");
                db.filterHistory(userId, strStartDate, strEndDate, task -> {
                    Log.d(TAG, "Accessing Database...");
                    if(task.isSuccessful()){
                        Log.d(TAG, "Filter History Success!");
                        QuerySnapshot querySnapshot = task.getResult();
                        if(querySnapshot != null){
                            historyDataList.clear();
                            for(DocumentSnapshot document : querySnapshot){
                                addToHistory(document);
                            }
                            Log.d(TAG, "Documents added to the recyclerview");
                        }
                        Log.d(TAG, "Displaying the filtered history");
                        pbDialog.dismiss();
                        recyclerView.setVisibility(View.VISIBLE);
                    }else{
                        Log.d(TAG, "filterHistory: " + task.getException().toString());
                    }
                });
            }
        });
        dialog.show();
    }

    private void renderCircleProgressBar(){
        pbDialog = new Dialog(getContext());
        pbDialog.setContentView(R.layout.progress_bar_dialog);
        pbDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        pbDialog.getWindow().setBackgroundDrawableResource(R.drawable.card);
        pbDialog.setCancelable(false);
        pbDialog.show();
    }

    private void fetchUserHistory(){
        renderCircleProgressBar();
        String userId = auth.getUserId();
        Log.d(TAG, "User id: " + userId);
        db.getHistory(userId, task -> {
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

        historyData = new HistoryData(nitrogen, phosphorus, potassium, pH, date);
        historyDataList.add(historyData);

        pbDialog.dismiss();
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void resetEditText(EditText et){
        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                et.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

}