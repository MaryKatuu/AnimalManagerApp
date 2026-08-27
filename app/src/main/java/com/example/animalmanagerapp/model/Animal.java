package com.example.animalmanagerapp.model;

/**
 * Represents a single animal record kept by the farmer.
 */
public class Animal {

    private long id;
    private String tagNumber;
    private String typeBreed;     // e.g. "Dairy cow - Friesian", "Goat", "Poultry batch"
    private String dateAcquired;  // stored as yyyy-MM-dd
    private String sex;           // "Male" / "Female" / "Mixed" (for poultry batches)
    private String age;           // free text, e.g. "2 years" or "6 weeks"

    public Animal() {
    }

    public Animal(long id, String tagNumber, String typeBreed, String dateAcquired,
                  String sex, String age) {
        this.id = id;
        this.tagNumber = tagNumber;
        this.typeBreed = typeBreed;
        this.dateAcquired = dateAcquired;
        this.sex = sex;
        this.age = age;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTagNumber() {
        return tagNumber;
    }

    public void setTagNumber(String tagNumber) {
        this.tagNumber = tagNumber;
    }

    public String getTypeBreed() {
        return typeBreed;
    }

    public void setTypeBreed(String typeBreed) {
        this.typeBreed = typeBreed;
    }

    public String getDateAcquired() {
        return dateAcquired;
    }

    public void setDateAcquired(String dateAcquired) {
        this.dateAcquired = dateAcquired;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }
}