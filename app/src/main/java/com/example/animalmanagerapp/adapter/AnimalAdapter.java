package com.example.animalmanagerapp.adapter;

import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.animalmanagerapp.R;
import com.example.animalmanagerapp.db.DatabaseHelper;
import com.example.animalmanagerapp.db.DateUtils;
import com.example.animalmanagerapp.model.Animal;
import com.example.animalmanagerapp.model.HealthEvent;

import java.util.List;

public class AnimalAdapter extends RecyclerView.Adapter<AnimalAdapter.AnimalViewHolder> {

    public interface OnAnimalClickListener {
        void onAnimalClick(Animal animal);
    }

    private List<Animal> animals;
    private final DatabaseHelper dbHelper;
    private final OnAnimalClickListener listener;

    public AnimalAdapter(List<Animal> animals, DatabaseHelper dbHelper, OnAnimalClickListener listener) {
        this.animals = animals;
        this.dbHelper = dbHelper;
        this.listener = listener;
    }

    public void updateData(List<Animal> newAnimals) {
        this.animals = newAnimals;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AnimalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_animal, parent, false);
        return new AnimalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AnimalViewHolder holder, int position) {
        Animal animal = animals.get(position);
        holder.tvTagNumber.setText("Tag #" + animal.getTagNumber());

        String typeLine = animal.getAnimalType();
        if (!TextUtils.isEmpty(animal.getVariety())) {
            typeLine += " • " + animal.getVariety();
        }
        holder.tvTypeBreed.setText(typeLine);
        holder.tvQuantity.setText("Qty: " + (TextUtils.isEmpty(animal.getQuantity()) ? "1" : animal.getQuantity()));

        HealthEvent lastEvent = dbHelper.getMostRecentEvent(animal.getId());
        String badgeText;
        int color;

        if (lastEvent == null) {
            holder.tvLastEvent.setText("No events logged yet");
            badgeText = "New";
            color = 0xFF9E9E9E;
        } else {
            holder.tvLastEvent.setText("Last: " + lastEvent.getEventType() + ", " +
                    DateUtils.toDisplayFormat(lastEvent.getEventDate()));

            int daysSince = DateUtils.daysSince(lastEvent.getEventDate());
            if (daysSince == Integer.MIN_VALUE) {
                badgeText = "-";
                color = 0xFF9E9E9E;
            } else if (daysSince <= 7) {
                badgeText = daysSince + "d ago";
                color = 0xFF1B5E20;
            } else if (daysSince <= 30) {
                badgeText = daysSince + "d ago";
                color = 0xFFE65100;
            } else {
                badgeText = daysSince + "d ago";
                color = 0xFFB71C1C;
            }
        }

        holder.tvStatusBadge.setText(badgeText);
        GradientDrawable bg = (GradientDrawable) holder.tvStatusBadge.getBackground().mutate();
        bg.setColor(color);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onAnimalClick(animal);
        });
    }

    @Override
    public int getItemCount() {
        return animals == null ? 0 : animals.size();
    }

    static class AnimalViewHolder extends RecyclerView.ViewHolder {
        TextView tvTagNumber, tvTypeBreed, tvQuantity, tvLastEvent, tvStatusBadge;

        AnimalViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTagNumber = itemView.findViewById(R.id.tvTagNumber);
            tvTypeBreed = itemView.findViewById(R.id.tvTypeBreed);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvLastEvent = itemView.findViewById(R.id.tvLastEvent);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
        }
    }
}