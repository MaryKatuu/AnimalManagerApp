package com.example.animalmanagerapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.animalmanagerapp.R;
import com.example.animalmanagerapp.catalog.AnimalImageResolver;

import java.util.List;

public class AnimalCatalogAdapter extends RecyclerView.Adapter<AnimalCatalogAdapter.CatalogViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(String typeName, boolean isCustom);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(String typeName, boolean isCustom);
    }

    /** Simple holder pairing a type name with its resolved photo (or null for icon fallback). */
    public static class Entry {
        public final String typeName;
        public final String category;
        public final String imagePath;
        public final boolean isCustom;

        public Entry(String typeName, String category, String imagePath, boolean isCustom) {
            this.typeName = typeName;
            this.category = category;
            this.imagePath = imagePath;
            this.isCustom = isCustom;
        }
    }

    private List<Entry> entries;
    private final OnItemClickListener listener;
    private final OnItemLongClickListener longClickListener;

    public AnimalCatalogAdapter(List<Entry> entries, OnItemClickListener listener, OnItemLongClickListener longClickListener) {
        this.entries = entries;
        this.listener = listener;
        this.longClickListener = longClickListener;
    }

    public void updateData(List<Entry> newEntries) {
        this.entries = newEntries;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CatalogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_animal_catalog, parent, false);
        return new CatalogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CatalogViewHolder holder, int position) {
        Entry entry = entries.get(position);
        holder.tvAnimalTypeName.setText(entry.typeName);
        AnimalImageResolver.applyAnimalImage(holder.ivAnimalTypeImage, holder.itemView.getContext(),
                entry.typeName, entry.imagePath);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(entry.typeName, entry.isCustom);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) longClickListener.onItemLongClick(entry.typeName, entry.isCustom);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return entries == null ? 0 : entries.size();
    }

    static class CatalogViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAnimalTypeImage;
        TextView tvAnimalTypeName;

        CatalogViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAnimalTypeImage = itemView.findViewById(R.id.ivAnimalTypeImage);
            tvAnimalTypeName = itemView.findViewById(R.id.tvAnimalTypeName);
        }
    }
}