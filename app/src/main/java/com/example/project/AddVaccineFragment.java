package com.example.project;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddVaccineFragment extends Fragment {

    private static final String ARG_PET = "pet";
    private Pet pet;
    private EditText vaccineNameInput, drugNameInput, vaccineDateInput, vaccineTimeInput, veterinarianNameInput, clinicPlaceInput;
    private DBHelper dbHelper;
    private Calendar calendar = Calendar.getInstance();

    public static AddVaccineFragment newInstance(Pet pet) {
        AddVaccineFragment fragment = new AddVaccineFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PET, pet); // Pass the pet object
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            pet = (Pet) getArguments().getSerializable(ARG_PET); // Retrieve the pet object
        }
        dbHelper = new DBHelper(getActivity()); // Initialize the database helper
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_vaccine, container, false);

        // Initialize inputs
        vaccineNameInput = view.findViewById(R.id.VaccineNameInput);
        drugNameInput = view.findViewById(R.id.DrugNameInput);
        vaccineDateInput = view.findViewById(R.id.VaccineDateInput);
        vaccineTimeInput = view.findViewById(R.id.VaccineTimeInput);
        veterinarianNameInput = view.findViewById(R.id.VeterinarianNameInput);
        clinicPlaceInput = view.findViewById(R.id.ClinicPlaceInput);

        // Set up date and time pickers
        vaccineDateInput.setOnClickListener(v -> showDatePickerDialog());
        vaccineTimeInput.setOnClickListener(v -> showTimePickerDialog());

        // Set up buttons
        Button saveButton = view.findViewById(R.id.saveButton);
        Button cancelButton = view.findViewById(R.id.cancelButton);

        saveButton.setOnClickListener(v -> onSaveButtonClicked());
        cancelButton.setOnClickListener(v -> onCancelButtonClicked());

        return view;
    }

    // Show Date Picker dialog
    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getActivity(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateInput();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    // Show Time Picker dialog
    private void showTimePickerDialog() {
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                getActivity(),
                (view, hourOfDay, selectedMinute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, selectedMinute);
                    updateTimeInput();
                },
                hour, minute, true
        );
        timePickerDialog.show();
    }

    // Update the EditText field with the selected date
    private void updateDateInput() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        vaccineDateInput.setText(dateFormat.format(calendar.getTime()));
    }

    // Update the EditText field with the selected time
    private void updateTimeInput() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.US);
        vaccineTimeInput.setText(timeFormat.format(calendar.getTime()));
    }

    // Save the vaccine data to the database
    private void saveVaccineData() {
        String vaccineName = vaccineNameInput.getText().toString().trim();
        String drugName = drugNameInput.getText().toString().trim();
        String vaccineDate = vaccineDateInput.getText().toString().trim();
        String vaccineTime = vaccineTimeInput.getText().toString().trim();
        String veterinarianName = veterinarianNameInput.getText().toString().trim();
        String clinicPlace = clinicPlaceInput.getText().toString().trim();

        if (vaccineName.isEmpty() || drugName.isEmpty() || vaccineDate.isEmpty() || vaccineTime.isEmpty() ||
                veterinarianName.isEmpty() || clinicPlace.isEmpty()) {
            Toast.makeText(getActivity(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (pet == null) {
            Toast.makeText(getActivity(), "Pet data is unavailable", Toast.LENGTH_SHORT).show();
            return;
        }

        // Insert vaccine data into the database
        boolean success = dbHelper.insertVaccine(vaccineName, drugName, vaccineDate, vaccineTime, pet.getId(), veterinarianName, clinicPlace);

        if (success) {
            Snackbar.make(requireView(), "Vaccine added successfully!", Snackbar.LENGTH_SHORT).show();
            scheduleVaccineNotification(vaccineName, vaccineDate, vaccineTime); // Schedule the notification
            goToVaccinationFragment(); // Navigate to VaccinationFragment
        } else {
            Snackbar.make(requireView(), "Failed to add vaccine", Snackbar.LENGTH_SHORT).show();
        }
    }


    private void scheduleVaccineNotification(String vaccineName, String vaccineDate, String vaccineTime) {
        // Convert the selected date and time into milliseconds
        long notificationTime = calendar.getTimeInMillis(); // Calendar is already set with the selected date and time

        // Create an intent to trigger the NotificationReceiver
        Intent intent = new Intent(getActivity(), NotificationReceiver.class);

        // Correct the keys for notification extras
        String title = "Vaccine Reminder for " + pet.getName();
        String message = pet.getName() + " needs the " + vaccineName + " vaccine on " + vaccineDate + " at " + vaccineTime;

        intent.putExtra("notificationTitle", title); // Set the title as the pet name with reminder
        intent.putExtra("notificationText", message); // Set the message with the vaccine name, date, and time

        // Create PendingIntent with FLAG_IMMUTABLE
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Schedule the notification
        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, notificationTime, pendingIntent);
        }
    }



    // Navigate to VaccinationFragment
    private void goToVaccinationFragment() {
        FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
        VaccinationFragment vaccinationFragment = VaccinationFragment.newInstance(pet);
        fragmentTransaction.replace(R.id.fragment_container, vaccinationFragment); // Ensure this is the correct container ID
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void onCancelButtonClicked() {
        goToVaccinationFragment(); // Go back to the VaccinationFragment when 'Cancel' is clicked
    }

    private void onSaveButtonClicked() {
        saveVaccineData(); // Save the vaccine data and navigate to VaccinationFragment
    }
}
