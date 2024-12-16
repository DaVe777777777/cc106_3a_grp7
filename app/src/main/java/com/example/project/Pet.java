package com.example.project;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.Date;


public class Pet implements Serializable {
    private int id;
    private String name;
    private String gender;
    private String dob; // Date of birth in "yyyy-MM-dd" format
    private double height;
    private double weight;
    private String breed;
    private String imageUri; // Profile picture URI
    private String user; // User associated with the pet

    // Constructor with ID
    public Pet(int id, String name, String gender, String dob, double height, double weight, String breed, String imageUri, String user) {
        this.id = id;
        this.name = name;
        this.gender = gender;
        this.dob = dob;
        this.height = height;
        this.weight = weight;
        this.breed = breed;
        this.imageUri = imageUri;
        this.user = user; // Initialize the user
    }

    // Constructor without ID (for scenarios where ID is not immediately known)
    public Pet(String name, String gender, String dob, double height, double weight, String breed, String imageUri) {
        this.name = name;
        this.gender = gender;
        this.dob = dob;
        this.height = height;
        this.weight = weight;
        this.breed = breed;
        this.imageUri = imageUri;
    }

    

    public Pet() {

    }





    // Getter for ID
    public int getId() {
        return id;
    }

    // Setter for ID (in case you need to update it later)
    public void setId(int id) {
        this.id = id;
    }

    // Getter methods
    public String getName() {
        return name;
    }

    public String getGender() {
        return gender;
    }

    public String getDob() {
        return dob;
    }

    public double getHeight() {
        return height;
    }

    public double getWeight() {
        return weight;
    }

    public String getBreed() {
        return breed;
    }

    public String getImageUri() {
        return imageUri;
    }

    public String getUser() {
        return user; // Getter for user
    }

    public int getBirthdayYear() {
        return getDateFromString(dob).getYear() + 1900; // Add 1900 because getYear() returns year since 1900
    }

    public int getBirthdayMonth() {
        return getDateFromString(dob).getMonth(); // getMonth() returns 0-11
    }

    public int getBirthdayDay() {
        return getDateFromString(dob).getDate(); // getDate() returns the day of the month
    }

    private Date getDateFromString(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return sdf.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null; // Handle parse exception accordingly
        }
    }

    // Setter methods for updating values if needed
    public void setName(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        this.name = name;
    }

    public void setGender(String gender) {
        if (gender == null || gender.isEmpty()) {
            throw new IllegalArgumentException("Gender cannot be empty");
        }
        this.gender = gender;
    }

    public void setDob(String dob) {
        // You may want to add a date format check here
        this.dob = dob;
    }

    public void setHeight(double height) {
        if (height <= 0) {
            throw new IllegalArgumentException("Height must be positive");
        }
        this.height = height;
    }

    public void setWeight(double weight) {
        if (weight <= 0) {
            throw new IllegalArgumentException("Weight must be positive");
        }
        this.weight = weight;
    }

    public void setBreed(String breed) {
        if (breed == null || breed.isEmpty()) {
            throw new IllegalArgumentException("Breed cannot be empty");
        }
        this.breed = breed;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri; // This can be null or empty if no image is set
    }

    public void setUser(String user) {
        this.user = user; // Setter for user
    }

    // Method to update properties
    public void updatePet(String name, String gender, String dob, double height, double weight, String breed, String imageUri) {
        setName(name);
        setGender(gender);
        setDob(dob);
        setHeight(height);
        setWeight(weight);
        setBreed(breed);
        setImageUri(imageUri);
    }

    // Method to calculate the age of the pet
    public int getAge() {
        LocalDate birthDate = LocalDate.parse(dob);
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    // Override toString() for debugging
    @Override
    public String toString() {
        return "Pet{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", gender='" + gender + '\'' +
                ", dob='" + dob + '\'' +
                ", height=" + height +
                ", weight=" + weight +
                ", breed='" + breed + '\'' +
                ", imageUri='" + imageUri + '\'' +
                ", user='" + user + '\'' +
                '}';
    }
}