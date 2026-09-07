package com.example.animalmanagerapp.model;

/**
 * Represents a single animal record kept by the farmer — may represent
 * one animal or a batch (e.g. a poultry batch), tracked via quantity.
 */
public class Animal {

    private long id;
    private String tagNumber;
    private String animalType;    // e.g. "Dairy Cattle", "Poultry - Layers"
    private String variety;       // breed, e.g. "Friesian", "Kienyeji"
    private String quantity;      // total head count this record represents
    private String dateAcquired;  // stored as yyyy-MM-dd
    private String sex;
    private String age;
    private String layingCount;      // poultry only, optional
    private String traysCollected;   // poultry only, optional
    private boolean sold;
    private String soldDate;      // stored as yyyy-MM-dd
    private String saleAmount;    // free text numeric, e.g. "15000"

    public Animal() {
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTagNumber() { return tagNumber; }
    public void setTagNumber(String tagNumber) { this.tagNumber = tagNumber; }

    public String getAnimalType() { return animalType; }
    public void setAnimalType(String animalType) { this.animalType = animalType; }

    public String getVariety() { return variety; }
    public void setVariety(String variety) { this.variety = variety; }

    public String getQuantity() { return quantity; }
    public void setQuantity(String quantity) { this.quantity = quantity; }

    public String getDateAcquired() { return dateAcquired; }
    public void setDateAcquired(String dateAcquired) { this.dateAcquired = dateAcquired; }

    public String getSex() { return sex; }
    public void setSex(String sex) { this.sex = sex; }

    public String getAge() { return age; }
    public void setAge(String age) { this.age = age; }

    public String getLayingCount() { return layingCount; }
    public void setLayingCount(String layingCount) { this.layingCount = layingCount; }

    public String getTraysCollected() { return traysCollected; }
    public void setTraysCollected(String traysCollected) { this.traysCollected = traysCollected; }

    public boolean isSold() { return sold; }
    public void setSold(boolean sold) { this.sold = sold; }

    public String getSoldDate() { return soldDate; }
    public void setSoldDate(String soldDate) { this.soldDate = soldDate; }

    public String getSaleAmount() { return saleAmount; }
    public void setSaleAmount(String saleAmount) { this.saleAmount = saleAmount; }
}