package com.example.animalmanagerapp.model;

/**
 * Represents a single health or feeding event (vaccination, deworming,
 * feeding schedule, treatment, etc.) logged against an animal.
 */
public class HealthEvent {

    private long id;
    private long animalId;
    private String eventType;
    private String eventDate; // stored as yyyy-MM-dd
    private String notes;

    public HealthEvent() {
    }

    public HealthEvent(long id, long animalId, String eventType, String eventDate, String notes) {
        this.id = id;
        this.animalId = animalId;
        this.eventType = eventType;
        this.eventDate = eventDate;
        this.notes = notes;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getAnimalId() {
        return animalId;
    }

    public void setAnimalId(long animalId) {
        this.animalId = animalId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}