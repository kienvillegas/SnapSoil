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

        Log.d(TAG, "onBindViewHolder: Crop = " + crop);

        holder.tvResultNitrogen.setText(df.format(nitrogen));
        holder.tvResultPhosphorus.setText(df.format(phosphorus));
        holder.tvResultPotassium.setText(df.format(potassium));
        holder.tvResultpH.setText(df.format(pH));
        holder.tvDate.setText("Petsa: " + date);
        holder.tvResultCrop.setText(crop);
    }
    @Override
    public int getItemCount() {
        return historyDataList.size();
    }
}
