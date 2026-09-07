package com.example.animalmanagerapp.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.animalmanagerapp.R;
import com.example.animalmanagerapp.db.DateUtils;
import com.example.animalmanagerapp.model.Animal;

import java.util.List;

public class SoldAnimalAdapter extends RecyclerView.Adapter<SoldAnimalAdapter.SoldViewHolder> {

    public interface OnSoldAnimalClickListener {
        void onAnimalClick(Animal animal);
    }

    private List<Animal> animals;
    private final OnSoldAnimalClickListener listener;

    public SoldAnimalAdapter(List<Animal> animals, OnSoldAnimalClickListener listener) {
        this.animals = animals;
        this.listener = listener;
    }

    public void updateData(List<Animal> newAnimals) {
        this.animals = newAnimals;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SoldViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sold_animal, parent, false);
        return new SoldViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SoldViewHolder holder, int position) {
        Animal animal = animals.get(position);
        String title = "Tag #" + animal.getTagNumber() + " — " + animal.getAnimalType();
        if (!TextUtils.isEmpty(animal.getVariety())) {
            title += " (" + animal.getVariety() + ")";
        }
        holder.tvTagNumber.setText(title);
        holder.tvSoldDate.setText("Sold: " + DateUtils.toDisplayFormat(animal.getSoldDate()));
        holder.tvSaleAmount.setText("Sale amount: " +
                (TextUtils.isEmpty(animal.getSaleAmount()) ? "Not recorded" : "KES " + animal.getSaleAmount()));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onAnimalClick(animal);
        });
    }

    @Override
    public int getItemCount() {
        return animals == null ? 0 : animals.size();
    }

    static class SoldViewHolder extends RecyclerView.ViewHolder {
        TextView tvTagNumber, tvSoldDate, tvSaleAmount;

        SoldViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTagNumber = itemView.findViewById(R.id.tvTagNumber);
            tvSoldDate = itemView.findViewById(R.id.tvSoldDate);
            tvSaleAmount = itemView.findViewById(R.id.tvSaleAmount);
        }
    }
}