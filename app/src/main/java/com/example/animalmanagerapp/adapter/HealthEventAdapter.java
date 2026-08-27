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
import com.example.animalmanagerapp.model.HealthEvent;

import java.util.List;

/**
 * Binds the list of health/feeding events (vaccination, deworming,
 * feeding schedule, treatment, etc.) logged against a specific animal.
 */
public class HealthEventAdapter extends RecyclerView.Adapter<HealthEventAdapter.EventViewHolder> {

    private List<HealthEvent> events;

    public HealthEventAdapter(List<HealthEvent> events) {
        this.events = events;
    }

    public void updateData(List<HealthEvent> newEvents) {
        this.events = newEvents;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_health_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        HealthEvent event = events.get(position);
        holder.tvEventType.setText(event.getEventType());
        holder.tvEventDate.setText(DateUtils.toDisplayFormat(event.getEventDate()));

        if (!TextUtils.isEmpty(event.getNotes())) {
            holder.tvEventNotes.setText(event.getNotes());
            holder.tvEventNotes.setVisibility(View.VISIBLE);
        } else {
            holder.tvEventNotes.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return events == null ? 0 : events.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventType, tvEventDate, tvEventNotes;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventType = itemView.findViewById(R.id.tvEventType);
            tvEventDate = itemView.findViewById(R.id.tvEventDate);
            tvEventNotes = itemView.findViewById(R.id.tvEventNotes);
        }
    }
}