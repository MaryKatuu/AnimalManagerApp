package com.example.animalmanagerapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.animalmanagerapp.R;
import com.example.animalmanagerapp.db.DateUtils;
import com.example.animalmanagerapp.model.OutputLog;

import java.util.List;

public class OutputAdapter extends RecyclerView.Adapter<OutputAdapter.OutputViewHolder> {

    private List<OutputLog> logs;

    public OutputAdapter(List<OutputLog> logs) {
        this.logs = logs;
    }

    public void updateData(List<OutputLog> newLogs) {
        this.logs = newLogs;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OutputViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_output, parent, false);
        return new OutputViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OutputViewHolder holder, int position) {
        OutputLog log = logs.get(position);
        holder.tvOutputQuantity.setText(log.getQuantity() + " " + log.getUnit());
        holder.tvOutputDate.setText(DateUtils.toDisplayFormat(log.getOutputDate()));
    }

    @Override
    public int getItemCount() {
        return logs == null ? 0 : logs.size();
    }

    static class OutputViewHolder extends RecyclerView.ViewHolder {
        TextView tvOutputQuantity, tvOutputDate;

        OutputViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOutputQuantity = itemView.findViewById(R.id.tvOutputQuantity);
            tvOutputDate = itemView.findViewById(R.id.tvOutputDate);
        }
    }
}