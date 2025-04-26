package com.example.snapsoil;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.List;


public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.MyViewHolder> {
    private final DecimalFormat df = new DecimalFormat("0.00");

    private List<HistoryData> historyDataList;

    public HistoryAdapter(List<HistoryData> historyDataList) {
        this.historyDataList = historyDataList;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        TextView tvResultNitrogen, tvResultPhosphorus, tvResultPotassium, tvResultpH, tvDate, tvResultCrop;

        public MyViewHolder(@NonNull View itemView){
            super(itemView);
            tvResultNitrogen = itemView.findViewById(R.id.tvItemN);
            tvResultPhosphorus = itemView.findViewById(R.id.tvItemP);
            tvResultPotassium = itemView.findViewById(R.id.tvItemK);
            tvResultpH = itemView.findViewById(R.id.tvItempH);
            tvDate = itemView.findViewById(R.id.tvItemDate);
            tvResultCrop = itemView.findViewById(R.id.tvItemCrop);
        }
    }
    @NonNull
    @Override
    public HistoryAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, null);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryAdapter.MyViewHolder holder, int position) {
        HistoryData historyData = historyDataList.get(position);
        Double nitrogen, phosphorus, potassium, pH;
        String date, crop;

        nitrogen =  historyData.getNitrogen();
        phosphorus = historyData.getPhosphorus();
        potassium = historyData.getPotassium();
        pH = historyData.getpH();
        date = historyData.getCreatedAt();
        crop = historyData.getCrop();

        String nMeaning = "";
        String pMeaning = "";
        String kMeaning = "";
        String pHMeaning = "";

        nMeaning = evalNitrogen(nitrogen);
        pMeaning = evalPhosphorus(phosphorus);
        kMeaning = evalPotassium(potassium);
        pHMeaning = evalpH(pH);

        Log.d(TAG, "onBindViewHolder: Crop = " + crop);

        holder.tvResultNitrogen.setText(df.format(nitrogen) + " (" + nMeaning + ")");
        holder.tvResultPhosphorus.setText(df.format(phosphorus) + " (" + pMeaning + ")");
        holder.tvResultPotassium.setText(df.format(potassium) + " (" + kMeaning + ")");
        holder.tvResultpH.setText(df.format(pH) + " (" + pHMeaning + ")");
        holder.tvDate.setText("Petsa: " + date);
        holder.tvResultCrop.setText(crop);
    }

    public static String evalNitrogen(Double n){
        if(n <= 30){
            return "Low";
        }else if(n > 30 && n <= 150){
            return "Medium";
        }else if(n > 150 && n <= 500) {
            return "High";
        } else return "";
    }

    public static String evalPhosphorus(Double p){
        if(p <= 40){
            return "Low";
        }else if(p > 40 && p <= 100){
            return "Medium";
        }else if(p > 100 && p <= 240){
            return "High";
        }else return "";
    }
    public static String evalPotassium(Double k){
        if(k <= 40){
            return "Low";
        }else if(k > 40 && k <= 100){
            return "Medium";
        }else if(k > 100 && k <= 240){
            return "High";
        }else return "";
    }
    public static String evalpH(Double pH){
        if(pH >= 4.0 && pH <= 5.5){
            return "Low";
        }else if(pH > 5.5 && pH <= 7.5){
            return "Medium";
        }else if(pH > 7.5 && pH <= 10.0){
            return "High";
        }else return "";
    }
    @Override
    public int getItemCount() {
        return historyDataList.size();
    }
}
