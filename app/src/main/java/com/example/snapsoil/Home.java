package com.example.snapsoil;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.provider.MediaStore;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.ByteArrayOutputStream;


public class Home extends Fragment {
    private static final int REQUEST_PICK_IMAGE = 1001;
    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private static final int REQUEST_CAMERA_PERMISSION = 200;

    Button btnCam, btnUpload;
    TextView weeklyTab, monthlyTab, yearlyTab;
    private boolean isWeeklyTab, isMonthlyTab, isYearlyTab = false;

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
        btnCam = view.findViewById(R.id.btnCam);
        btnUpload = view.findViewById(R.id.btnUpload);
        weeklyTab = view.findViewById(R.id.weeklyTab);
        monthlyTab = view.findViewById(R.id.monthlyTab);
        yearlyTab = view.findViewById(R.id.yearlyTab);

        btnCam.setOnClickListener(v -> {dispatchTakePictureIntent();});
        btnUpload.setOnClickListener(v -> {openGallery();});

        weeklyTab.setOnClickListener(v -> {
            isWeeklyTab = true;
            isMonthlyTab = false;
            isYearlyTab = false;

            if (isWeeklyTab) {
                setActiveTab(weeklyTab, monthlyTab, yearlyTab);
                loadFragment(new WeeklyChart());
            }
        });

        monthlyTab.setOnClickListener(v -> {
            isWeeklyTab = false;
            isMonthlyTab = true;
            isYearlyTab = false;

            if (isMonthlyTab) {
                setActiveTab(monthlyTab, weeklyTab, yearlyTab);
                loadFragment(new MonthlyChart());
            }
        });

        yearlyTab.setOnClickListener(v -> {
            isWeeklyTab = false;
            isMonthlyTab = false;
            isYearlyTab = true;

            if (isYearlyTab) {
                setActiveTab(yearlyTab, weeklyTab, monthlyTab);
                loadFragment(new YearlyChart());
            }
        });

        return view;
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

    public void passImgUri(Uri imgUri) {
        Context context = getContext();
        if (context != null) {
            Intent intent = new Intent(context, ViewingPage.class);
            intent.putExtra("imageUri", imgUri.toString());
            startActivity(intent);
        }
    }

    private Uri bitmapToUri(Bitmap bitmap) {
        Context context = getContext();
        if (context != null) {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Title", null);
            return Uri.parse(path);
        }
        return null;
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

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainerView3, fragment);
        fragmentTransaction.commit();
    }
    private int getThemeColor(Context context, int attr) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attr, typedValue, true);
        return typedValue.data;
    }

    private boolean isNightModeEnabled() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("NightMode", false);
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
            }
        }
    }
}