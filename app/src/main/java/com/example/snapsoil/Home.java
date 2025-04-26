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
import android.os.Build;
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
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InsertGesture;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.renderer.YAxisRenderer;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.transform.Templates;

public class Home extends Fragment {
    private static final String PREFS_NAME = "myPrefs";
    private static final int REQUEST_PICK_IMAGE = 1001;
    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private static final String TAG = "Home";


    Button btnCam, btnUpload;
    TextView weeklyTab, monthlyTab, yearlyTab;
    private boolean isWeeklyTab, isMonthlyTab, isYearlyTab = false;
    private NetworkRequestManager requestManager;
    private FirebaseAuthHelper auth;
    private FirebaseFirestoreHelper db;
    private Calendar calendar;
    private Dialog dialog;
    private LineChart nitrogenChart, phosphorusChart, potassiumChart, pHChart;
    List<Nutrients> nutrientsList;

    DataAggregator dataAggregator = new DataAggregator();
    Map<Integer, Map<String, Double>> nutrientAveragesMap;
    List<String> xAxisLabels;
    Dialog pbDialog;

    public Home() {
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
        nutrientsList = new ArrayList<>();

        fetchData("weekly");

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
                fetchData("weekly");
            }
        });

        monthlyTab.setOnClickListener(v -> {
            isWeeklyTab = false;
            isMonthlyTab = true;
            isYearlyTab = false;

            if (isMonthlyTab) {
                setActiveTab(monthlyTab, weeklyTab, yearlyTab);
                fetchData("monthly");
            }
        });

        yearlyTab.setOnClickListener(v -> {
            isWeeklyTab = false;
            isMonthlyTab = false;
            isYearlyTab = true;

            if (isYearlyTab) {
                setActiveTab(yearlyTab, weeklyTab, monthlyTab);
                fetchData("yearly");
            }
        });

        return view;
    }
    private  void fetchData(String label){
        renderCircleProgressBar();

        String userId = auth.getUserId();
        db.getHistory(userId, task -> {
            if(task.isSuccessful()){
                QuerySnapshot querySnapshot = task.getResult();
                for(DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()){
                    storeData(documentSnapshot);
                }
                pbDialog.dismiss();
            }else{
                Log.e(TAG, "fetchData: " + task.getException());
            }

            if(label.equalsIgnoreCase("weekly")) nutrientAveragesMap = dataAggregator.getAverageByWeek(nutrientsList);
            else if(label.equalsIgnoreCase("monthly")) nutrientAveragesMap = dataAggregator.getAverageByMonth(nutrientsList);
            else if(label.equalsIgnoreCase("yearly"))nutrientAveragesMap = dataAggregator.getAverageByYear(nutrientsList);

            xAxisLabels = getLabels(label, nutrientAveragesMap);
            displayChart(nutrientAveragesMap, nitrogenChart, "nitrogen", xAxisLabels);
            displayChart(nutrientAveragesMap, phosphorusChart, "phosphorus", xAxisLabels);
            displayChart(nutrientAveragesMap, potassiumChart, "potassium", xAxisLabels);
            displayChart(nutrientAveragesMap, pHChart, "ph", xAxisLabels);


        });
    }
    private void storeData(DocumentSnapshot documentSnapshot){
        double nitrogen, phosphorus, potassium, pH;
        String date;

        nitrogen = documentSnapshot.getDouble("nitrogen");
        phosphorus = documentSnapshot.getDouble("phosphorus");
        potassium = documentSnapshot.getDouble("potassium");
        pH = documentSnapshot.getDouble("pH");
        date = documentSnapshot.getString("createdAt");

        Nutrients nutrients = new Nutrients(nitrogen, phosphorus, potassium, pH, date);
        nutrientsList.add(nutrients);
    }

    private List<String> getLabels(String label, Map<Integer, Map<String, Double>> averages){
        String[] months = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sept",
                "Oct", "Nov", "Dec"};
        xAxisLabels = new ArrayList<>();

        for(Integer key : averages.keySet()){
            if(label.equalsIgnoreCase("weekly")){
                xAxisLabels.add("Week " + key);
            }else if(label.equalsIgnoreCase("monthly")){
                xAxisLabels.add(months[key - 1]);
            }else if(label.equalsIgnoreCase("yearly")){
                xAxisLabels.add(key.toString());
            }
        }
        return xAxisLabels;
    }

    private void displayChart(Map<Integer, Map<String, Double>> averages, LineChart lineChart, String label, List<String> xAxisLabels) {
        ArrayList<Entry> entries = new ArrayList<>();

        if (averages == null || averages.isEmpty()) {
            Log.d(TAG, "No data available to display.");
            lineChart.clear(); // Clear previous chart data
            lineChart.setNoDataText("No Data Available.");
            lineChart.invalidate();
            return;
        }

        int index = 0;
        int nutrientColor;

        switch (label.toLowerCase()) {
            case "nitrogen":
                nutrientColor = Color.parseColor("#2196F3");
                break;
            case "phosphorus":
                nutrientColor = Color.parseColor("#FF9800");
                break;
            case "potassium":
                nutrientColor = Color.parseColor("#9C27B0");
                break;
            case "ph":
                nutrientColor = Color.parseColor("#4CAF50");
                break;
            default:
                nutrientColor = Color.GRAY;
                break;
        }
        for (Integer key : averages.keySet()) {
            Map<String, Double> nutrient = averages.get(key);
            if (nutrient != null) {
                Double averageVal = nutrient.get(label.toLowerCase());
                if (averageVal != null) {
                    entries.add(new Entry(index, averageVal.floatValue()));
                    index++;
                } else {
                    Log.d(TAG, "Average Value is null for key: " + key);
                    entries.add(new Entry(index, 0.0f));
                    index++;
                }
            } else {
                Log.d(TAG, "Nutrient map for key " + key + " is null");
                entries.add(new Entry(key, 0.0f));
            }
        }



//      Setting up the data for the chart
        LineDataSet dataSet = new LineDataSet(entries, label);
        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        lineChart.setData(lineData);

//      Setting the color of the chart
        dataSet.setColor(nutrientColor);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setDrawValues(false);

//      X-Axis
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setDrawLabels(true);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(xAxisLabels.size(), true);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xAxisLabels));

        if (xAxisLabels.size() > 1) {
            xAxis.setLabelRotationAngle(45f);
        } else {
            xAxis.setLabelRotationAngle(0f);
        }

//      Chart Interactions
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);
        lineChart.setDoubleTapToZoomEnabled(false);

//      Chart Display
        lineChart.setVisibleXRangeMaximum(4);
        lineChart.moveViewToX(0);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(true);

//        Chart Animation
        lineChart.animateY(1000, Easing.EaseOutBack);

        lineChart.setLogEnabled(true);
        lineChart.setNoDataText("No Data Yet.");
        lineChart.notifyDataSetChanged();
        lineChart.invalidate();
    }

    private void renderCircleProgressBar(){
        pbDialog = new Dialog(getContext());
        pbDialog.setContentView(R.layout.progress_bar_dialog);
        pbDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        pbDialog.getWindow().setBackgroundDrawableResource(R.drawable.card);
        pbDialog.setCancelable(false);
        pbDialog.show();
    }

    private void passPredictionResult(Uri uri){
        renderCircleProgressBar();

        NetworkRequestManager requestManager = new NetworkRequestManager();
        requestManager.requestPrediction(getContext(), uri, new PredictionRequestListenter() {
            @Override
            public void onRequestCompleted(HistoryData data) {
                double n, p, k, pH;
                String crop;
                n = data.getNitrogen();
                p = data.getPhosphorus();
                k = data.getPotassium();
                pH = data.getpH();
                crop = data.getCrop();


                addToHistory(data);

                Intent intent = new Intent(getContext(), ResultPage.class);
                intent.putExtra("n_pred", n);
                intent.putExtra("p_pred", p);
                intent.putExtra("k_pred", k);
                intent.putExtra("pH_pred", pH);
                intent.putExtra("crop", crop);

                pbDialog.dismiss();
                startActivity(intent);
            }

            @Override
            public void onRequestFailed(String errorMessage) {
                Log.e(TAG, "passPredictionResult: " + errorMessage);
            }
        });
    }

    private void addToHistory(HistoryData data){
        Log.d(TAG, "addToHistory: Adding to history");
        String id = auth.getUserId();
        db.addHistory(data, id, task -> {
            if(task.isSuccessful()){
                Log.d(TAG, "addToHistory: Success");
            }else{
                Log.d(TAG, "addToHistory: " + task.getException().toString());
            }
        });
    }

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
        renderCircleProgressBar();

        if (uri != null) {
            Log.d(TAG, "Uri: " + uri);
            requestManager = new NetworkRequestManager();
            requestManager.requestDetection(getContext(), uri, new DetectionRequestListener() {
                @Override
                public void onRequestCompleted(String code) {
                    if(code.equals("201")){
                        displayDialog();
                    }else if(code.equals("202")){
                        passPredictionResult(uri);
//                        pbDialog.dismiss();
                    }
                }
                @Override
                public void onRequestFailed(String errorMessage) {
                    Log.e(TAG, "passImageUri: " + errorMessage);
                    pbDialog.dismiss();
                }
            });
            pbDialog.dismiss();
        } else {
            pbDialog.dismiss();
            Toast.makeText(getContext(), "Selected image URI is null", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayDialog(){
        renderCircleProgressBar();

        Window window = getActivity().getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.alpha = 0.5f;
        window.setAttributes(layoutParams);

        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.soil_detection_dialog);
        dialog.getWindow().setLayout(1000, ViewGroup.LayoutParams.WRAP_CONTENT);
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

            layoutParams.alpha = 1.0f;
            window.setAttributes(layoutParams);
        });
        pbDialog.dismiss();
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
