package com.example.project;

import androidx.lifecycle.LiveData;


import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.project.DBHelper;
import com.example.project.Pet;
import com.example.project.Vaccine;

import java.util.List;

public class NotificationViewModel extends ViewModel {

    private DBHelper dbHelper;
    private String username;
    private MutableLiveData<List<Pet>> petsWithBirthdayLiveData = new MutableLiveData<>();
    private MutableLiveData<List<Vaccine>> vaccinesDueTodayLiveData = new MutableLiveData<>();

    public NotificationViewModel(DBHelper dbHelper, String username) {
        this.dbHelper = dbHelper;
        this.username = username;
        loadNotifications();
    }

    private void loadNotifications() {
        // Load pets with birthdays today
        petsWithBirthdayLiveData.setValue(dbHelper.getAllPetsWithBirthdayToday(username));
        // Load vaccines due today
        vaccinesDueTodayLiveData.setValue(dbHelper.getVaccinesDueToday(username));
    }

    public LiveData<List<Pet>> getPetsWithBirthdayLiveData() {
        return petsWithBirthdayLiveData;
    }

    public LiveData<List<Vaccine>> getVaccinesDueTodayLiveData() {
        return vaccinesDueTodayLiveData;
    }

    public void refreshData() {
        loadNotifications(); // Refresh data by re-querying the DB
    }
}
