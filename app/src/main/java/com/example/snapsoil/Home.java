package com.example.snapsoil;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class Home extends Fragment {
    private static final String PREFS_NAME = "myPrefs";
    private static final int REQUEST_PICK_IMAGE = 1001;
    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private static final String TAG = "Home";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private Map<String, Nutrients> weeklyDataMap = new HashMap<>();
    private Map<String, Nutrients> monthlyDataMap = new HashMap<>();
    private Map<Integer, Nutrients> yearDataMap = new HashMap<>();

    Button btnCam, btnUpload;
    TextView weeklyTab, monthlyTab, yearlyTab;
    private boolean isWeeklyTab, isMonthlyTab, isYearlyTab = false;
    private NetworkRequestManager requestManager;
    private FirebaseAuthHelper auth;
    private FirebaseFirestoreHelper db;
    private Calendar calendar;
    private Dialog dialog;
    private LineChart nitrogenChart, phosphorusChart, potassiumChart, pHChart;
    private SharedPreferences sharedPreferences;
    List<Double> nitrogenList;
    List<Double> phosphorusList;
    List<Double> potassiumList;
    List<Double> pHList;

    public Home() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        btnCam = view.findViewById(R.id.btnCam);
        btnUpload = view.findViewById(R.id.btnUpload);
        weeklyTab = view.findViewById(R.id.weeklyTab);
        monthlyTab = view.findViewById(R.id.monthlyTab);
        yearlyTab = view.findViewById(R.id.yearlyTab);
        nitrogenChart = view.findViewById(R.id.nitrogenChart);
        phosphorusChart = view.findViewById(R.id.phosphorusChart);
        potassiumChart = view.findViewById(R.id.potassiumChart);
        pHChart = view.findViewById(R.id.pHChart);

        auth = new FirebaseAuthHelper();
        db = new FirebaseFirestoreHelper();
        calendar = Calendar.getInstance();
        String userId = auth.getUserId();

        nitrogenList = new ArrayList<>();
        phosphorusList = new ArrayList<>();
        potassiumList = new ArrayList<>();
        pHList = new ArrayList<>();

        fetchWeeklyPred(userId);

        btnCam.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("method", "cam");
            editor.apply();

            dispatchTakePictureIntent();
        });
        btnUpload.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("method", "gallery");
            editor.apply();

            openGallery();
        });

        weeklyTab.setOnClickListener(v -> {
            isWeeklyTab = true;
            isMonthlyTab = false;
            isYearlyTab = false;

            if (isWeeklyTab) {
                setActiveTab(weeklyTab, monthlyTab, yearlyTab);
                fetchWeeklyPred(userId);
//                loadFragment(new WeeklyChart());
            }
        });

        monthlyTab.setOnClickListener(v -> {
            isWeeklyTab = false;
            isMonthlyTab = true;
            isYearlyTab = false;

            if (isMonthlyTab) {
                setActiveTab(monthlyTab, weeklyTab, yearlyTab);
//                loadFragment(new MonthlyChart());
                displayMonthlyData();
            }
        });

        yearlyTab.setOnClickListener(v -> {
            isWeeklyTab = false;
            isMonthlyTab = false;
            isYearlyTab = true;

            if (isYearlyTab) {
                setActiveTab(yearlyTab, weeklyTab, monthlyTab);
//                loadFragment(new YearlyChart());
                displayYearlyData();
            }
        });

        return view;
    }

    private void fetchWeeklyPred(String userId) {
        db.getHistory(userId, true, task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "fetchWeeklyPred: " + task.getResult());
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot != null) {
                    List<String> xAxisLabels = new ArrayList<>();

                    for (DocumentSnapshot document : querySnapshot) {
                        Log.d(TAG, "fetchWeeklyPred: " + document);
                        groupByWeek(document);
                    }

                    List<Entry> nitrogenEntries = prepareChartData("Nitrogen", weeklyDataMap, xAxisLabels);
                    List<Entry> phosphorusEntries = prepareChartData("Phosphorus", weeklyDataMap, xAxisLabels);
                    List<Entry> potassiumEntries = prepareChartData("Potassium", weeklyDataMap, xAxisLabels);
                    List<Entry> pHEntries = prepareChartData("pH", weeklyDataMap, xAxisLabels);

                    displayChart(nitrogenEntries, nitrogenChart, "Nitrogen", xAxisLabels);
                    displayChart(phosphorusEntries, phosphorusChart, "Phosphorus", xAxisLabels);
                    displayChart(potassiumEntries, potassiumChart, "Potassium", xAxisLabels);
                    displayChart(pHEntries, pHChart, "pH", xAxisLabels);
                } else {
                    Log.d(TAG, "fetchWeeklyPred: querysnapshot is null");
                }
            } else {
                Log.d(TAG, "fetchWeeklyPred: " + task.getException());
            }
        });
    }

    private void groupByWeek(DocumentSnapshot document) {
        Log.d(TAG, "Grouping by week");
        String createdAt = document.getString("createdAt");
        double nitrogen = document.getDouble("nitrogen");
        double phosphorus = document.getDouble("phosphorus");
        double potassium = document.getDouble("potassium");
        double pH = document.getDouble("pH");

        try {
            Date timestamp = dateFormat.parse(createdAt);
            if (timestamp != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(timestamp);
                int weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);
                String weekKey = "Week " + weekOfYear;

                Nutrients nutrients = weeklyDataMap.getOrDefault(weekKey, new Nutrients());
                nutrients.addData(nitrogen, phosphorus, potassium, pH);
                weeklyDataMap.put(weekKey, nutrients);
            }
        } catch (ParseException e) {
            Log.e(TAG, "fetchData: Failed to parse date", e);
        }
    }

    private String getMonthName(int monthIndex) {
        String[] months = {
                "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        };
        return months[monthIndex];
    }
    private List<Entry> prepareChartData(String nutrient, Map<String, Nutrients> nutrientData, List<String> xAxisLabels) {
        List<Entry> entries = new ArrayList<>();
        List<String> sortedKeys = new ArrayList<>(nutrientData.keySet());
        Collections.sort(sortedKeys);

        Set<String> uniqueLabels = new LinkedHashSet<>(sortedKeys);

        for (int i = 0; i < sortedKeys.size(); i++) {
            String key = sortedKeys.get(i);
            Log.d(TAG, "Key: " + key);
            Nutrients nutrients = nutrientData.get(key);
            float averageValue = 0;
            switch (nutrient) {
                case "Nitrogen":
                    averageValue = (float) nutrients.getAverageNitrogen();
                    break;
                case "Phosphorus":
                    averageValue = (float) nutrients.getAveragePhosphorus();
                    break;
                case "Potassium":
                    averageValue = (float) nutrients.getAveragePotassium();
                    break;
                case "pH":
                    averageValue = (float) nutrients.getAveragepH();
                    break;
            }
            entries.add(new Entry(i, averageValue));
        }

        xAxisLabels.clear();
        xAxisLabels.addAll(uniqueLabels);

        Log.d(TAG, "Labels after processing: " + xAxisLabels.toString());
        return entries;
    }

    private void displayMonthlyData() {
        List<String> xAxisLabels = new ArrayList<>();
        xAxisLabels.add("Jan");
        xAxisLabels.add("Feb");
        xAxisLabels.add("Mar");
        xAxisLabels.add("Apr");
        xAxisLabels.add("May");
        xAxisLabels.add("June");
        xAxisLabels.add("July");
        xAxisLabels.add("Aug");
        xAxisLabels.add("Sept");
        xAxisLabels.add("Oct");
        xAxisLabels.add("Nov");
        xAxisLabels.add("Dec");


        List<Entry> nitrogenEntries = new ArrayList<>();
        nitrogenEntries.add(new Entry(0, 10));
        nitrogenEntries.add(new Entry(1, 15));
        nitrogenEntries.add(new Entry(2, 7));
        nitrogenEntries.add(new Entry(3, 12));
        nitrogenEntries.add(new Entry(4, 10));
        nitrogenEntries.add(new Entry(5, 15));
        nitrogenEntries.add(new Entry(6, 7));
        nitrogenEntries.add(new Entry(7, 12));
        nitrogenEntries.add(new Entry(8, 10));
        nitrogenEntries.add(new Entry(9, 15));
        nitrogenEntries.add(new Entry(10, 7));
        nitrogenEntries.add(new Entry(11, 12));

        List<Entry> phosphorusEntries = new ArrayList<>();
        phosphorusEntries.add(new Entry(0, 5));
        phosphorusEntries.add(new Entry(1, 8));
        phosphorusEntries.add(new Entry(2, 6));
        phosphorusEntries.add(new Entry(3, 7));
        phosphorusEntries.add(new Entry(4, 5));
        phosphorusEntries.add(new Entry(5, 8));
        phosphorusEntries.add(new Entry(6, 6));
        phosphorusEntries.add(new Entry(7, 7));
        phosphorusEntries.add(new Entry(8, 5));
        phosphorusEntries.add(new Entry(9, 8));
        phosphorusEntries.add(new Entry(10, 6));
        phosphorusEntries.add(new Entry(11, 7));

        List<Entry> potassiumEntries = new ArrayList<>();
        potassiumEntries.add(new Entry(0, 20));
        potassiumEntries.add(new Entry(1, 25));
        potassiumEntries.add(new Entry(2, 22));
        potassiumEntries.add(new Entry(3, 18));
        potassiumEntries.add(new Entry(4, 20));
        potassiumEntries.add(new Entry(5, 25));
        potassiumEntries.add(new Entry(6, 22));
        potassiumEntries.add(new Entry(7, 18));
        potassiumEntries.add(new Entry(8, 20));
        potassiumEntries.add(new Entry(9, 25));
        potassiumEntries.add(new Entry(10, 22));
        potassiumEntries.add(new Entry(11, 18));

        List<Entry> pHEntries = new ArrayList<>();
        pHEntries.add(new Entry(0, 6.5f));
        pHEntries.add(new Entry(1, 6.8f));
        pHEntries.add(new Entry(2, 6.7f));
        pHEntries.add(new Entry(3, 6.9f));
        pHEntries.add(new Entry(4, 6.5f));
        pHEntries.add(new Entry(5, 6.8f));
        pHEntries.add(new Entry(6, 6.7f));
        pHEntries.add(new Entry(7, 6.9f));
        pHEntries.add(new Entry(8, 6.5f));
        pHEntries.add(new Entry(9, 6.8f));
        pHEntries.add(new Entry(10, 6.7f));
        pHEntries.add(new Entry(11, 6.9f));

        displayChart(nitrogenEntries, nitrogenChart, "Nitrogen", xAxisLabels);
        displayChart(phosphorusEntries, phosphorusChart, "Phosphorus", xAxisLabels);
        displayChart(potassiumEntries, potassiumChart, "Potassium", xAxisLabels);
        displayChart(pHEntries, pHChart, "pH", xAxisLabels);
    }


    private void displayYearlyData() {
        List<String> xAxisLabels = new ArrayList<>();
        xAxisLabels.add("2020");
        xAxisLabels.add("2021");
        xAxisLabels.add("2022");
        xAxisLabels.add("2023");
        xAxisLabels.add("2024");

        List<Entry> nitrogenEntries = new ArrayList<>();
        nitrogenEntries.add(new Entry(0, 15));
        nitrogenEntries.add(new Entry(1, 12));
        nitrogenEntries.add(new Entry(2, 10));
        nitrogenEntries.add(new Entry(3, 7));
        nitrogenEntries.add(new Entry(4, 12));

        List<Entry> phosphorusEntries = new ArrayList<>();
        phosphorusEntries.add(new Entry(0, 5));
        phosphorusEntries.add(new Entry(1, 7));
        phosphorusEntries.add(new Entry(2, 5));
        phosphorusEntries.add(new Entry(3, 8));
        phosphorusEntries.add(new Entry(4, 7));

        List<Entry> potassiumEntries = new ArrayList<>();
        potassiumEntries.add(new Entry(0, 20));
        potassiumEntries.add(new Entry(1, 22));
        potassiumEntries.add(new Entry(2, 20));
        potassiumEntries.add(new Entry(3, 22));
        potassiumEntries.add(new Entry(4, 18));

        List<Entry> pHEntries = new ArrayList<>();
        pHEntries.add(new Entry(0, 6.5f));
        pHEntries.add(new Entry(1, 6.8f));
        pHEntries.add(new Entry(2, 6.9f));
        pHEntries.add(new Entry(3, 6.5f));
        pHEntries.add(new Entry(4, 6.7f));

        displayChart(nitrogenEntries, nitrogenChart, "Nitrogen", xAxisLabels);
        displayChart(phosphorusEntries, phosphorusChart, "Phosphorus", xAxisLabels);
        displayChart(potassiumEntries, potassiumChart, "Potassium", xAxisLabels);
        displayChart(pHEntries, pHChart, "pH", xAxisLabels);
    }


    private void displayChart(List<Entry> entries, LineChart lineChart, String label, List<String> xAxisLabels) {
        LineDataSet dataSet = new LineDataSet(entries, label);
        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(4);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xAxisLabels));

        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);

        if (xAxisLabels.size() > 1) {
            xAxis.setLabelRotationAngle(45f);
        } else {
            xAxis.setLabelRotationAngle(0f);
        }

        lineChart.setVisibleXRangeMaximum(4);
        lineChart.moveViewToX(0);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(false);

        lineChart.notifyDataSetChanged();
        lineChart.invalidate();
    }

//    private void displayChart(List<Entry> entries, LineChart lineChart, String label, List<String> xAxisLabels) {
//        LineDataSet dataSet = new LineDataSet(entries, label);
//        LineData lineData = new LineData(dataSet);
//        lineChart.setData(lineData);
//
//        XAxis xAxis = lineChart.getXAxis();
//        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
//        xAxis.setGranularity(1f);
//        xAxis.setLabelCount(xAxisLabels.size(), true);
//        xAxis.setValueFormatter(new IndexAxisValueFormatter(xAxisLabels));
//
//        if (xAxisLabels.size() > 1) {
//            xAxis.setLabelRotationAngle(45f);
//        } else {
//            xAxis.setLabelRotationAngle(0f);
//        }
//
//        lineChart.getAxisRight().setEnabled(false);
//        lineChart.getDescription().setEnabled(false);
//        lineChart.getLegend().setEnabled(false);
//
//        lineChart.notifyDataSetChanged(); // Ensure data is updated
//        lineChart.invalidate(); // Refresh chart
//    }

    private void dispatchTakePictureIntent() {
        Context context = getContext();
        if (context != null && ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            } else {
                Toast.makeText(context, "No camera app available", Toast.LENGTH_SHORT).show();
            }
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_PICK_IMAGE);
    }

    private Uri bitmapToUri(Bitmap bitmap) {
        Context context = getContext();
        if (context != null) {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, imageFileName, null);
            return Uri.parse(path);
        }
        return null;
    }

    private void passImgUri(Uri uri){
        if (uri != null) {
            Log.d(TAG, "Uri: " + uri);
            requestManager = new NetworkRequestManager();
            requestManager.requestDetection(getContext(), uri, new DetectionRequestListener() {
                @Override
                public void onRequestCompleted(String code) {
                    if(code.equals("201")){
                        displayDialog();
                    }else if(code.equals("202")){
                        Intent intent = new Intent(getContext(), ViewingPage.class);
                        intent.putExtra("imageUri", uri.toString());
                        startActivity(intent);
                    }
                }
                @Override
                public void onRequestFailed(String errorMessage) {
                    Log.e(TAG, "passImageUri: " + errorMessage);
                }
            });
        } else {
            Toast.makeText(getContext(), "Selected image URI is null", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayDialog(){
        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.soil_detection_dialog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.card);
        dialog.setCancelable(false);

        Button btn_OK = dialog.findViewById(R.id.btnSoilDetectionOK);
        btn_OK.setOnClickListener(v -> {
            SharedPreferences sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String methodPref = sharedPreferences.getString("method", "cam");
            Log.d(TAG, "Method: " + methodPref);
            dialog.dismiss();
            if(methodPref.equalsIgnoreCase("cam")){
                dispatchTakePictureIntent();
            }else{
                openGallery();
            }
        });
        dialog.show();
    }
    private void setActiveTab(TextView activeTab, TextView... inactiveTabs) {
        activeTab.setEnabled(false);
        activeTab.setBackgroundResource(R.drawable.back_select);
        activeTab.setTextColor(getResources().getColor(R.color.colorAccent));

        for (TextView tab : inactiveTabs) {
            tab.setEnabled(true);
            tab.setBackgroundResource(0);
            tab.setTextColor(getThemeColor(tab.getContext(), R.attr.textColorPrimary));
        }
    }

//    private void loadFragment(Fragment fragment) {
//        FragmentManager fragmentManager = getChildFragmentManager();
//        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//        fragmentTransaction.replace(R.id.fragmentContainerView3, fragment);
//        fragmentTransaction.commit();
//    }

    private int getThemeColor(Context context, int attr) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attr, typedValue, true);
        return typedValue.data;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(getContext(), "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == MainActivity.RESULT_OK) {
            if (requestCode == REQUEST_PICK_IMAGE && data != null) {
                Uri selectedImageUri = data.getData();
                passImgUri(selectedImageUri);
            } else if (requestCode == REQUEST_IMAGE_CAPTURE && data != null) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    if (imageBitmap != null) {
                        passImgUri(bitmapToUri(imageBitmap));
                    } else {
                        Toast.makeText(getContext(), "Failed to capture image", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "No data received", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.d(TAG, "onActivityResult: " + requestCode);
            }
        } else {
            Log.d(TAG, "onActivityResult: " + resultCode);
        }
    }
}
