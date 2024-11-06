package com.example.project;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

public class Vaccine implements Serializable {
    private int vaccineId; // Unique identifier for the vaccine
    private String vaccineName;
    private String drugName;
    private String vaccineDate;
    private String vaccineTime; // Field for vaccine time
    private int petId;
    private String veterinarianName; // Field for veterinarian name
    private String clinicPlace; // Field for clinic location

    // Constructor
    public Vaccine(int vaccineId, String vaccineName, String drugName, String vaccineDate, String vaccineTime, int petId, String veterinarianName, String clinicPlace) {
        this.vaccineId = vaccineId;
        this.vaccineName = vaccineName;
        this.drugName = drugName;
        this.vaccineDate = vaccineDate;
        this.vaccineTime = vaccineTime;
        this.petId = petId;
        this.veterinarianName = veterinarianName;
        this.clinicPlace = clinicPlace;
    }

    public Vaccine() {
    }

    public void setVaccineId(int vaccineId) {
        this.vaccineId = vaccineId;
    }

    public void setPetId(int petId) {
        this.petId = petId;
    }

    // Getters
    public int getVaccineId() {
        return vaccineId;
    }

    public String getVaccineName() {
        return vaccineName;
    }

    public String getDrugName() {
        return drugName;
    }

    public String getVaccineDate() {
        return vaccineDate;
    }

    public String getVaccineTime() {
        return vaccineTime;
    }

    public int getPetId() {
        return petId;
    }

    public String getVetName() {
        return veterinarianName;
    }

    public String getClinicLocation() {
        return clinicPlace;
    }

    public int getDueYear() {
        return getDateFromString(vaccineDate).getYear() + 1900; // Add 1900
    }

    public int getDueMonth() {
        return getDateFromString(vaccineDate).getMonth(); // 0-11
    }

    public int getDueDay() {
        return getDateFromString(vaccineDate).getDate();
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

    // Setters for updating fields
    public void setVaccineName(String vaccineName) {
        this.vaccineName = vaccineName;
    }

    public void setDrugName(String drugName) {
        this.drugName = drugName;
    }

    public void setVaccineDate(String vaccineDate) {
        this.vaccineDate = vaccineDate;
    }

    public void setVaccineTime(String vaccineTime) {
        this.vaccineTime = vaccineTime;
    }

    public void setVetName(String veterinarianName) {
        this.veterinarianName = veterinarianName;
    }

    public void setClinicLocation(String clinicPlace) {
        this.clinicPlace = clinicPlace;
    }

    // Override toString method
    @Override
    public String toString() {
        return "Vaccine{" +
                "vaccineId=" + vaccineId +
                ", vaccineName='" + vaccineName + '\'' +
                ", drugName='" + drugName + '\'' +
                ", vaccineDate='" + vaccineDate + '\'' +
                ", vaccineTime='" + vaccineTime + '\'' +
                ", petId=" + petId +
                ", vetName='" + veterinarianName + '\'' +
                ", clinicLocation='" + clinicPlace + '\'' +
                '}';
    }
}
