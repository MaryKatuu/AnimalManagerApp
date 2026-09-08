package com.example.animalmanagerapp.model;

/** A single production output entry for an animal — eggs, milk, etc. */
public class OutputLog {

    private long id;
    private long animalId;
    private String outputDate; // yyyy-MM-dd
    private String quantity;   // numeric, e.g. "12"
    private String unit;       // e.g. "Eggs", "Litres", "Kg"
    private String notes;

    public OutputLog() { }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getAnimalId() { return animalId; }
    public void setAnimalId(long animalId) { this.animalId = animalId; }

    public String getOutputDate() { return outputDate; }
    public void setOutputDate(String outputDate) { this.outputDate = outputDate; }

    public String getQuantity() { return quantity; }
    public void setQuantity(String quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}